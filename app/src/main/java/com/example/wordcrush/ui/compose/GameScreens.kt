package com.example.wordcrush.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wordcrush.R
import com.example.wordcrush.ui.model.MatchGameEvent
import com.example.wordcrush.ui.model.MatchCardType
import com.example.wordcrush.ui.model.MatchCardUiModel
import com.example.wordcrush.ui.model.MatchCardFeedback
import com.example.wordcrush.ui.viewmodel.BreakthroughUiState
import com.example.wordcrush.ui.viewmodel.BreakthroughViewModel
import com.example.wordcrush.ui.viewmodel.TimeLimitUiState
import com.example.wordcrush.ui.viewmodel.TimeLimitViewModel

private enum class MatchMode(
    val title: String,
    val subtitle: String,
    val gameType: Int,
    val emptyTitle: String,
    val emptyMessage: String
) {
    CLASSIC(
        title = "Match Challenge",
        subtitle = "Choose a mode, then start.",
        gameType = 0,
        emptyTitle = "Ready to start",
        emptyMessage = "Tap start to load a fresh set of words for classic mode."
    ),
    TIMED(
        title = "Timed Match",
        subtitle = "Choose a mode, then start.",
        gameType = 1,
        emptyTitle = "Ready to start",
        emptyMessage = "The timer begins only after you tap start."
    )
}

