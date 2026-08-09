package com.example.wordcrush.ui.compose

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wordcrush.constants.AppConstants
import com.example.wordcrush.constants.AppStrings
import com.example.wordcrush.data.model.GameRecordItem
import com.example.wordcrush.data.model.RankingItem
import com.example.wordcrush.data.model.WordItem
import com.example.wordcrush.ui.viewmodel.GameRecordAction
import com.example.wordcrush.ui.viewmodel.GameRecordViewModel
import com.example.wordcrush.ui.viewmodel.GameRecordEffect
import com.example.wordcrush.ui.viewmodel.HomeAction
import com.example.wordcrush.ui.viewmodel.HomeEffect
import com.example.wordcrush.ui.viewmodel.HomeViewModel
import com.example.wordcrush.ui.viewmodel.RankingAction
import com.example.wordcrush.ui.viewmodel.RankingEffect
import com.example.wordcrush.ui.viewmodel.RankingViewModel
import com.example.wordcrush.ui.viewmodel.WordBookAction
import com.example.wordcrush.ui.viewmodel.WordBookEffect
import com.example.wordcrush.ui.viewmodel.WordBookViewModel
import com.example.wordcrush.ui.viewmodel.WordFilter

@Composable
internal fun WordBookRoute(
    onPlayAudio: suspend (String, Int) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val dims = appDimens()
    val viewModel: WordBookViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WordBookEffect.PlayAudio -> onPlayAudio(effect.word, effect.type)
            }
        }
    }

    LaunchedEffect(listState, uiState.words.size, uiState.canLoadMore, uiState.isLoading, uiState.isAppending) {
        snapshotFlow {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val totalCount = listState.layoutInfo.totalItemsCount
            lastVisibleIndex to totalCount
        }.collect { (lastVisibleIndex, totalCount) ->
            if (!uiState.isLoading &&
                !uiState.isAppending &&
                uiState.canLoadMore &&
                totalCount > 0 &&
                lastVisibleIndex >= totalCount - AppConstants.WordBook.LOAD_MORE_THRESHOLD
            ) {
                viewModel.onAction(WordBookAction.LoadMore)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = dims.pagePadding)
            .fillMaxWidth(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(dims.sectionSpacing)
    ) {
        item {
            ScreenHeader(
                title = AppStrings.WordBook.TITLE,
                subtitle = AppStrings.WordBook.SUBTITLE
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dims.controlSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = { viewModel.onAction(WordBookAction.QueryChanged(it)) },
                    modifier = Modifier.weight(1f),
                    label = { Text(AppStrings.WordBook.SEARCH_WORDS) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { viewModel.onAction(WordBookAction.ApplySearch) },
                        modifier = Modifier.height(dims.inputHeight)
                    ) {
                        Text(AppStrings.Common.SEARCH)
                    }
                }
            }
        }
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(dims.chipSpacing),
                verticalArrangement = Arrangement.spacedBy(dims.chipSpacing)
            ) {
                WordFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = uiState.filter == filter,
                        onClick = { viewModel.onAction(WordBookAction.FilterChanged(filter)) },
                        label = { Text(filter.toDisplayName()) }
                    )
                }
            }
        }
        when {
            uiState.isLoading -> item {
                LoadingSection()
            }
            uiState.words.isEmpty() -> item {
                EmptyStateCard(
                    title = uiState.emptyStateTitle,
                    message = uiState.emptyStateMessage
                )
            }
            else -> items(
                items = uiState.words,
                key = { word -> word.id }
            ) { word ->
                WordItemCard(
                    word = word,
                    onPlayUk = { viewModel.onAction(WordBookAction.PlayAudio(word, 1)) },
                    onPlayUs = { viewModel.onAction(WordBookAction.PlayAudio(word, 2)) },
                    onMasteryChanged = { isMastered ->
                        viewModel.onAction(WordBookAction.MasteryChanged(word, isMastered))
                    }
                )
            }
        }
        item {
            if (uiState.isAppending) {
                LoadingSection()
            } else {
                Spacer(modifier = Modifier.height(dims.scaled(24.dp)))
            }
        }
        item {
            Spacer(modifier = Modifier.height(dims.scaled(24.dp)))
        }
    }
}

