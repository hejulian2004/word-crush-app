package com.example.wordcrush.ui.compose

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
import com.example.wordcrush.ui.model.MatchGameEvent
import com.example.wordcrush.ui.viewmodel.GameRecordViewModel
import com.example.wordcrush.ui.viewmodel.HomeViewModel
import com.example.wordcrush.ui.viewmodel.RankingViewModel
import com.example.wordcrush.ui.viewmodel.SessionNavigationEvent
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

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is MatchGameEvent.PlayAudio -> onPlayAudio(event.word, event.type)
                is MatchGameEvent.Message -> onShowMessage(event.text)
                is MatchGameEvent.GameOver -> Unit
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
                viewModel.loadMore()
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
                    onValueChange = viewModel::updateQuery,
                    modifier = Modifier.weight(1f),
                    label = { Text("Search words") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = viewModel::applySearch,
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
                        onClick = { viewModel.updateFilter(filter) },
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
                    onPlayUk = { viewModel.playAudio(word, 1) },
                    onPlayUs = { viewModel.playAudio(word, 2) },
                    onMasteryChanged = { isMastered ->
                        viewModel.updateMastery(word, isMastered)
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
    val navigationEvent by viewModel.navigationEvent.collectAsStateWithLifecycle()
    var showPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var oldPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.messageEvent.collect(onShowMessage)
    }

    LaunchedEffect(navigationEvent) {
        if (navigationEvent == SessionNavigationEvent.NavigateToLogin) {
            viewModel.resetNavigationEvent()
            onLogout()
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
                AvatarBadge(
                    label = uiState.username.ifBlank { "Guest" }.take(1).uppercase(),
                    size = dims.avatarSize
                )
                Spacer(modifier = Modifier.width(dims.pagePadding))
                Column {
                    Text(
                        text = uiState.username.ifBlank { "Guest" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
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
                        onValueChange = viewModel::updateDailyTargetInput,
                        modifier = Modifier.weight(1f),
                        label = { Text("Daily word count") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number
                        )
                    )
                    Button(
                        onClick = viewModel::saveDailyTarget,
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
                    onClick = onOpenRecords,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dims.inputHeight)
                ) {
                    Text("Game records")
                }
                FilledTonalButton(
                    onClick = viewModel::syncCloudData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dims.inputHeight)
                ) {
                    Text("Sync cloud data")
                }
                OutlinedButton(
                    onClick = viewModel::getAllScore,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dims.inputHeight)
                ) {
                    Text("Refresh scores")
                }
                OutlinedButton(
                    onClick = { showPasswordDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dims.inputHeight)
                ) {
                    Text("Change password")
                }
            }
        }
        OutlinedButton(
            onClick = viewModel::logout,
            modifier = Modifier
                .fillMaxWidth()
                .height(dims.inputHeight)
        ) {
            Text("Log out")
        }
        Spacer(modifier = Modifier.height(dims.scaled(24.dp)))
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Change password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Current password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm new password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.changePassword(oldPassword, newPassword, confirmPassword)
                    showPasswordDialog = false
                    oldPassword = ""
                    newPassword = ""
                    confirmPassword = ""
                }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
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
        viewModel.loadRankings(gameType)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let(onShowMessage)
    }

    ScreenScaffold(
        title = if (gameType == 0) "Match ranking" else "Timed ranking",
        onBack = onBack
    ) {
        when {
            uiState.isLoading -> LoadingSection()
            uiState.rankings.isEmpty() -> EmptyStateCard(
                title = "No ranking data",
                message = "Play a few rounds and sync the leaderboard again."
            )
            else -> RankingList(uiState.rankings)
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
    var expandedRecordId by rememberSaveable { mutableStateOf<Int?>(null) }
    var pendingDelete by remember { mutableStateOf<GameRecordItem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadGameRecords()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let(onShowMessage)
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            if (event is MatchGameEvent.Message) {
                onShowMessage(event.text)
            }
        }
    }

    ScreenScaffold(
        title = "Game records",
        onBack = onBack
    ) {
        when {
            uiState.isLoading -> LoadingSection()
            uiState.records.isEmpty() -> EmptyStateCard(
                title = "No local records",
                message = "Finish a game to create your first record."
            )
            else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.records.forEach { record ->
                    RecordCard(
                        record = record,
                        expanded = expandedRecordId == record.id,
                        onToggleExpanded = {
                            expandedRecordId = if (expandedRecordId == record.id) null else record.id
                        },
                        onDelete = { pendingDelete = record }
                    )
                }
            }
        }
    }

    if (pendingDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete record") },
            text = { Text("This removes the local record and attempts to sync the deletion to the server.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteRecord(pendingDelete!!)
                    pendingDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
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
private fun RankingList(rankings: List<RankingItem>) {
    val dims = appDimens()
    Column(verticalArrangement = Arrangement.spacedBy(dims.controlSpacing)) {
        rankings.forEachIndexed { index, item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dims.cardPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarBadge(label = (index + 1).toString(), size = dims.smallAvatarSize)
                    Spacer(modifier = Modifier.width(dims.scaled(14.dp)))
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dims.cardPadding),
            verticalArrangement = Arrangement.spacedBy(dims.scaled(10.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.gameTypeLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = record.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = record.score.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(dims.scaled(10.dp))) {
                OutlinedButton(onClick = onToggleExpanded) {
                    Text(if (expanded) "Hide words" else "Show words")
                }
                TextButton(onClick = onDelete) {
                    Text("Delete")
                }
            }
            if (expanded) {
                HorizontalDivider()
                if (record.learnedWords.isEmpty()) {
                    Text(
                        text = "No learned words saved for this record.",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(dims.scaled(6.dp))) {
                        record.learnedWords.forEachIndexed { index, word ->
                            Text("${index + 1}. $word")
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