@Composable
internal fun MatchRoute(
    onPlayAudio: suspend (String, Int) -> Unit,
    onShowMessage: (String) -> Unit,
    onOpenRanking: (Int) -> Unit
) {
    val dims = appDimens()
    val breakthroughViewModel: BreakthroughViewModel = hiltViewModel()
    val breakthroughUiState by breakthroughViewModel.uiState.collectAsStateWithLifecycle()
    val timeLimitViewModel: TimeLimitViewModel = hiltViewModel()
    val timeLimitUiState by timeLimitViewModel.uiState.collectAsStateWithLifecycle()
    var selectedMode by rememberSaveable { androidx.compose.runtime.mutableStateOf(MatchMode.CLASSIC) }
    var showStopConfirmDialog by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    val isCurrentGameStarted = when (selectedMode) {
        MatchMode.CLASSIC -> breakthroughUiState.hasStarted
        MatchMode.TIMED -> timeLimitUiState.hasStarted
    }

    LaunchedEffect(Unit) {
        breakthroughViewModel.event.collect { event ->
            when (event) {
                is MatchGameEvent.Message -> onShowMessage(event.text)
                is MatchGameEvent.PlayAudio -> onPlayAudio(event.word, event.type)
                is MatchGameEvent.GameOver -> Unit
            }
        }
    }

    LaunchedEffect(Unit) {
        timeLimitViewModel.event.collect { event ->
            when (event) {
                is MatchGameEvent.Message -> onShowMessage(event.text)
                is MatchGameEvent.PlayAudio -> onPlayAudio(event.word, event.type)
                is MatchGameEvent.GameOver -> Unit
            }
        }
    }

    MatchGameScaffold(
        title = selectedMode.title,
        subtitle = selectedMode.subtitle,
        showHeader = !isCurrentGameStarted,
        showStats = isCurrentGameStarted,
        score = if (selectedMode == MatchMode.CLASSIC) breakthroughUiState.score else timeLimitUiState.score,
        hearts = if (selectedMode == MatchMode.CLASSIC) breakthroughUiState.hearts else timeLimitUiState.hearts,
        extra = if (selectedMode == MatchMode.TIMED) "${timeLimitUiState.remainingSeconds}s" else null,
        onPrimaryAction = {
            when (selectedMode) {
                MatchMode.CLASSIC -> breakthroughViewModel.restartGame()
                MatchMode.TIMED -> timeLimitViewModel.startGame()
            }
        },
        primaryActionLabel = when (selectedMode) {
            MatchMode.CLASSIC -> if (breakthroughUiState.hasStarted) "Restart" else "Start"
            MatchMode.TIMED -> if (timeLimitUiState.hasStarted) "Restart" else "Start"
        },
        onStopAction = {
            showStopConfirmDialog = true
        },
        stopVisible = isCurrentGameStarted,
        onSecondaryAction = { onOpenRanking(selectedMode.gameType) },
        secondaryActionLabel = "Ranking",
        topControls = {
            if (!isCurrentGameStarted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dims.controlSpacing)
                ) {
                    MatchMode.entries.forEach { mode ->
                        val selected = selectedMode == mode
                        if (selected) {
                            Button(
                                onClick = { selectedMode = mode },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(dims.buttonHeight)
                            ) {
                                Text(if (mode == MatchMode.CLASSIC) "Classic" else "Timed")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { selectedMode = mode },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(dims.buttonHeight)
                            ) {
                                Text(if (mode == MatchMode.CLASSIC) "Classic" else "Timed")
                            }
                        }
                    }
                }
            }
        },
        preActionContent = {
            if (!isCurrentGameStarted) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dims.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(dims.scaled(6.dp))
                    ) {
                        Text(
                            text = if (selectedMode == MatchMode.CLASSIC) {
                                "Today: ${breakthroughUiState.masteredTodayCount}/${breakthroughUiState.todayWordCount}"
                            } else {
                                "Today: ${timeLimitUiState.masteredTodayCount}/${timeLimitUiState.todayWordCount}"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                LearnedWordsSummaryCard(
                    summaries = if (selectedMode == MatchMode.CLASSIC) {
                        breakthroughUiState.learnedWordSummaries
                    } else {
                        timeLimitUiState.learnedWordSummaries
                    }
                )
            }
        }
    ) {
        if (isCurrentGameStarted) {
            LatestMatchedWordCard(
                word = if (selectedMode == MatchMode.CLASSIC) {
                    breakthroughUiState.latestMatchedWord
                } else {
                    timeLimitUiState.latestMatchedWord
                },
                onMarkUnremembered = {
                    when (selectedMode) {
                        MatchMode.CLASSIC -> breakthroughViewModel.markLatestWordUnremembered()
                        MatchMode.TIMED -> timeLimitViewModel.markLatestWordUnremembered()
                    }
                }
            )
        }

        when (selectedMode) {
            MatchMode.CLASSIC -> {
                if (!breakthroughUiState.hasStarted || !breakthroughUiState.gameVisible) {
                    EmptyStateCard(
                        title = if (breakthroughUiState.statusTitle == "Today's fixed set") {
                            selectedMode.emptyTitle
                        } else {
                            breakthroughUiState.statusTitle
                        },
                        message = if (breakthroughUiState.statusTitle == "Today's fixed set") {
                            selectedMode.emptyMessage
                        } else {
                            breakthroughUiState.statusMessage
                        }
                    )
                } else {
                    MatchGameContent(
                        uiState = breakthroughUiState,
                        onCardClick = breakthroughViewModel::onCardClicked,
                        onPronunciationClick = breakthroughViewModel::playAudioForWord
                    )
                }
            }
            MatchMode.TIMED -> {
                if (!timeLimitUiState.hasStarted || !timeLimitUiState.gameVisible) {
                    EmptyStateCard(
                        title = if (timeLimitUiState.statusTitle == "Today's fixed set") {
                            selectedMode.emptyTitle
                        } else {
                            timeLimitUiState.statusTitle
                        },
                        message = if (timeLimitUiState.statusTitle == "Today's fixed set") {
                            selectedMode.emptyMessage
                        } else {
                            timeLimitUiState.statusMessage
                        }
                    )
                } else {
                    MatchGameContent(
                        uiState = timeLimitUiState,
                        onCardClick = timeLimitViewModel::onCardClicked,
                        onPronunciationClick = timeLimitViewModel::playAudioForWord
                    )
                }
            }
        }
    }

    if (breakthroughUiState.gameOverMessage != null && breakthroughUiState.gameOverScore != null) {
        AlertDialog(
            onDismissRequest = {
                breakthroughViewModel.clearGameOverDialog()
            },
            title = { Text("Round complete") },
            text = { Text("${breakthroughUiState.gameOverMessage}\nScore: ${breakthroughUiState.gameOverScore}") },
            confirmButton = {
                Button(onClick = {
                    breakthroughViewModel.clearGameOverDialog()
                    breakthroughViewModel.restartGame()
                }) {
                    Text("Play again")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    breakthroughViewModel.clearGameOverDialog()
                }) {
                    Text("Close")
                }
            }
        )
    }

    if (timeLimitUiState.gameOverMessage != null && timeLimitUiState.gameOverScore != null) {
        AlertDialog(
            onDismissRequest = {
                timeLimitViewModel.clearGameOverDialog()
            },
            title = { Text("Time's up") },
            text = { Text("${timeLimitUiState.gameOverMessage}\nScore: ${timeLimitUiState.gameOverScore}") },
            confirmButton = {
                Button(onClick = {
                    timeLimitViewModel.clearGameOverDialog()
                    timeLimitViewModel.startGame()
                }) {
                    Text("Play again")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    timeLimitViewModel.clearGameOverDialog()
                }) {
                    Text("Close")
                }
            }
        )
    }

    if (showStopConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showStopConfirmDialog = false },
            title = { Text("End current game?") },
            text = { Text("Stopping now will save the current game record and end this round.") },
            confirmButton = {
                Button(onClick = {
                    showStopConfirmDialog = false
                    when (selectedMode) {
                        MatchMode.CLASSIC -> breakthroughViewModel.endGameEarly()
                        MatchMode.TIMED -> timeLimitViewModel.endGameEarly()
                    }
                }) {
                    Text("Stop")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MatchGameScaffold(
    title: String,
    subtitle: String,
    showHeader: Boolean,
    showStats: Boolean,
    score: Int,
    hearts: Int,
    extra: String?,
    onPrimaryAction: () -> Unit,
    primaryActionLabel: String,
    onStopAction: () -> Unit,
    stopVisible: Boolean,
    onSecondaryAction: () -> Unit,
    secondaryActionLabel: String,
    topControls: @Composable ColumnScope.() -> Unit = {},
    preActionContent: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val dims = appDimens()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = dims.pagePadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(dims.sectionSpacing)
    ) {
        if (showHeader) {
            ScreenHeader(title = title, subtitle = subtitle, subtitleMaxLines = 1)
        }
        topControls()
        preActionContent()
        if (showStats && extra == null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(dims.controlSpacing)
            ) {
                StatCard(
                    "Score",
                    score.toString(),
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                LivesStatCard(
                    hearts = hearts,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        } else if (showStats) {
            Column(verticalArrangement = Arrangement.spacedBy(dims.controlSpacing)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(dims.controlSpacing)
                ) {
                    StatCard(
                        "Score",
                        score.toString(),
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    StatCard(
                        "Timer",
                        extra ?: "",
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
                LivesStatCard(
                    hearts = hearts,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        GameActionRow(
            primaryLabel = primaryActionLabel,
            onPrimaryClick = onPrimaryAction,
            onStopClick = onStopAction,
            stopVisible = stopVisible,
            secondaryLabel = secondaryActionLabel,
            onSecondaryClick = onSecondaryAction
        )
        content()
        Spacer(modifier = Modifier.height(dims.scaled(24.dp)))
    }
}

@Composable
private fun LatestMatchedWordCard(
    word: com.example.wordcrush.data.model.WordItem?,
    onMarkUnremembered: () -> Unit
) {
    val dims = appDimens()
    if (word == null) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dims.pagePadding, vertical = dims.controlSpacing),
            horizontalArrangement = Arrangement.spacedBy(dims.controlSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(dims.tinySpacing)) {
                Text(
                    text = "Latest pair",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = word.english,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = word.chinese.replace("\n", " ").trim(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            androidx.compose.material3.OutlinedButton(onClick = onMarkUnremembered) {
                Text("Not remembered")
            }
        }
    }
}

@Composable
private fun LearnedWordsSummaryCard(
    summaries: List<String>
) {
    val dims = appDimens()
    if (summaries.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dims.pagePadding),
            verticalArrangement = Arrangement.spacedBy(dims.compactSpacing)
        ) {
            Text(
                text = "Learned this round",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            summaries.take(6).forEach { summary ->
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (summaries.size > 6) {
                Text(
                    text = "+${summaries.size - 6} more",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GameActionRow(
    primaryLabel: String,
    onPrimaryClick: () -> Unit,
    onStopClick: () -> Unit,
    stopVisible: Boolean,
    secondaryLabel: String,
    onSecondaryClick: () -> Unit
) {
    val dims = appDimens()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dims.controlSpacing)
    ) {
        Button(
            onClick = onPrimaryClick,
            modifier = Modifier
                .weight(1f)
                .height(dims.inputHeight)
        ) {
            Text(primaryLabel)
        }
        if (stopVisible) {
            androidx.compose.material3.OutlinedButton(
                onClick = onStopClick,
                modifier = Modifier
                    .weight(1f)
                    .height(dims.inputHeight)
            ) {
                Text("Stop")
            }
        }
        androidx.compose.material3.FilledTonalButton(
            onClick = onSecondaryClick,
            modifier = Modifier
                .weight(1f)
                .height(dims.inputHeight)
        ) {
            Text(secondaryLabel)
        }
    }
}

@Composable
private fun LivesStatCard(
    hearts: Int,
    modifier: Modifier = Modifier
) {
    val dims = appDimens()
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(dims.cardCorner),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dims.pagePadding),
            verticalArrangement = Arrangement.spacedBy(dims.compactSpacing)
        ) {
            Text(
                text = "Lives",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(dims.scaled(6.dp))) {
                repeat(5) { index ->
                    Image(
                        painter = painterResource(
                            id = if (index < hearts) {
                                R.drawable.icon_red_heart
                            } else {
                                R.drawable.icon_white_heart
                            }
                        ),
                        contentDescription = if (index < hearts) "Life remaining" else "Life lost",
                        modifier = Modifier.size(dims.heartSize)
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchGameContent(
    uiState: BreakthroughUiState,
    onCardClick: (Int) -> Unit,
    onPronunciationClick: (Int) -> Unit
) {
    when {
        uiState.isLoading -> LoadingSection()
        uiState.cards.isEmpty() -> EmptyStateCard(
            title = uiState.statusTitle,
            message = uiState.statusMessage
        )
        else -> MatchCardGrid(
            cards = uiState.cards,
            onCardClick = onCardClick,
            onPronunciationClick = onPronunciationClick
        )
    }
}

@Composable
private fun MatchGameContent(
    uiState: TimeLimitUiState,
    onCardClick: (Int) -> Unit,
    onPronunciationClick: (Int) -> Unit
) {
    when {
        uiState.isLoading -> LoadingSection()
        uiState.cards.isEmpty() -> EmptyStateCard(
            title = uiState.statusTitle,
            message = uiState.statusMessage
        )
        else -> MatchCardGrid(
            cards = uiState.cards,
            onCardClick = onCardClick,
            onPronunciationClick = onPronunciationClick
        )
    }
}

@Composable
private fun MatchCardGrid(
    cards: List<MatchCardUiModel>,
    onCardClick: (Int) -> Unit,
    onPronunciationClick: (Int) -> Unit
) {
    val dims = appDimens()
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = dims.staggeredGridMinCell),
        modifier = Modifier
            .fillMaxWidth()
            .height(dims.matchGridHeight),
        horizontalArrangement = Arrangement.spacedBy(dims.controlSpacing),
        verticalItemSpacing = dims.controlSpacing
    ) {
        itemsIndexed(cards, key = { _, item -> item.id }) { index, item ->
            val matchedAlpha by animateFloatAsState(
                targetValue = if (item.feedback == MatchCardFeedback.MATCHED) 0f else if (item.isMatched) 0f else 1f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                label = "matchedAlpha"
            )
            val matchedScale by animateFloatAsState(
                targetValue = if (item.feedback == MatchCardFeedback.MATCHED) 0.82f else 1f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                label = "matchedScale"
            )
            val flashProgress = remember(item.id) { Animatable(0f) }
            LaunchedEffect(item.feedback) {
                if (item.feedback == MatchCardFeedback.MISMATCH) {
                    flashProgress.snapTo(0f)
                    flashProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = keyframes {
                            durationMillis = 640
                            0f at 0
                            1f at 80
                            0f at 160
                            1f at 320
                            0f at 480
                            1f at 560
                            0f at 640
                        }
                    )
                } else {
                    flashProgress.snapTo(0f)
                }
            }
            val containerColor = when {
                flashProgress.value > 0f -> Color(
                    red = 0.20f + flashProgress.value * 0.18f,
                    green = 0.20f + flashProgress.value * 0.18f,
                    blue = 0.20f + flashProgress.value * 0.18f,
                    alpha = 0.26f + flashProgress.value * 0.44f
                )
                item.isMatched -> MaterialTheme.colorScheme.secondaryContainer
                item.isSelected && item.type == MatchCardType.ENGLISH -> Color(0xFF151515)
                item.isSelected && item.type == MatchCardType.CHINESE -> Color(0xFF3A3A3A)
                item.type == MatchCardType.ENGLISH -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
            val contentColor = when {
                item.isSelected -> Color(0xFFF7F7F5)
                else -> MaterialTheme.colorScheme.onSurface
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = dims.matchCardMinHeight)
                    .scale(matchedScale)
                    .alpha(matchedAlpha)
                    .clickable(enabled = !item.isMatched && item.feedback != MatchCardFeedback.MATCHED) {
                        onCardClick(index)
                    },
                colors = CardDefaults.cardColors(containerColor = containerColor),
                border = if (item.isSelected) BorderStroke(2.dp, Color(0xFFE8E8E3)) else null,
                shape = RoundedCornerShape(dims.cardCornerLarge)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = dims.matchCardMinHeight)
                        .padding(dims.pagePadding),
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = if (item.type == MatchCardType.ENGLISH) "EN" else "CN",
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.78f)
                    )
                    Spacer(modifier = Modifier.height(dims.compactSpacing))
                    if (item.type == MatchCardType.ENGLISH && !item.pronunciation.isNullOrBlank()) {
                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = contentColor
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = item.pronunciation,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.84f),
                            modifier = Modifier.clickable {
                                onPronunciationClick(item.wordId)
                            }
                        )
                    } else {
                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}
