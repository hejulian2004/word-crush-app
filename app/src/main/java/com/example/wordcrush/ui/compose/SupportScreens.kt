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
                lastVisibleIndex >= totalCount - 6
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
                title = "Word Book",
                subtitle = "Search, review and mark vocabulary mastery."
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
                    label = { Text("Search words") },
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
                        Text("Search")
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
            title = "Profile",
            subtitle = "Progress, daily plan and account actions."
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
                    fallbackLabel = uiState.username.ifBlank { "Guest" }.take(1).uppercase(),
                    size = dims.avatarSize
                )
                Spacer(modifier = Modifier.width(dims.pagePadding))
                Column(verticalArrangement = Arrangement.spacedBy(dims.tinySpacing)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dims.controlSpacing)
                    ) {
                        Text(
                            text = uiState.username.ifBlank { "Guest" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedButton(
                            onClick = { avatarPicker.launch("image/*") },
                            enabled = !uiState.isUploadingAvatar,
                            modifier = Modifier.height(dims.scaled(36.dp))
                        ) {
                            Text(if (uiState.isUploadingAvatar) "Uploading..." else "Upload")
                        }
                    }
                    Text(
                        text = "Learning profile",
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
                    text = "Score summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Match: ${uiState.breakthroughScore}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Timed: ${uiState.timeLimitScore}",
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
                    text = "Daily learning plan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = when {
                        uiState.allWordsMastered -> "All words have already been learned."
                        uiState.dailyCompleted && uiState.canIncreaseDailyTarget ->
                            "Today's words are done. Increase the daily learning count if you want more words today."
                        uiState.dailyCompleted ->
                            "Today's fixed set is complete."
                        else ->
                            "Today's fixed set: ${uiState.completedTodayCount}/${uiState.todayWordCount} learned. Each word needs 3 correct matches."
                    },
                    style = MaterialTheme.typography.bodyMedium,
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
                        label = { Text("Daily word count") },
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
                        Text("Save")
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
                    text = "Quick actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Button(
                    onClick = { viewModel.onAction(HomeAction.OpenRecords) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dims.inputHeight)
                ) {
                    Text("Game records")
                }
                FilledTonalButton(
                    onClick = { viewModel.onAction(HomeAction.SyncCloudData) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dims.inputHeight)
                ) {
                    Text("Sync cloud data")
                }
                OutlinedButton(
                    onClick = { viewModel.onAction(HomeAction.Refresh) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dims.inputHeight)
                ) {
                    Text("Refresh scores")
                }
                OutlinedButton(
                    onClick = { viewModel.onAction(HomeAction.ShowPasswordDialog) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dims.inputHeight)
                ) {
                    Text("Change password")
                }
            }
        }
        OutlinedButton(
            onClick = { viewModel.onAction(HomeAction.Logout) },
            modifier = Modifier
                .fillMaxWidth()
                .height(dims.inputHeight)
        ) {
            Text("Log out")
        }
        Spacer(modifier = Modifier.height(dims.scaled(24.dp)))
    }

    if (uiState.showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onAction(HomeAction.DismissPasswordDialog) },
            title = { Text("Change password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = uiState.oldPassword,
                        onValueChange = { viewModel.onAction(HomeAction.OldPasswordChanged(it)) },
                        label = { Text("Current password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = uiState.newPassword,
                        onValueChange = { viewModel.onAction(HomeAction.NewPasswordChanged(it)) },
                        label = { Text("New password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = uiState.confirmPassword,
                        onValueChange = { viewModel.onAction(HomeAction.ConfirmPasswordChanged(it)) },
                        label = { Text("Confirm new password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.onAction(HomeAction.SubmitPasswordChange) }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onAction(HomeAction.DismissPasswordDialog) }) {
                    Text("Cancel")
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
        title = if (gameType == 0) "Match ranking" else "Timed ranking",
        onBack = onBack,
        scrollable = false
    ) {
        when {
            uiState.isLoading -> LoadingSection()
            uiState.rankings.isEmpty() -> EmptyStateCard(
                title = "No ranking data",
                message = "Play a few rounds and sync the leaderboard again."
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
        title = "Game records",
        onBack = onBack,
        scrollable = false
    ) {
        when {
            uiState.isLoading -> LoadingSection()
            uiState.records.isEmpty() -> EmptyStateCard(
                title = "No local records",
                message = "Finish a game to create your first record."
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
            title = { Text("Delete record") },
            text = { Text("This removes the local record and attempts to sync the deletion to the server.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.onAction(GameRecordAction.ConfirmDelete)
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onAction(GameRecordAction.DismissDelete) }) {
                    Text("Cancel")
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
    val statusLabel = if (word.isMastered) "Remembered" else "Learning"

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
                        AssistChip(onClick = onPlayUk, label = { Text("UK") })
                        AssistChip(onClick = onPlayUs, label = { Text("US") })
                    }
                    if (word.isMastered) {
                        OutlinedButton(
                            onClick = { onMasteryChanged(false) },
                            modifier = Modifier.height(dims.inputHeight)
                        ) {
                            Text("Reset")
                        }
                    } else {
                        FilledTonalButton(
                            onClick = { onMasteryChanged(true) },
                            modifier = Modifier.height(dims.inputHeight)
                        ) {
                            Text("Mark")
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
    val title = if (gameType == 0) "Match Challenge" else "Timed Match"

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
                text = "Top players are ranked by best score, with earlier finish times breaking ties.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dims.controlSpacing)
            ) {
                StatCard(
                    title = "Players",
                    value = rankings.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Top score",
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
            if (index < 3) {
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
                    text = "Rank #${index + 1}",
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
                    text = "#${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Score",
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
                text = "Your recent runs",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Review saved sessions, compare modes and reopen the learned words from each run.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dims.controlSpacing)
            ) {
                StatCard(
                    title = "Records",
                    value = records.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Best score",
                    value = bestScore.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Words",
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
                        text = "Score",
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
                    label = { Text("${record.wordProgress.size} matched") }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(dims.controlSpacing)) {
                FilledTonalButton(onClick = onToggleExpanded) {
                    Text(if (expanded) "Hide details" else "View details")
                }
                TextButton(onClick = onDelete) {
                    Text("Delete")
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
                            text = "Learned words",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        HorizontalDivider()
                        if (record.wordProgress.isEmpty()) {
                            Text(
                                text = "No learned words saved for this record.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(dims.scaled(6.dp))) {
                                record.wordProgress.forEachIndexed { index, progress ->
                                    Text(
                                        text = "${index + 1}. ${progress.displayLabel}",
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
        WordFilter.ALL -> "All"
        WordFilter.MASTERED -> "Mastered"
        WordFilter.UNMASTERED -> "Learning"
    }
}













