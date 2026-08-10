package com.example.wordcrush.ui.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wordcrush.constants.AppConstants
import com.example.wordcrush.constants.AppStrings
import com.example.wordcrush.data.model.WordItem
import com.example.wordcrush.ui.compose.theme.WordCrushMatchChineseSelected
import com.example.wordcrush.ui.compose.theme.WordCrushMatchEnglishSelected
import com.example.wordcrush.ui.compose.theme.WordCrushMatchSelectedBorder
import com.example.wordcrush.ui.compose.theme.WordCrushMatchSelectedContent
import com.example.wordcrush.ui.compose.theme.matchCardFlashColor
import com.example.wordcrush.ui.model.MatchCardFeedback
import com.example.wordcrush.ui.model.MatchCardType
import com.example.wordcrush.ui.model.MatchCardUiModel
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
        extra = if (selectedMode == MatchMode.TIMED) {
            AppStrings.Game.timer(activeUiState.remainingSeconds)
        } else {
            null
        },
        onPrimaryAction = { viewModel.onAction(MatchAction.StartOrRestart) },
        primaryActionLabel = if (activeUiState.hasStarted) {
            AppStrings.Common.RESTART
        } else {
            AppStrings.Common.START
        },
        onStopAction = { viewModel.onAction(MatchAction.RequestStop) },
        stopVisible = isCurrentGameStarted,
        onSecondaryAction = { viewModel.onAction(MatchAction.OpenRanking) },
        secondaryActionLabel = AppStrings.Common.RANKING,
        topControls = {
            if (!isCurrentGameStarted) {
                MatchModeSelector(
                    selectedMode = selectedMode,
                    onModeSelected = { viewModel.onAction(MatchAction.ModeSelected(it)) }
                )
            }
        },
        preActionContent = {
            if (!isCurrentGameStarted) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = if (selectedMode == MatchMode.CLASSIC) {
                                AppStrings.Game.CLASSIC
                            } else {
                                AppStrings.Game.TIMED
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        ProgressSummary(
                            label = AppStrings.Game.todayProgress(
                                activeUiState.masteredTodayCount,
                                activeUiState.todayWordCount
                            ),
                            completed = activeUiState.masteredTodayCount,
                            total = activeUiState.todayWordCount
                        )
                    }
                }
                LearnedWordsSummaryCard(activeUiState.learnedWordSummaries)
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

        if (!activeUiState.hasStarted || !activeUiState.gameVisible) {
            EmptyStateCard(
                title = if (activeUiState.statusTitle == AppStrings.Learning.TODAY_FIXED_SET) {
                    selectedMode.emptyTitle
                } else {
                    activeUiState.statusTitle
                },
                message = if (activeUiState.statusTitle == AppStrings.Learning.TODAY_FIXED_SET) {
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

    if (activeUiState.gameOverMessage != null && activeUiState.gameOverScore != null) {
        GameOverDialog(
            title = if (selectedMode == MatchMode.CLASSIC) {
                AppStrings.Game.ROUND_COMPLETE
            } else {
                AppStrings.Game.TIMES_UP
            },
            message = AppStrings.scoreSummary(
                activeUiState.gameOverMessage,
                activeUiState.gameOverScore
            ),
            onPlayAgain = {
                viewModel.onAction(MatchAction.ClearGameOver)
                viewModel.onAction(MatchAction.StartOrRestart)
            },
            onClose = { viewModel.onAction(MatchAction.ClearGameOver) }
        )
    }

    if (uiState.showStopConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onAction(MatchAction.DismissStop) },
            title = { Text(AppStrings.Game.END_CURRENT_GAME) },
            text = { Text(AppStrings.Game.STOPPING_GAME_MESSAGE) },
            confirmButton = {
                Button(onClick = { viewModel.onAction(MatchAction.ConfirmStop) }) {
                    Text(AppStrings.Game.STOP)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onAction(MatchAction.DismissStop) }) {
                    Text(AppStrings.Common.CANCEL)
                }
            }
        )
    }
}

