package com.vero.repolens.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vero.repolens.data.models.Risk
import com.vero.repolens.ui.components.ConfidenceBadge
import com.vero.repolens.ui.components.EmptyState
import com.vero.repolens.ui.components.FilePathChip
import com.vero.repolens.ui.components.SeverityBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RisksScreen(
    risks: List<Risk>,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Risks") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        if (risks.isEmpty()) {
            EmptyState(
                message = "No risks identified",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            val groupedRisks = risks.groupBy { it.severity.lowercase() }
            val severityOrder = listOf("critical", "high", "medium", "low")

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    RisksSummaryCard(risks = risks)
                }

                severityOrder.forEach { severity ->
                    val risksInCategory = groupedRisks[severity] ?: emptyList()
                    if (risksInCategory.isNotEmpty()) {
                        item {
                            SeveritySectionHeader(
                                severity = severity,
                                count = risksInCategory.size
                            )
                        }

                        items(risksInCategory) { risk ->
                            RiskCard(risk = risk)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RisksSummaryCard(risks: List<Risk>) {
    val criticalCount = risks.count { it.severity.equals("critical", true) }
    val highCount = risks.count { it.severity.equals("high", true) }
    val mediumCount = risks.count { it.severity.equals("medium", true) }
    val lowCount = risks.count { it.severity.equals("low", true) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.32f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "${risks.size} risks identified",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (criticalCount > 0) RiskInfoChip("$criticalCount critical")
                if (highCount > 0) RiskInfoChip("$highCount high")
                if (mediumCount > 0) RiskInfoChip("$mediumCount medium")
                if (lowCount > 0) RiskInfoChip("$lowCount low")
                RiskInfoChip("${risks.count { it.affectedFiles.isNotEmpty() }} with files")
            }
        }
    }
}

@Composable
private fun SeveritySectionHeader(
    severity: String,
    count: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${severity.displayName()} ($count)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Surface(
            color = severityAccent(severity).copy(alpha = 0.12f),
            shape = MaterialTheme.shapes.large
        ) {
            Text(
                text = severity.displayName(),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                color = severityAccent(severity)
            )
        }
    }
}

@Composable
private fun RiskCard(risk: Risk) {
    val accent = severityAccent(risk.severity)
    val container = when (risk.severity.lowercase()) {
        "critical", "high" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.94f)
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = when (risk.severity.lowercase()) {
        "critical", "high" -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    val mutedColor = when (risk.severity.lowercase()) {
        "critical", "high" -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.78f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = container),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = contentColor
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = risk.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor
                        )
                        Text(
                            text = "Category: ${risk.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = mutedColor
                        )
                    }
                }
                SeverityBadge(severity = risk.severity)
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                risk.confidence?.let { ConfidenceBadge(confidence = it) }
                if (risk.affectedModules.isNotEmpty()) {
                    RiskInfoChip("${risk.affectedModules.size} modules", accent = accent)
                }
                if (risk.affectedFiles.isNotEmpty()) {
                    RiskInfoChip("${risk.affectedFiles.size} files", accent = accent)
                }
            }

            risk.whyItMatters?.let {
                RiskInfoPanel(
                    title = "Why it matters",
                    body = it,
                    titleColor = accent,
                    bodyColor = contentColor,
                    tone = accent.copy(alpha = 0.08f)
                )
            }

            risk.recommendation?.let {
                RiskInfoPanel(
                    title = "Recommendation",
                    body = it,
                    titleColor = accent,
                    bodyColor = contentColor,
                    tone = MaterialTheme.colorScheme.surface.copy(alpha = 0.58f)
                )
            }

            risk.suggestedTest?.let {
                RiskInfoPanel(
                    title = "Suggested test",
                    body = it,
                    titleColor = accent,
                    bodyColor = contentColor,
                    tone = accent.copy(alpha = 0.08f)
                )
            }

            if (risk.affectedFiles.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Affected files",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        risk.affectedFiles.forEach { file ->
                            FilePathChip(path = file)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RiskInfoPanel(
    title: String,
    body: String,
    titleColor: Color,
    bodyColor: Color,
    tone: Color
) {
    Surface(
        color = tone,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = bodyColor
            )
        }
    }
}

@Composable
private fun RiskInfoChip(
    text: String,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        color = accent.copy(alpha = 0.14f),
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.16f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(10.dp),
                shape = MaterialTheme.shapes.small,
                color = accent
            ) {}
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun severityAccent(severity: String): Color = when (severity.lowercase()) {
    "critical", "high" -> MaterialTheme.colorScheme.error
    "medium" -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.primary
}

private fun String.displayName(): String = replaceFirstChar { it.uppercase() }

// Made with Bob
