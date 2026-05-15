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
import com.vero.repolens.data.models.Feature
import com.vero.repolens.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureDetailScreen(
    feature: Feature,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(feature.name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
            // Feature Info
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = feature.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            SeverityBadge(severity = feature.riskLevel)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = feature.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (feature.businessPurpose != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Purpose: ${feature.businessPurpose}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ComplexityBadge(complexity = feature.complexity)
                            ConfidenceBadge(confidence = feature.confidence)
                        }
                    }
                }
            }

            // Entry Points
            if (feature.entryPoints.isNotEmpty()) {
                item { SectionHeader(title = "Entry Points") }
                item {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        feature.entryPoints.forEach { entry ->
                            FilePathChip(path = entry)
                        }
                    }
                }
            }

            // Core Files
            if (feature.coreFiles.isNotEmpty()) {
                item { SectionHeader(title = "Core Files") }
                item {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        feature.coreFiles.forEach { file ->
                            FilePathChip(path = file)
                        }
                    }
                }
            }

            // Architecture Components
            if (feature.viewModels.isNotEmpty() || feature.useCases.isNotEmpty() || feature.repositories.isNotEmpty()) {
                item { SectionHeader(title = "Architecture Components") }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            if (feature.viewModels.isNotEmpty()) {
                                Text(
                                    text = "ViewModels:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                feature.viewModels.forEach { vm ->
                                    Text(
                                        text = "• $vm",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                    )
                                }
                            }
                            if (feature.useCases.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Use Cases:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                feature.useCases.forEach { uc ->
                                    Text(
                                        text = "• $uc",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                    )
                                }
                            }
                            if (feature.repositories.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Repositories:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                feature.repositories.forEach { repo ->
                                    Text(
                                        text = "• $repo",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // UI Components
            if (feature.uiComponents.isNotEmpty()) {
                item { SectionHeader(title = "UI Components") }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            feature.uiComponents.forEach { component ->
                                Text(
                                    text = "• $component",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // State Model
            if (feature.stateModel != null) {
                item { SectionHeader(title = "State Model") }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            if (feature.stateModel.stateClass != null) {
                                Text(
                                    text = "State Class: ${feature.stateModel.stateClass}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (feature.stateModel.states.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "States: ${feature.stateModel.states.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (feature.stateModel.events.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Events: ${feature.stateModel.events.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (feature.stateModel.sideEffects.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Side Effects: ${feature.stateModel.sideEffects.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            // Testing
            item { SectionHeader(title = "Testing") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        if (feature.existingTests.isNotEmpty()) {
                            Text(
                                text = "Existing Tests:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            feature.existingTests.forEach { test ->
                                Text(
                                    text = "• $test",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                )
                            }
                        }
                        if (feature.missingTests.isNotEmpty()) {
                            if (feature.existingTests.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            Text(
                                text = "Missing Tests:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                            feature.missingTests.forEach { test ->
                                Text(
                                    text = "• $test",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Risks
            if (feature.risks.isNotEmpty()) {
                item { SectionHeader(title = "Risks") }
                items(feature.risks) { risk ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
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
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                SeverityBadge(severity = risk.severity)
                            }
                            if (risk.whyItMatters != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = risk.whyItMatters,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            if (risk.recommendation != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "→ ${risk.recommendation}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }

            // PR Checklist
            if (feature.suggestedPrChecklist.isNotEmpty()) {
                item { SectionHeader(title = "PR Checklist") }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            feature.suggestedPrChecklist.forEach { item ->
                                Text(
                                    text = "☐ $item",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Made with Bob