@Composable
private fun MatchModeSelector(
    selectedMode: MatchMode,
    onModeSelected: (MatchMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MatchMode.entries.forEach { mode ->
            val selected = mode == selectedMode
            if (selected) {
                Button(
                    onClick = { onModeSelected(mode) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                ) {
                    Text(if (mode == MatchMode.CLASSIC) AppStrings.Game.CLASSIC else AppStrings.Game.TIMED)
                }
            } else {
                OutlinedButton(
                    onClick = { onModeSelected(mode) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                ) {
                    Text(if (mode == MatchMode.CLASSIC) AppStrings.Game.CLASSIC else AppStrings.Game.TIMED)
                }
            }
        }
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dims.pagePadding),
        verticalArrangement = Arrangement.spacedBy(dims.sectionSpacing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = dims.contentMaxWidth)
                .align(Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(dims.sectionSpacing)
        ) {
            if (showHeader) {
                ScreenHeader(title = title, subtitle = subtitle, subtitleMaxLines = 2)
            }
            topControls()
            preActionContent()
            if (showStats) {
                GameStats(score = score, hearts = hearts, extra = extra)
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
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GameStats(
    score: Int,
    hearts: Int,
    extra: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = AppStrings.Common.SCORE,
                value = score.toString(),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            if (extra != null) {
                StatCard(
                    title = AppStrings.Common.TIMER,
                    value = extra,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            } else {
                LivesStatCard(
                    hearts = hearts,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
        if (extra != null) {
            LivesStatCard(hearts = hearts, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun LatestMatchedWordCard(
    word: WordItem?,
    onMarkUnremembered: () -> Unit
) {
    if (word == null) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = AppStrings.Game.LATEST_PAIR,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = word.english,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = word.chinese.replace("\n", " ").trim(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.82f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            OutlinedButton(onClick = onMarkUnremembered) {
                Text(AppStrings.Game.NOT_REMEMBERED)
            }
        }
    }
}

@Composable
private fun LearnedWordsSummaryCard(summaries: List<String>) {
    if (summaries.isEmpty()) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = AppStrings.Game.LEARNED_THIS_ROUND,
                style = MaterialTheme.typography.titleMedium
            )
            summaries.take(AppConstants.Game.MAX_LEARNED_SUMMARIES).forEach { summary ->
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (summaries.size > AppConstants.Game.MAX_LEARNED_SUMMARIES) {
                Text(
                    text = AppStrings.Game.moreSummaries(
                        summaries.size - AppConstants.Game.MAX_LEARNED_SUMMARIES
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onPrimaryClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
        ) {
            Text(primaryLabel)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (stopVisible) {
                OutlinedButton(
                    onClick = onStopClick,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                ) {
                    Text(AppStrings.Game.STOP)
                }
            }
            FilledTonalButton(
                onClick = onSecondaryClick,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Leaderboard,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(secondaryLabel)
            }
        }
    }
}

@Composable
private fun LivesStatCard(
    hearts: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = AppStrings.Game.LIVES,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.semantics {
                    contentDescription = "$hearts ${AppStrings.Game.LIFE_REMAINING}"
                },
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(AppConstants.Game.MAX_HEARTS) { index ->
                    Icon(
                        imageVector = if (index < hearts) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                        tint = if (index < hearts) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        },
                        modifier = Modifier.size(20.dp)
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
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = dims.gridMinCell),
        modifier = Modifier
            .fillMaxWidth()
            .height(dims.matchGridHeight),
        contentPadding = PaddingValues(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = cards, key = { it.id }) { item ->
            val index = cards.indexOf(item)
            val matchedAlpha by animateFloatAsState(
                targetValue = if (item.feedback == MatchCardFeedback.MATCHED || item.isMatched) 0f else 1f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                label = AppConstants.Animation.MATCHED_ALPHA_LABEL
            )
            val matchedScale by animateFloatAsState(
                targetValue = if (item.feedback == MatchCardFeedback.MATCHED) 0.82f else 1f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                label = AppConstants.Animation.MATCHED_SCALE_LABEL
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
                flashProgress.value > 0f -> matchCardFlashColor(flashProgress.value)
                item.isMatched -> MaterialTheme.colorScheme.secondaryContainer
                item.isSelected && item.type == MatchCardType.ENGLISH -> WordCrushMatchEnglishSelected
                item.isSelected && item.type == MatchCardType.CHINESE -> WordCrushMatchChineseSelected
                item.type == MatchCardType.ENGLISH -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.54f)
                else -> MaterialTheme.colorScheme.surface
            }
            val contentColor = when {
                item.isSelected -> WordCrushMatchSelectedContent
                else -> MaterialTheme.colorScheme.onSurface
            }
            val typeLabel = if (item.type == MatchCardType.ENGLISH) {
                AppStrings.Game.ENGLISH_LABEL
            } else {
                AppStrings.Game.CHINESE_LABEL
            }
            Card(
                onClick = { onCardClick(index) },
                enabled = !item.isMatched && item.feedback != MatchCardFeedback.MATCHED,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = dims.matchCardMinHeight)
                    .scale(matchedScale)
                    .alpha(matchedAlpha)
                    .semantics(mergeDescendants = true) {
                        contentDescription = "$typeLabel ${item.text}"
                    },
                colors = CardDefaults.cardColors(containerColor = containerColor),
                border = if (item.isSelected) {
                    androidx.compose.foundation.BorderStroke(2.dp, WordCrushMatchSelectedBorder)
                } else {
                    null
                },
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = dims.matchCardMinHeight)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.78f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = contentColor
                    )
                    if (item.type == MatchCardType.ENGLISH && !item.pronunciation.isNullOrBlank()) {
                        Spacer(modifier = Modifier.weight(1f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.pronunciation,
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.84f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            IconButton(
                                onClick = { onPronunciationClick(item.wordId) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = AppStrings.Accessibility.PLAY_UK,
                                    tint = contentColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameOverDialog(
    title: String,
    message: String,
    onPlayAgain: () -> Unit,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onPlayAgain) {
                Text(AppStrings.Common.PLAY_AGAIN)
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text(AppStrings.Common.CLOSE)
            }
        }
    )
}
