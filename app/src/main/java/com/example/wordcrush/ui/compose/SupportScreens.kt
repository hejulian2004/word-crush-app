package com.example.wordcrush.ui.compose

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wordcrush.constants.AppConstants
import com.example.wordcrush.constants.AppStrings
import com.example.wordcrush.data.model.GameRecordItem
import com.example.wordcrush.data.model.RankingItem
import com.example.wordcrush.data.model.WordItem
import com.example.wordcrush.ui.viewmodel.GameRecordAction
import com.example.wordcrush.ui.viewmodel.GameRecordEffect
import com.example.wordcrush.ui.viewmodel.GameRecordViewModel
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

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = dims.contentMaxWidth)
                .align(Alignment.TopCenter)
                .background(MaterialTheme.colorScheme.background)
                .imePadding(),
            state = listState,
            contentPadding = PaddingValues(horizontal = dims.pagePadding, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(dims.sectionSpacing)
        ) {
            item {
                ScreenHeader(
                    title = AppStrings.WordBook.TITLE,
                    subtitle = AppStrings.WordBook.SUBTITLE
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = { viewModel.onAction(WordBookAction.QueryChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(AppStrings.WordBook.SEARCH_WORDS) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = AppStrings.Accessibility.SEARCH)
                    },
                    trailingIcon = {
                        if (uiState.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onAction(WordBookAction.QueryChanged("")) }) {
                                Text(AppStrings.Common.CLEAR)
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { viewModel.onAction(WordBookAction.ApplySearch) }
                    )
                )
            }
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
            if (uiState.error != null) {
                item {
                    EmptyStateCard(
                        title = AppStrings.Errors.LOAD_WORDS_TITLE,
                        message = uiState.error ?: AppStrings.Errors.LOAD_WORDS_FAILED,
                        actionLabel = AppStrings.Common.RETRY,
                        onAction = { viewModel.onAction(WordBookAction.Retry) },
                        isError = true
                    )
                }
            } else {
                when {
                    uiState.isLoading -> item { LoadingSection() }
                    uiState.words.isEmpty() -> item {
                        EmptyStateCard(
                            title = uiState.emptyStateTitle,
                            message = uiState.emptyStateMessage
                        )
                    }
                    dims.isCompact -> items(
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
                    else -> {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                uiState.words.chunked(2).forEach { rowWords ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowWords.forEach { word ->
                                            WordItemCard(
                                                modifier = Modifier.weight(1f),
                                                word = word,
                                                onPlayUk = { viewModel.onAction(WordBookAction.PlayAudio(word, 1)) },
                                                onPlayUs = { viewModel.onAction(WordBookAction.PlayAudio(word, 2)) },
                                                onMasteryChanged = { isMastered ->
                                                    viewModel.onAction(WordBookAction.MasteryChanged(word, isMastered))
                                                }
                                            )
                                        }
                                        if (rowWords.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                if (uiState.isAppending) LoadingSection() else Spacer(modifier = Modifier.height(24.dp))
            }
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
        if (uri != null) viewModel.onAction(HomeAction.UploadAvatar(uri))
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = dims.contentMaxWidth)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = dims.pagePadding, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(dims.sectionSpacing)
        ) {
            ScreenHeader(
                title = AppStrings.Profile.TITLE,
                subtitle = AppStrings.Profile.SUBTITLE
            )
            if (uiState.isRefreshing) {
                androidx.compose.material3.LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            uiState.error?.let { error ->
                InlineNotice(
                    message = error,
                    isError = true,
                    actionLabel = AppStrings.Common.RETRY,
                    onAction = { viewModel.onAction(HomeAction.Refresh) }
                )
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UserAvatar(
                        imageUrl = uiState.avatarUrl,
                        fallbackLabel = uiState.username.ifBlank { AppStrings.Common.GUEST }
                            .take(1).uppercase(),
                        size = dims.avatarSize
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.username.ifBlank { AppStrings.Common.GUEST },
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = AppStrings.Profile.LEARNING_PROFILE,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { avatarPicker.launch(AppStrings.Profile.IMAGE_CONTENT_TYPE) },
                        enabled = !uiState.isUploadingAvatar
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = AppStrings.Accessibility.UPLOAD_AVATAR
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = AppStrings.Game.MATCH,
                    value = uiState.breakthroughScore.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = AppStrings.Game.TIMED,
                    value = uiState.timeLimitScore.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(AppStrings.Profile.DAILY_PLAN, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = when {
                            uiState.allWordsMastered -> AppStrings.Profile.ALL_WORDS_LEARNED
                            uiState.dailyCompleted && uiState.canIncreaseDailyTarget ->
                                AppStrings.Profile.DAILY_WORDS_DONE
                            uiState.dailyCompleted -> AppStrings.Profile.FIXED_SET_COMPLETE
                            else -> AppStrings.Profile.fixedSetProgress(
                                uiState.completedTodayCount,
                                uiState.todayWordCount
                            )
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ProgressSummary(
                        label = AppStrings.Learning.TODAY_FIXED_SET,
                        completed = uiState.completedTodayCount,
                        total = uiState.todayWordCount
                    )
                    if (uiState.pendingLearningMutations == 0) {
                        SyncStatusPill(synced = true)
                    } else {
                        SyncStatusPill(synced = false, count = uiState.pendingLearningMutations)
                    }
                    OutlinedTextField(
                        value = uiState.dailyTargetInput,
                        onValueChange = { viewModel.onAction(HomeAction.DailyTargetChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(AppStrings.Profile.DAILY_WORD_COUNT) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = androidx.compose.ui.text.input.ImeAction.Done
                        )
                    )
                    Button(
                        onClick = { viewModel.onAction(HomeAction.SaveDailyTarget) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text(AppStrings.Common.SAVE)
                    }
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(AppStrings.Profile.QUICK_ACTIONS, style = MaterialTheme.typography.titleMedium)
                    ActionListRow(
                        icon = Icons.Filled.History,
                        label = AppStrings.Common.RECORDS,
                        onClick = { viewModel.onAction(HomeAction.OpenRecords) }
                    )
                    ActionListRow(
                        icon = Icons.Filled.Sync,
                        label = AppStrings.Profile.SYNC_CLOUD_DATA,
                        onClick = { viewModel.onAction(HomeAction.SyncCloudData) },
                        enabled = !uiState.isLoading
                    )
                    ActionListRow(
                        icon = Icons.Filled.Refresh,
                        label = AppStrings.Profile.REFRESH_SCORES,
                        onClick = { viewModel.onAction(HomeAction.Refresh) },
                        enabled = !uiState.isRefreshing
                    )
                    ActionListRow(
                        icon = Icons.Filled.Lock,
                        label = AppStrings.Profile.CHANGE_PASSWORD,
                        onClick = { viewModel.onAction(HomeAction.ShowPasswordDialog) }
                    )
                }
            }
            OutlinedButton(
                onClick = { viewModel.onAction(HomeAction.Logout) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(AppStrings.Profile.LOG_OUT)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (uiState.showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onAction(HomeAction.DismissPasswordDialog) },
            title = { Text(AppStrings.Profile.CHANGE_PASSWORD) },
            text = {
                Column(
                    modifier = Modifier.imePadding(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PasswordInput(
                        value = uiState.oldPassword,
                        label = AppStrings.Profile.CURRENT_PASSWORD,
                        onValueChange = { viewModel.onAction(HomeAction.OldPasswordChanged(it)) }
                    )
                    PasswordInput(
                        value = uiState.newPassword,
                        label = AppStrings.Profile.NEW_PASSWORD,
                        onValueChange = { viewModel.onAction(HomeAction.NewPasswordChanged(it)) }
                    )
                    PasswordInput(
                        value = uiState.confirmPassword,
                        label = AppStrings.Profile.CONFIRM_NEW_PASSWORD,
                        onValueChange = { viewModel.onAction(HomeAction.ConfirmPasswordChanged(it)) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onAction(HomeAction.SubmitPasswordChange) },
                    enabled = !uiState.isLoading
                ) {
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
private fun SyncStatusPill(synced: Boolean, count: Int = 0) {
    val label = if (synced) AppStrings.Common.SYNCED else AppStrings.Profile.pendingLearning(count)
    val icon = if (synced) Icons.Filled.CloudDone else Icons.Filled.CloudOff
    val color = if (synced) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.tertiaryContainer
    }
    Surface(color = color, shape = RoundedCornerShape(10.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ActionListRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        contentPadding = PaddingValues(horizontal = 14.dp)
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f))
        Icon(Icons.Outlined.ChevronRight, contentDescription = null)
    }
}

@Composable
private fun PasswordInput(
    value: String,
    label: String,
    onValueChange: (String) -> Unit
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        keyboardActions = KeyboardActions.Default,
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (visible) {
                        AppStrings.Accessibility.HIDE_PASSWORD
                    } else {
                        AppStrings.Accessibility.SHOW_PASSWORD
                    }
                )
            }
        }
    )
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
            uiState.isLoading && uiState.rankings.isEmpty() -> LoadingSection()
            uiState.error != null && uiState.rankings.isEmpty() -> EmptyStateCard(
                title = AppStrings.Errors.LOAD_RANKING_FAILED,
                message = uiState.error ?: AppStrings.Errors.LOAD_RANKING_FAILED,
                actionLabel = AppStrings.Common.RETRY,
                onAction = { viewModel.onAction(RankingAction.Retry) },
                isError = true
            )
            else -> {
                uiState.error?.let { error ->
                    InlineNotice(
                        message = error,
                        isError = true,
                        actionLabel = AppStrings.Common.RETRY,
                        onAction = { viewModel.onAction(RankingAction.Retry) }
                    )
                }
                if (uiState.rankings.isEmpty()) {
                    EmptyStateCard(
                        title = AppStrings.Ranking.NO_DATA,
                        message = AppStrings.Ranking.EMPTY_MESSAGE
                    )
                } else {
                    RankingContent(uiState.rankings, gameType)
                }
            }
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

    LaunchedEffect(Unit) { viewModel.onAction(GameRecordAction.Load) }
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
            uiState.isLoading && uiState.records.isEmpty() -> LoadingSection()
            uiState.error != null && uiState.records.isEmpty() -> EmptyStateCard(
                title = AppStrings.Errors.LOAD_RECORDS_FAILED,
                message = uiState.error ?: AppStrings.Errors.LOAD_RECORDS_FAILED,
                actionLabel = AppStrings.Common.RETRY,
                onAction = { viewModel.onAction(GameRecordAction.Retry) },
                isError = true
            )
            else -> {
                uiState.error?.let { error ->
                    InlineNotice(
                        message = error,
                        isError = true,
                        actionLabel = AppStrings.Common.RETRY,
                        onAction = { viewModel.onAction(GameRecordAction.Retry) }
                    )
                }
                if (uiState.records.isEmpty()) {
                    EmptyStateCard(
                        title = AppStrings.Records.NO_RECORDS,
                        message = AppStrings.Records.EMPTY_MESSAGE
                    )
                } else {
                    GameRecordContent(
                        records = uiState.records,
                        expandedRecordId = uiState.expandedRecordId,
                        onToggleExpanded = { viewModel.onAction(GameRecordAction.ToggleExpanded(it)) },
                        onDelete = { viewModel.onAction(GameRecordAction.RequestDelete(it)) }
                    )
                }
            }
        }
    }

    uiState.pendingDelete?.let {
        AlertDialog(
            onDismissRequest = { viewModel.onAction(GameRecordAction.DismissDelete) },
            title = { Text(AppStrings.Records.DELETE_TITLE) },
            text = { Text(AppStrings.Records.DELETE_MESSAGE) },
            confirmButton = {
                Button(onClick = { viewModel.onAction(GameRecordAction.ConfirmDelete) }) {
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
    modifier: Modifier = Modifier,
    word: WordItem,
    onPlayUk: () -> Unit,
    onPlayUs: () -> Unit,
    onMasteryChanged: (Boolean) -> Unit
) {
    val statusContainer = if (word.isMastered) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    OutlinedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = word.english,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = word.pronunciation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(color = statusContainer, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = if (word.isMastered) AppStrings.WordBook.REMEMBERED else AppStrings.Common.LEARNING,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Text(text = word.chinese, style = MaterialTheme.typography.bodyLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onPlayUk) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = AppStrings.Accessibility.PLAY_UK)
                }
                Text(AppStrings.Common.UK, style = MaterialTheme.typography.labelMedium)
                IconButton(onClick = onPlayUs) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = AppStrings.Accessibility.PLAY_US)
                }
                Text(AppStrings.Common.US, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.width(8.dp))
                if (word.isMastered) {
                    OutlinedButton(
                        onClick = { onMasteryChanged(false) },
                        modifier = Modifier.heightIn(min = 44.dp)
                    ) {
                        Text(AppStrings.Common.RESET)
                    }
                } else {
                    FilledTonalButton(
                        onClick = { onMasteryChanged(true) },
                        modifier = Modifier.heightIn(min = 44.dp)
                    ) {
                        Text(AppStrings.Common.MARK)
                    }
                }
            }
        }
    }
}

@Composable
private fun RankingContent(rankings: List<RankingItem>, gameType: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RankingOverviewCard(rankings, gameType)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = rankings,
                key = { item -> "${item.username}-${item.time}-${item.avatarVersion}" }
            ) { item ->
                val index = rankings.indexOf(item)
                if (index < AppConstants.Ranking.HIGHLIGHT_COUNT) {
                    RankingHighlightCard(index, item)
                } else {
                    RankingStandardCard(index, item)
                }
            }
        }
    }
}

@Composable
private fun RankingOverviewCard(rankings: List<RankingItem>, gameType: Int) {
    val topScore = rankings.maxOfOrNull { it.score } ?: 0
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Leaderboard, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = AppStrings.Records.modeTitle(gameType == 0),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Text(
                text = AppStrings.Ranking.TOP_PLAYERS_DESCRIPTION,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(AppStrings.Ranking.PLAYERS, rankings.size.toString(), Modifier.weight(1f))
                StatCard(AppStrings.Ranking.TOP_SCORE, topScore.toString(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RankingHighlightCard(index: Int, item: RankingItem) {
    val color = when (index) {
        0 -> MaterialTheme.colorScheme.primaryContainer
        1 -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = AppStrings.Ranking.compactRank(index),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            UserAvatar(
                imageUrl = item.avatarUrl,
                fallbackLabel = item.username.take(1).uppercase(),
                size = appDimens().smallAvatarSize
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(item.username, style = MaterialTheme.typography.titleMedium)
                Text(
                    item.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(item.score.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RankingStandardCard(index: Int, item: RankingItem) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = AppStrings.Ranking.compactRank(index),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(38.dp)
            )
            UserAvatar(
                imageUrl = item.avatarUrl,
                fallbackLabel = item.username.take(1).uppercase(),
                size = appDimens().smallAvatarSize
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(item.username, style = MaterialTheme.typography.titleMedium)
                Text(
                    item.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(item.score.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RecordOverviewCard(records)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(records, key = { it.id }) { record ->
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
    val bestScore = records.maxOfOrNull { it.score } ?: 0
    val learnedWordTotal = records.sumOf { it.wordProgress.size }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(AppStrings.Records.YOUR_RECENT_RUNS, style = MaterialTheme.typography.titleLarge)
            Text(
                AppStrings.Records.RECENT_RUNS_DESCRIPTION,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(AppStrings.Common.RECORDS, records.size.toString(), Modifier.weight(1f))
                StatCard(AppStrings.Records.BEST_SCORE, bestScore.toString(), Modifier.weight(1f))
                StatCard(AppStrings.Common.WORDS, learnedWordTotal.toString(), Modifier.weight(1f))
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
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(record.gameTypeLabel, style = MaterialTheme.typography.titleMedium)
                    Text(
                        record.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(record.score.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(AppStrings.Common.SCORE, style = MaterialTheme.typography.labelMedium)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, enabled = false, label = { Text(record.gameTypeLabel) })
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(AppStrings.WordBook.matched(record.wordProgress.size)) }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onToggleExpanded,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) AppStrings.Accessibility.COLLAPSE else AppStrings.Accessibility.EXPAND
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (expanded) AppStrings.Records.HIDE_DETAILS else AppStrings.Records.VIEW_DETAILS)
                }
                TextButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = AppStrings.Accessibility.DELETE)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AppStrings.Common.DELETE)
                }
            }
            if (expanded) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(AppStrings.Records.LEARNED_WORDS, style = MaterialTheme.typography.titleMedium)
                        HorizontalDivider()
                        if (record.wordProgress.isEmpty()) {
                            Text(
                                AppStrings.Records.NO_LEARNED_WORDS,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            record.wordProgress.forEachIndexed { index, progress ->
                                Text(
                                    AppStrings.WordBook.numbered(index, progress.displayLabel),
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

private fun WordFilter.toDisplayName(): String = when (this) {
    WordFilter.ALL -> AppStrings.Common.ALL
    WordFilter.MASTERED -> AppStrings.Common.MASTERED
    WordFilter.UNMASTERED -> AppStrings.Common.LEARNING
}
