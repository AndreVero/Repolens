package com.vero.repolens.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vero.repolens.data.models.RepoLensReport
import com.vero.repolens.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

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
    onNavigateToPR: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RepoLens") },
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
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Repository Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = report.repository.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = report.repository.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(
                                onClick = { },
                                label = { Text(report.repository.mainLanguage) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Code,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                            AssistChip(
                                onClick = { },
                                label = { Text(report.repository.platform) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.PhoneAndroid,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Architecture: ${report.repository.architectureStyle}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Generated: ${formatDate(report.metadata.generatedAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Metrics Grid
            item {
                SectionHeader(title = "Key Metrics")
            }

            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(400.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        listOf(
                            MetricData("Modules", report.metrics.modulesCount.toString(), Icons.Default.Folder),
                            MetricData("Features", report.metrics.featuresCount.toString(), Icons.Default.Star),
                            MetricData("High Risks", report.metrics.highRiskAreasCount.toString(), Icons.Default.Warning),
                            MetricData("Missing Tests", report.metrics.missingTestsCount.toString(), Icons.Default.BugReport),
                            MetricData("PR Score", "${report.metrics.prReadinessScore}%", Icons.Default.CheckCircle),
                            MetricData("Arch Score", "${report.metrics.architectureConfidenceScore}%", Icons.Default.Architecture)
                        )
                    ) { metric ->
                        MetricCard(
                            title = metric.title,
                            value = metric.value,
                            icon = metric.icon
                        )
                    }
                }
            }

            // Navigation Sections
            item {
                SectionHeader(title = "Explore")
            }

            item {
                SectionCard(
                    title = "Architecture",
                    subtitle = report.architecture.style,
                    icon = Icons.Default.Architecture,
                    onClick = onNavigateToArchitecture
                )
            }

            item {
                SectionCard(
                    title = "Modules",
                    subtitle = "${report.modules.size} modules in project",
                    icon = Icons.Default.Folder,
                    onClick = onNavigateToModules
                )
            }

            item {
                SectionCard(
                    title = "Features",
                    subtitle = "${report.features.size} features detected",
                    icon = Icons.Default.Star,
                    onClick = onNavigateToFeatures
                )
            }

            if (report.dependencyInjection != null) {
                item {
                    SectionCard(
                        title = "Dependency Injection",
                        subtitle = report.dependencyInjection.framework,
                        icon = Icons.Default.Link,
                        onClick = onNavigateToDI
                    )
                }
            }

            if (report.uiLayer != null) {
                item {
                    SectionCard(
                        title = "UI Layer",
                        subtitle = report.uiLayer.framework,
                        icon = Icons.Default.Smartphone,
                        onClick = onNavigateToUI
                    )
                }
            }

            if (report.concurrency != null) {
                item {
                    SectionCard(
                        title = "Concurrency",
                        subtitle = report.concurrency.technologies.joinToString(", "),
                        icon = Icons.Default.Sync,
                        onClick = onNavigateToConcurrency
                    )
                }
            }

            if (report.testing != null) {
                item {
                    SectionCard(
                        title = "Testing",
                        subtitle = "${report.metrics.existingTestsCount} tests, ${report.metrics.missingTestsCount} gaps",
                        icon = Icons.Default.Science,
                        onClick = onNavigateToTesting
                    )
                }
            }

            item {
                SectionCard(
                    title = "Risks",
                    subtitle = "${report.risks.size} risks identified",
                    icon = Icons.Default.Warning,
                    onClick = onNavigateToRisks
                )
            }

            if (report.prReadiness != null) {
                item {
                    SectionCard(
                        title = "PR Readiness",
                        subtitle = "Score: ${report.prReadiness.score}%",
                        icon = Icons.Default.CheckCircle,
                        onClick = onNavigateToPR
                    )
                }
            }

            // Footer
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Generated by ${report.metadata.generatedBy}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private data class MetricData(
    val title: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

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
