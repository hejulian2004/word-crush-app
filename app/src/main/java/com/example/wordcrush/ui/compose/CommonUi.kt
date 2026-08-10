package com.example.wordcrush.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.wordcrush.constants.AppStrings

@Composable
internal fun LoadingScreen(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(text = message, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
internal fun AuthScreen(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BrandMark()
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
    }
}

@Composable
private fun BrandMark() {
    Surface(
        modifier = Modifier.size(64.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "W",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScreenScaffold(
    title: String,
    onBack: () -> Unit,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val dims = appDimens()
    val appBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
        actionIconContentColor = MaterialTheme.colorScheme.onBackground
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
            .imePadding()
    ) {
        TopAppBar(
            title = { Text(title, style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = AppStrings.Accessibility.BACK
                    )
                }
            },
            colors = appBarColors
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = dims.contentMaxWidth)
                .align(Alignment.CenterHorizontally)
                .then(if (scrollable) Modifier else Modifier.weight(1f))
                .padding(horizontal = dims.pagePadding),
            verticalArrangement = Arrangement.spacedBy(dims.sectionSpacing),
            content = content
        )
        if (scrollable) {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
internal fun ScreenHeader(
    title: String,
    subtitle: String,
    subtitleMaxLines: Int = Int.MAX_VALUE
) {
    Column(
        modifier = Modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = subtitleMaxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun ActionButtonRow(
    primaryLabel: String,
    onPrimaryClick: () -> Unit,
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
            Text(primaryLabel, textAlign = TextAlign.Center)
        }
        FilledTonalButton(
            onClick = onSecondaryClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text(secondaryLabel, textAlign = TextAlign.Center)
        }
    }
}

@Composable
internal fun StatCard(
    title: String,
    value: String,
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
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
internal fun ProgressSummary(
    label: String,
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (total > 0) (completed.toFloat() / total).coerceIn(0f, 1f) else 0f
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(
                text = "$completed/$total",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer
        )
    }
}

@Composable
internal fun AvatarBadge(
    label: String,
    size: Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
internal fun EmptyStateCard(
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    isError: Boolean = false
) {
    val container = if (isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val content = if (isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = container
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = if (isError) Icons.Outlined.ErrorOutline else Icons.Outlined.Info,
                contentDescription = null,
                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = content
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            if (actionLabel != null && onAction != null) {
                OutlinedButton(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
internal fun InlineNotice(
    message: String,
    isError: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val container = if (isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val content = if (isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isError) Icons.Outlined.ErrorOutline else Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) {
                    Text(actionLabel, color = content)
                }
            }
        }
    }
}

@Composable
internal fun LoadingSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
internal fun ErrorText(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error
    )
}