@Composable
internal fun HomeRoute(
    onOpenRecords: () -> Unit,
    onLogout: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val dims = appDimens()
    val viewModel: HomeViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            viewModel.onAction(HomeAction.UploadAvatar(uri))
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowMessage -> onShowMessage(effect.message)
                HomeEffect.NavigateToLogin -> onLogout()
                HomeEffect.OpenRecords -> onOpenRecords()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = dims.pagePadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(dims.sectionSpacing)
    ) {
        ScreenHeader(
            title = AppStrings.Profile.TITLE,
            subtitle = AppStrings.Profile.SUBTITLE
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dims.cardPaddingLarge),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    imageUrl = uiState.avatarUrl,
                    fallbackLabel = uiState.username.ifBlank { AppStrings.Common.GUEST }.take(1).uppercase(),
                    size = dims.avatarSize
                )
                Spacer(modifier = Modifier.width(dims.pagePadding))
                Column(verticalArrangement = Arrangement.spacedBy(dims.tinySpacing)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dims.controlSpacing)
                    ) {
                        Text(
                            text = uiState.username.ifBlank { AppStrings.Common.GUEST },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedButton(
                            onClick = { avatarPicker.launch(AppStrings.Profile.IMAGE_CONTENT_TYPE) },
                            enabled = !uiState.isUploadingAvatar,
                            modifier = Modifier.height(dims.scaled(36.dp))
                        ) {
                            Text(
                                if (uiState.isUploadingAvatar) {
                                    AppStrings.Profile.UPLOADING
                                } else {
                                    AppStrings.Profile.UPLOAD
                                }
                            )
                        }
                    }
                    Text(
                        text = AppStrings.Profile.LEARNING_PROFILE,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dims.cardPadding),
                verticalArrangement = Arrangement.spacedBy(dims.compactSpacing)
            ) {
                Text(
                    text = AppStrings.Profile.SCORE_SUMMARY,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = AppStrings.Profile.matchScore(uiState.breakthroughScore),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = AppStrings.Profile.timedScore(uiState.timeLimitScore),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
                verticalArrangement = Arrangement.spacedBy(dims.controlSpacing)
            ) {
                Text(
                    text = AppStrings.Profile.DAILY_PLAN,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = when {
                        uiState.allWordsMastered -> AppStrings.Profile.ALL_WORDS_LEARNED
                        uiState.dailyCompleted && uiState.canIncreaseDailyTarget ->
                            AppStrings.Profile.DAILY_WORDS_DONE
                        uiState.dailyCompleted ->
                            AppStrings.Profile.FIXED_SET_COMPLETE
                        else ->
                            AppStrings.Profile.fixedSetProgress(
                                uiState.completedTodayCount,
                                uiState.todayWordCount
                            )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (uiState.pendingLearningMutations == 0) {
                        AppStrings.Profile.LEARNING_PROGRESS_SYNCED
                    } else {
                        AppStrings.Profile.pendingLearning(uiState.pendingLearningMutations)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dims.controlSpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.dailyTargetInput,
                        onValueChange = { viewModel.onAction(HomeAction.DailyTargetChanged(it)) },
                        modifier = Modifier.weight(1f),
                        label = { Text(AppStrings.Profile.DAILY_WORD_COUNT) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number
                        )
                    )
                    Button(
                        onClick = { viewModel.onAction(HomeAction.SaveDailyTarget) },
                        modifier = Modifier.height(dims.inputHeight)
                    ) {
                        Text(AppStrings.Common.SAVE)
                    }
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dims.cardPadding),
                verticalArrangement = Arrangement.spacedBy(dims.controlSpacing)
            ) {
                Text(
                    text = AppStrings.Profile.QUICK_ACTIONS,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Button(
                    onClick = { viewModel.onAction(HomeAction.OpenRecords) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dims.inputHeight)
                ) {
                    Text(AppStrings.Common.RECORDS)
                }
                FilledTonalButton(
                    onClick = { viewModel.onAction(HomeAction.SyncCloudData) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dims.inputHeight)
                ) {
                    Text(AppStrings.Profile.SYNC_CLOUD_DATA)
                }
                OutlinedButton(
                    onClick = { viewModel.onAction(HomeAction.Refresh) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dims.inputHeight)
                ) {
                    Text(AppStrings.Profile.REFRESH_SCORES)
                }
                OutlinedButton(
                    onClick = { viewModel.onAction(HomeAction.ShowPasswordDialog) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dims.inputHeight)
                ) {
                    Text(AppStrings.Profile.CHANGE_PASSWORD)
                }
            }
        }
        OutlinedButton(
            onClick = { viewModel.onAction(HomeAction.Logout) },
            modifier = Modifier
                .fillMaxWidth()
                .height(dims.inputHeight)
        ) {
            Text(AppStrings.Profile.LOG_OUT)
        }
        Spacer(modifier = Modifier.height(dims.scaled(24.dp)))
    }

    if (uiState.showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onAction(HomeAction.DismissPasswordDialog) },
            title = { Text(AppStrings.Profile.CHANGE_PASSWORD) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = uiState.oldPassword,
                        onValueChange = { viewModel.onAction(HomeAction.OldPasswordChanged(it)) },
                        label = { Text(AppStrings.Profile.CURRENT_PASSWORD) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = uiState.newPassword,
                        onValueChange = { viewModel.onAction(HomeAction.NewPasswordChanged(it)) },
                        label = { Text(AppStrings.Profile.NEW_PASSWORD) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = uiState.confirmPassword,
                        onValueChange = { viewModel.onAction(HomeAction.ConfirmPasswordChanged(it)) },
                        label = { Text(AppStrings.Profile.CONFIRM_NEW_PASSWORD) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.onAction(HomeAction.SubmitPasswordChange) }) {
                    Text(AppStrings.Common.UPDATE)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onAction(HomeAction.DismissPasswordDialog) }) {
                    Text(AppStrings.Common.CANCEL)
                }
            }
        )
    }
}

