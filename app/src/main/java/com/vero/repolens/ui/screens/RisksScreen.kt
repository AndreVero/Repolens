package com.vero.repolens.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vero.repolens.data.models.Risk
import com.vero.repolens.ui.components.*

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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "${risks.size} risks identified",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                severityOrder.forEach { severity ->
                    val risksInCategory = groupedRisks[severity] ?: emptyList()
                    if (risksInCategory.isNotEmpty()) {
                        item {
                            SectionHeader(title = "${severity.capitalize()} (${risksInCategory.size})")
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
private fun RiskCard(risk: Risk) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (risk.severity.lowercase()) {
                "critical", "high" -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = risk.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                SeverityBadge(severity = risk.severity)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Category: ${risk.category}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (risk.whyItMatters != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Why it matters:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = risk.whyItMatters,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (risk.recommendation != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Recommendation:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = risk.recommendation,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (risk.suggestedTest != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Suggested test:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = risk.suggestedTest,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (risk.affectedFiles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Affected files:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    risk.affectedFiles.take(5).forEach { file ->
                        FilePathChip(path = file)
                    }
                }
            }

            if (risk.confidence != null) {
                Spacer(modifier = Modifier.height(12.dp))
                ConfidenceBadge(confidence = risk.confidence)
            }
        }
    }
}

private fun String.capitalize() = replaceFirstChar { it.uppercase() }

// Made with Bob
