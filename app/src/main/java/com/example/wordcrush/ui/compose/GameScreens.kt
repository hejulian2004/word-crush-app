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
import com.example.wordcrush.ui.model.MatchCardType
import com.example.wordcrush.ui.model.MatchCardUiModel
import com.example.wordcrush.ui.model.MatchCardFeedback
import com.example.wordcrush.ui.model.MatchMode
import com.example.wordcrush.ui.viewmodel.MatchAction
import com.example.wordcrush.ui.viewmodel.MatchEffect
import com.example.wordcrush.ui.viewmodel.MatchSessionUiState
import com.example.wordcrush.ui.viewmodel.MatchViewModel

@Composable
internal fun MatchRoute(
    onPlayAudio: suspend (String, Int) -> Unit,
    onShowMessage: (String) -> Unit,
    onOpenRanking: (Int) -> Unit
) {
    val dims = appDimens()
    val viewModel: MatchViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedMode = uiState.selectedMode
    val activeUiState = uiState.session(selectedMode)
    val isCurrentGameStarted = activeUiState.hasStarted

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MatchEffect.ShowMessage -> onShowMessage(effect.message)
                is MatchEffect.PlayAudio -> onPlayAudio(effect.word, effect.type)
                is MatchEffect.OpenRanking -> onOpenRanking(effect.gameType)
            }
        }
    }

    MatchGameScaffold(
        title = selectedMode.title,
        subtitle = selectedMode.subtitle,
        showHeader = !isCurrentGameStarted,
        showStats = isCurrentGameStarted,
        score = activeUiState.score,
        hearts = activeUiState.hearts,
        extra = if (selectedMode == MatchMode.TIMED) "${activeUiState.remainingSeconds}s" else null,
        onPrimaryAction = { viewModel.onAction(MatchAction.StartOrRestart) },
        primaryActionLabel = when (selectedMode) {
            MatchMode.CLASSIC -> if (activeUiState.hasStarted) "Restart" else "Start"
            MatchMode.TIMED -> if (activeUiState.hasStarted) "Restart" else "Start"
        },
        onStopAction = { viewModel.onAction(MatchAction.RequestStop) },
        stopVisible = isCurrentGameStarted,
        onSecondaryAction = { viewModel.onAction(MatchAction.OpenRanking) },
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
                                onClick = { viewModel.onAction(MatchAction.ModeSelected(mode)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(dims.buttonHeight)
                            ) {
                                Text(if (mode == MatchMode.CLASSIC) "Classic" else "Timed")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.onAction(MatchAction.ModeSelected(mode)) },
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
                                "Today: ${activeUiState.masteredTodayCount}/${activeUiState.todayWordCount}"
                            } else {
                                "Today: ${activeUiState.masteredTodayCount}/${activeUiState.todayWordCount}"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                LearnedWordsSummaryCard(
                    summaries = if (selectedMode == MatchMode.CLASSIC) {
                        activeUiState.learnedWordSummaries
                    } else {
                        activeUiState.learnedWordSummaries
                    }
                )
            }
        }
    ) {
        if (isCurrentGameStarted) {
            LatestMatchedWordCard(
                word = activeUiState.latestMatchedWord,
                onMarkUnremembered = {
                    viewModel.onAction(MatchAction.MarkLatestWordUnremembered)
                }
            )
        }

        when (selectedMode) {
            MatchMode.CLASSIC -> {
                if (!activeUiState.hasStarted || !activeUiState.gameVisible) {
                    EmptyStateCard(
                        title = if (activeUiState.statusTitle == "Today's fixed set") {
                            selectedMode.emptyTitle
                        } else {
                            activeUiState.statusTitle
                        },
                        message = if (activeUiState.statusTitle == "Today's fixed set") {
                            selectedMode.emptyMessage
                        } else {
                            activeUiState.statusMessage
                        }
                    )
                } else {
                    MatchGameContent(
                        uiState = activeUiState,
                        onCardClick = { viewModel.onAction(MatchAction.CardClicked(it)) },
                        onPronunciationClick = { viewModel.onAction(MatchAction.PlayAudio(it)) }
                    )
                }
            }
            MatchMode.TIMED -> {
                if (!activeUiState.hasStarted || !activeUiState.gameVisible) {
                    EmptyStateCard(
                        title = if (activeUiState.statusTitle == "Today's fixed set") {
                            selectedMode.emptyTitle
                        } else {
                            activeUiState.statusTitle
                        },
                        message = if (activeUiState.statusTitle == "Today's fixed set") {
                            selectedMode.emptyMessage
                        } else {
                            activeUiState.statusMessage
                        }
                    )
                } else {
                    MatchGameContent(
                        uiState = activeUiState,
                        onCardClick = { viewModel.onAction(MatchAction.CardClicked(it)) },
                        onPronunciationClick = { viewModel.onAction(MatchAction.PlayAudio(it)) }
                    )
                }
            }
        }
    }

    if (selectedMode == MatchMode.CLASSIC && activeUiState.gameOverMessage != null && activeUiState.gameOverScore != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.onAction(MatchAction.ClearGameOver)
            },
            title = { Text("Round complete") },
            text = { Text("${activeUiState.gameOverMessage}\nScore: ${activeUiState.gameOverScore}") },
            confirmButton = {
                Button(onClick = {
                    viewModel.onAction(MatchAction.ClearGameOver)
                    viewModel.onAction(MatchAction.StartOrRestart)
                }) {
                    Text("Play again")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.onAction(MatchAction.ClearGameOver)
                }) {
                    Text("Close")
                }
            }
        )
    }

    if (selectedMode == MatchMode.TIMED && activeUiState.gameOverMessage != null && activeUiState.gameOverScore != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.onAction(MatchAction.ClearGameOver)
            },
            title = { Text("Time's up") },
            text = { Text("${activeUiState.gameOverMessage}\nScore: ${activeUiState.gameOverScore}") },
            confirmButton = {
                Button(onClick = {
                    viewModel.onAction(MatchAction.ClearGameOver)
                    viewModel.onAction(MatchAction.StartOrRestart)
                }) {
                    Text("Play again")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.onAction(MatchAction.ClearGameOver)
                }) {
                    Text("Close")
                }
            }
        )
    }

    if (uiState.showStopConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onAction(MatchAction.DismissStop) },
            title = { Text("End current game?") },
            text = { Text("Stopping now will save the current game record and end this round.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.onAction(MatchAction.ConfirmStop)
                }) {
                    Text("Stop")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onAction(MatchAction.DismissStop) }) {
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
    uiState: MatchSessionUiState,
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