@Composable
internal fun RankingRoute(
    gameType: Int,
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val viewModel: RankingViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(gameType) {
        viewModel.onAction(RankingAction.Load(gameType))
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RankingEffect.ShowMessage -> onShowMessage(effect.message)
            }
        }
    }

    ScreenScaffold(
        title = AppStrings.Ranking.title(gameType == 0),
        onBack = onBack,
        scrollable = false
    ) {
        when {
            uiState.isLoading -> LoadingSection()
            uiState.rankings.isEmpty() -> EmptyStateCard(
                title = AppStrings.Ranking.NO_DATA,
                message = AppStrings.Ranking.EMPTY_MESSAGE
            )
            else -> RankingContent(
                rankings = uiState.rankings,
                gameType = gameType
            )
        }
    }
}

@Composable
internal fun GameRecordRoute(
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val viewModel: GameRecordViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onAction(GameRecordAction.Load)
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is GameRecordEffect.ShowMessage -> onShowMessage(effect.message)
            }
        }
    }

    ScreenScaffold(
        title = AppStrings.Records.TITLE,
        onBack = onBack,
        scrollable = false
    ) {
        when {
            uiState.isLoading -> LoadingSection()
            uiState.records.isEmpty() -> EmptyStateCard(
                title = AppStrings.Records.NO_RECORDS,
                message = AppStrings.Records.EMPTY_MESSAGE
            )
            else -> GameRecordContent(
                records = uiState.records,
                expandedRecordId = uiState.expandedRecordId,
                onToggleExpanded = { recordId ->
                    viewModel.onAction(GameRecordAction.ToggleExpanded(recordId))
                },
                onDelete = { record ->
                    viewModel.onAction(GameRecordAction.RequestDelete(record))
                }
            )
        }
    }

    if (uiState.pendingDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onAction(GameRecordAction.DismissDelete) },
            title = { Text(AppStrings.Records.DELETE_TITLE) },
            text = { Text(AppStrings.Records.DELETE_MESSAGE) },
            confirmButton = {
                Button(onClick = {
                    viewModel.onAction(GameRecordAction.ConfirmDelete)
                }) {
                    Text(AppStrings.Common.DELETE)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onAction(GameRecordAction.DismissDelete) }) {
                    Text(AppStrings.Common.CANCEL)
                }
            }
        )
    }
}

