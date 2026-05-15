package com.vero.repolens.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vero.repolens.data.engine.RecommendationEngine
import com.vero.repolens.data.models.RepoLensReport
import com.vero.repolens.ui.components.ExportDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    report: RepoLensReport,
    onNavigateToArchitecture: () -> Unit,
    onNavigateToModules: () -> Unit,
    onNavigateToFeatures: () -> Unit,
    onNavigateToDI: () -> Unit,
    onNavigateToUI: () -> Unit,
    onNavigateToConcurrency: () -> Unit,
    onNavigateToTesting: () -> Unit,
    onNavigateToRisks: () -> Unit,
    onNavigateToPR: () -> Unit,
    onNavigateToSearch: () -> Unit = {},
    onNavigateToActionItems: () -> Unit = {},
    onNavigateToPerformance: () -> Unit = {},
    onNavigateToRecommendations: () -> Unit = {},
    onNavigateToExport: () -> Unit = {}
) {
    val generatedRecommendations = remember(report) {
        RecommendationEngine().generateRecommendations(report)
    }
    var showExportDialog by remember { mutableStateOf(false) }
    if (showExportDialog) {
        ExportDialog(
            report = report,
            onDismiss = { showExportDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RepoLens") },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                OverviewHeroCard(report = report)
            }

            item {
                FeaturedScoresRow(report = report)
            }

            item {
                OverviewLabel(text = "Snapshot")
            }

            item {
                MetricsGrid(report = report)
            }

            item {
                OverviewLabel(text = "Architecture")
            }

            item {
                OverviewActionCard(
                    title = "Architecture",
                    subtitle = report.architecture.style,
                    supporting = architectureSummary(report),
                    icon = Icons.Default.Architecture,
                    onClick = onNavigateToArchitecture
                )
            }

            item {
                OverviewActionCard(
                    title = "Modules",
                    subtitle = "${report.modules.size} modules",
                    supporting = report.modules.take(3).joinToString(", ") { it.name }.ifBlank { "Project structure overview" },
                    icon = Icons.Default.Folder,
                    onClick = onNavigateToModules
                )
            }

            item {
                OverviewActionCard(
                    title = "Features",
                    subtitle = "${report.features.size} detected",
                    supporting = report.features.take(3).joinToString(", ") { it.name }.ifBlank { "Feature landscape" },
                    icon = Icons.Default.Star,
                    onClick = onNavigateToFeatures
                )
            }

            item {
                OverviewLabel(text = "Code Health")
            }

            report.dependencyInjection?.let { di ->
                item {
                    OverviewActionCard(
                        title = "Dependency Injection",
                        subtitle = di.framework,
                        supporting = di.summary,
                        icon = Icons.Default.Link,
                        onClick = onNavigateToDI
                    )
                }
            }

            report.uiLayer?.let { uiLayer ->
                item {
                    OverviewActionCard(
                        title = "UI Layer",
                        subtitle = uiLayer.framework,
                        supporting = uiLayer.summary,
                        icon = Icons.Default.Smartphone,
                        onClick = onNavigateToUI
                    )
                }
            }

            report.concurrency?.let { concurrency ->
                item {
                    OverviewActionCard(
                        title = "Concurrency",
                        subtitle = concurrency.technologies.take(2).joinToString(", "),
                        supporting = concurrency.summary,
                        icon = Icons.Default.Sync,
                        onClick = onNavigateToConcurrency
                    )
                }
            }

            report.testing?.let {
                item {
                    OverviewActionCard(
                        title = "Testing",
                        subtitle = "${report.metrics.existingTestsCount} tests",
                        supporting = "${report.metrics.missingTestsCount} gaps need attention",
                        icon = Icons.Default.Science,
                        onClick = onNavigateToTesting
                    )
                }
            }

            item {
                OverviewLabel(text = "Workflow")
            }

            item {
                OverviewActionCard(
                    title = "Risks",
                    subtitle = "${report.risks.size} identified",
                    supporting = riskSummary(report),
                    icon = Icons.Default.Warning,
                    onClick = onNavigateToRisks,
                    accentColor = MaterialTheme.colorScheme.tertiary
                )
            }

            report.prReadiness?.let { prReadiness ->
                item {
                    OverviewActionCard(
                        title = "PR Readiness",
                        subtitle = "Score ${prReadiness.score}%",
                        supporting = prReadiness.summary,
                        icon = Icons.Default.CheckCircle,
                        onClick = onNavigateToPR,
                        accentColor = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            item {
                OverviewActionCard(
                    title = "Action Items",
                    subtitle = "Track follow-up work",
                    supporting = "Manage development tasks and implementation next steps",
                    icon = Icons.Default.Task,
                    onClick = onNavigateToActionItems
                )
            }

            item {
                OverviewLabel(text = "Insights")
            }

            report.performance?.let { performance ->
                item {
                    OverviewActionCard(
                        title = "Performance Metrics",
                        subtitle = "${performance.buildTimeSeconds}s build time",
                        supporting = "${performance.methodCount} methods, ${performance.appSizeMB} MB app size",
                        icon = Icons.Default.Speed,
                        onClick = onNavigateToPerformance
                    )
                }
            }

            item {
                OverviewActionCard(
                    title = "Smart Recommendations",
                    subtitle = "${generatedRecommendations.size} recommendations",
                    supporting = "AI-guided opportunities across testing, architecture, and performance",
                    icon = Icons.Default.AutoAwesome,
                    onClick = onNavigateToRecommendations,
                    accentColor = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Text(
                    text = "Generated by ${report.metadata.generatedBy}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OverviewHeroCard(report: RepoLensReport) {
    var summaryExpanded by remember(report.repository.summary) { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = report.repository.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = report.repository.summary,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.86f),
                maxLines = if (summaryExpanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis
            )
            if (report.repository.summary.length > 140) {
                TextButton(
                    onClick = { summaryExpanded = !summaryExpanded },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (summaryExpanded) "Show less" else "Read more",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroChip(
                    icon = Icons.Default.Code,
                    label = report.repository.mainLanguage
                )
                HeroChip(
                    icon = Icons.Default.PhoneAndroid,
                    label = report.repository.platform
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MetaLine(
                        label = "Architecture",
                        value = report.repository.architectureStyle
                    )
                    MetaLine(
                        label = "Generated",
                        value = formatDate(report.metadata.generatedAt)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroChip(
    icon: ImageVector,
    label: String
) {
    AssistChip(
        onClick = {},
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f),
            labelColor = MaterialTheme.colorScheme.onSurface,
            leadingIconContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun MetaLine(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FeaturedScoresRow(report: RepoLensReport) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FeaturedScoreCard(
            title = "PR Readiness",
            value = "${report.metrics.prReadinessScore}%",
            subtitle = "Review confidence",
            icon = Icons.Default.CheckCircle,
            accentColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
        FeaturedScoreCard(
            title = "Architecture",
            value = "${report.metrics.architectureConfidenceScore}%",
            subtitle = "Structural confidence",
            icon = Icons.Default.Architecture,
            accentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FeaturedScoreCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = accentColor.copy(alpha = 0.14f),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OverviewLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun MetricsGrid(report: RepoLensReport) {
    val metrics = listOf(
        SnapshotMetric("Modules", report.modules.size.toString(), Icons.Default.Folder),
        SnapshotMetric("Features", report.features.size.toString(), Icons.Default.Star),
        SnapshotMetric("High Risks", report.metrics.highRiskAreasCount.toString(), Icons.Default.Warning),
        SnapshotMetric("Missing Tests", report.metrics.missingTestsCount.toString(), Icons.Default.BugReport)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        metrics.chunked(2).forEach { rowMetrics ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowMetrics.forEach { metric ->
                    SnapshotMetricCard(
                        metric = metric,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowMetrics.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SnapshotMetricCard(
    metric: SnapshotMetric,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = metric.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = metric.value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = metric.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OverviewActionCard(
    title: String,
    subtitle: String,
    supporting: String,
    icon: ImageVector,
    onClick: () -> Unit,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = accentColor.copy(alpha = 0.12f),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private data class SnapshotMetric(
    val title: String,
    val value: String,
    val icon: ImageVector
)

private fun architectureSummary(report: RepoLensReport): String {
    return report.architecture.layers
        .take(3)
        .joinToString(" · ") { it.name }
        .ifBlank { report.architecture.summary }
}

private fun riskSummary(report: RepoLensReport): String {
    return report.risks
        .take(2)
        .joinToString(" · ") { it.title }
        .ifBlank { "Review the most important delivery risks" }
}

private fun formatDate(isoDate: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(isoDate)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        isoDate
    }
}

// Made with Bob