@Composable
private fun WordItemCard(
    word: WordItem,
    onPlayUk: () -> Unit,
    onPlayUs: () -> Unit,
    onMasteryChanged: (Boolean) -> Unit
) {
    val dims = appDimens()
    val statusLabel = if (word.isMastered) AppStrings.WordBook.REMEMBERED else AppStrings.Common.LEARNING

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dims.cardPadding),
            verticalArrangement = Arrangement.spacedBy(dims.compactSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = dims.controlSpacing),
                    verticalArrangement = Arrangement.spacedBy(dims.compactSpacing)
                ) {
                    Text(
                        text = word.english,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = word.pronunciation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(text = word.chinese, style = MaterialTheme.typography.bodyMedium)
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(dims.scaled(10.dp))
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(dims.chipSpacing)) {
                        AssistChip(onClick = onPlayUk, label = { Text(AppStrings.Common.UK) })
                        AssistChip(onClick = onPlayUs, label = { Text(AppStrings.Common.US) })
                    }
                    if (word.isMastered) {
                        OutlinedButton(
                            onClick = { onMasteryChanged(false) },
                            modifier = Modifier.height(dims.inputHeight)
                        ) {
                            Text(AppStrings.Common.RESET)
                        }
                    } else {
                        FilledTonalButton(
                            onClick = { onMasteryChanged(true) },
                            modifier = Modifier.height(dims.inputHeight)
                        ) {
                            Text(AppStrings.Common.MARK)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RankingContent(
    rankings: List<RankingItem>,
    gameType: Int
) {
    val dims = appDimens()
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(dims.sectionSpacing)
    ) {
        RankingOverviewCard(rankings = rankings, gameType = gameType)
        RankingList(rankings = rankings, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun RankingOverviewCard(
    rankings: List<RankingItem>,
    gameType: Int
) {
    val dims = appDimens()
    val topScore = rankings.maxOfOrNull { it.score } ?: 0
    val title = AppStrings.Records.modeTitle(gameType == 0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dims.cardPaddingLarge),
            verticalArrangement = Arrangement.spacedBy(dims.controlSpacing)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = AppStrings.Ranking.TOP_PLAYERS_DESCRIPTION,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dims.controlSpacing)
            ) {
                StatCard(
                    title = AppStrings.Ranking.PLAYERS,
                    value = rankings.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = AppStrings.Ranking.TOP_SCORE,
                    value = topScore.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RankingList(
    rankings: List<RankingItem>,
    modifier: Modifier = Modifier
) {
    val dims = appDimens()
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dims.controlSpacing)
    ) {
        items(
            count = rankings.size,
            key = { index ->
                val item = rankings[index]
                "${item.username}-${item.time}-${item.avatarVersion}-$index"
            }
        ) { index ->
            val item = rankings[index]
            if (index < AppConstants.Ranking.HIGHLIGHT_COUNT) {
                RankingHighlightCard(index = index, item = item)
            } else {
                RankingStandardCard(index = index, item = item)
            }
        }
    }
}

@Composable
private fun RankingHighlightCard(
    index: Int,
    item: RankingItem
) {
    val dims = appDimens()
    val containerColor = when (index) {
        0 -> MaterialTheme.colorScheme.primaryContainer
        1 -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val onContainerColor = when (index) {
        0 -> MaterialTheme.colorScheme.onPrimaryContainer
        1 -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dims.cardPaddingLarge),
            verticalArrangement = Arrangement.spacedBy(dims.controlSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.Ranking.rank(index),
                    style = MaterialTheme.typography.labelLarge,
                    color = onContainerColor.copy(alpha = 0.8f)
                )
                Text(
                    text = item.score.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = onContainerColor
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    imageUrl = item.avatarUrl,
                    fallbackLabel = item.username.take(1).uppercase(),
                    size = dims.avatarSize
                )
                Spacer(modifier = Modifier.width(dims.controlSpacing))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.username,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = onContainerColor
                    )
                    Text(
                        text = item.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = onContainerColor.copy(alpha = 0.78f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RankingStandardCard(
    index: Int,
    item: RankingItem
) {
    val dims = appDimens()
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dims.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = AppStrings.Ranking.compactRank(index),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = AppStrings.Common.SCORE,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(dims.controlSpacing))
            UserAvatar(
                imageUrl = item.avatarUrl,
                fallbackLabel = item.username.take(1).uppercase(),
                size = dims.smallAvatarSize
            )
            Spacer(modifier = Modifier.width(dims.controlSpacing))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = item.score.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GameRecordContent(
    records: List<GameRecordItem>,
    expandedRecordId: Int?,
    onToggleExpanded: (Int) -> Unit,
    onDelete: (GameRecordItem) -> Unit
) {
    val dims = appDimens()
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(dims.sectionSpacing)
    ) {
        RecordOverviewCard(records)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(dims.controlSpacing)
        ) {
            items(
                count = records.size,
                key = { index -> records[index].id }
            ) { index ->
                val record = records[index]
                RecordCard(
                    record = record,
                    expanded = expandedRecordId == record.id,
                    onToggleExpanded = { onToggleExpanded(record.id) },
                    onDelete = { onDelete(record) }
                )
            }
        }
    }
}

@Composable
private fun RecordOverviewCard(records: List<GameRecordItem>) {
    val dims = appDimens()
    val bestScore = records.maxOfOrNull { it.score } ?: 0
    val learnedWordTotal = records.sumOf { it.wordProgress.size }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dims.cardPaddingLarge),
            verticalArrangement = Arrangement.spacedBy(dims.controlSpacing)
        ) {
            Text(
                text = AppStrings.Records.YOUR_RECENT_RUNS,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = AppStrings.Records.RECENT_RUNS_DESCRIPTION,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dims.controlSpacing)
            ) {
                StatCard(
                    title = AppStrings.Common.RECORDS,
                    value = records.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = AppStrings.Records.BEST_SCORE,
                    value = bestScore.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = AppStrings.Common.WORDS,
                    value = learnedWordTotal.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RecordCard(
    record: GameRecordItem,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onDelete: () -> Unit
) {
    val dims = appDimens()
    val containerColor = if (record.gameType == 0) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dims.cardPadding),
            verticalArrangement = Arrangement.spacedBy(dims.controlSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(dims.tinySpacing)
                ) {
                    Text(
                        text = record.gameTypeLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = record.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = record.score.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = AppStrings.Common.SCORE,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(dims.chipSpacing),
                verticalArrangement = Arrangement.spacedBy(dims.chipSpacing)
            ) {
                AssistChip(
                    onClick = { },
                    enabled = false,
                    label = { Text(record.gameTypeLabel) }
                )
                AssistChip(
                    onClick = { },
                    enabled = false,
                    label = { Text(AppStrings.WordBook.matched(record.wordProgress.size)) }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(dims.controlSpacing)) {
                FilledTonalButton(onClick = onToggleExpanded) {
                    Text(
                        if (expanded) AppStrings.Records.HIDE_DETAILS else AppStrings.Records.VIEW_DETAILS
                    )
                }
                TextButton(onClick = onDelete) {
                    Text(AppStrings.Common.DELETE)
                }
            }
            if (expanded) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dims.cardPadding),
                        verticalArrangement = Arrangement.spacedBy(dims.compactSpacing)
                    ) {
                        Text(
                            text = AppStrings.Records.LEARNED_WORDS,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        HorizontalDivider()
                        if (record.wordProgress.isEmpty()) {
                            Text(
                                text = AppStrings.Records.NO_LEARNED_WORDS,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(dims.scaled(6.dp))) {
                                record.wordProgress.forEachIndexed { index, progress ->
                                    Text(
                                        text = AppStrings.WordBook.numbered(index, progress.displayLabel),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun WordFilter.toDisplayName(): String {
    return when (this) {
        WordFilter.ALL -> AppStrings.Common.ALL
        WordFilter.MASTERED -> AppStrings.Common.MASTERED
        WordFilter.UNMASTERED -> AppStrings.Common.LEARNING
    }
}













