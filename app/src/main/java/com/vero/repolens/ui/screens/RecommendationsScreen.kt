package com.vero.repolens.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vero.repolens.data.models.EffortLevel
import com.vero.repolens.data.models.ImpactLevel
import com.vero.repolens.data.models.SearchFilter
import com.vero.repolens.ui.components.RecommendationCard
import com.vero.repolens.viewmodel.RecommendationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsScreen(
    onNavigateBack: () -> Unit,
    viewModel: RecommendationsViewModel = hiltViewModel()
) {
    val allRecommendations by viewModel.recommendations.collectAsState()
    val recommendations by viewModel.filteredRecommendations.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedImpact by viewModel.selectedImpact.collectAsState()
    val selectedEffort by viewModel.selectedEffort.collectAsState()

    var showFilters by remember { mutableStateOf(false) }
    val categories = remember(allRecommendations) {
        allRecommendations.map { it.category }.distinct().sorted()
    }
    val hasActiveFilters = selectedCategory != null || selectedImpact != null || selectedEffort != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Recommendations") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${recommendations.size} Recommendations",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (hasActiveFilters) {
                                    "${allRecommendations.size} total generated recommendations"
                                } else {
                                    "Based on your codebase analysis"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 12.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryChip("${allRecommendations.count { it.impact == ImpactLevel.HIGH }} high impact")
                        SummaryChip("${allRecommendations.count { it.effort == EffortLevel.SMALL }} quick wins")
                        SummaryChip("${categories.size} categories")
                    }
                }
            }

            if (showFilters) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FilterGroup(
                            title = "Category",
                            content = {
                                FilterChip(
                                    selected = selectedCategory == null,
                                    onClick = { viewModel.setCategory(null) },
                                    label = { Text("All") }
                                )
                                categories.forEach { category ->
                                    FilterChip(
                                        selected = selectedCategory == category,
                                        onClick = { viewModel.setCategory(category) },
                                        label = { Text(category) }
                                    )
                                }
                            }
                        )

                        FilterGroup(
                            title = "Impact",
                            content = {
                                FilterChip(
                                    selected = selectedImpact == null,
                                    onClick = { viewModel.setImpact(null) },
                                    label = { Text("All") }
                                )
                                ImpactLevel.entries.forEach { impact ->
                                    FilterChip(
                                        selected = selectedImpact == impact,
                                        onClick = { viewModel.setImpact(impact) },
                                        label = { Text(impact.name) }
                                    )
                                }
                            }
                        )

                        FilterGroup(
                            title = "Effort",
                            content = {
                                FilterChip(
                                    selected = selectedEffort == null,
                                    onClick = { viewModel.setEffort(null) },
                                    label = { Text("All") }
                                )
                                EffortLevel.entries.forEach { effort ->
                                    FilterChip(
                                        selected = selectedEffort == effort,
                                        onClick = { viewModel.setEffort(effort) },
                                        label = { Text(effort.name) }
                                    )
                                }
                            }
                        )

                        if (hasActiveFilters) {
                            TextButton(
                                onClick = { viewModel.clearFilters() },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = null
                                )
                                Text("Clear Filters")
                            }
                        }
                    }
                }
            }

            if (recommendations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.padding(bottom = 4.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (allRecommendations.isEmpty()) {
                                "No smart recommendations available"
                            } else {
                                "No recommendations match your filters"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (hasActiveFilters) {
                            TextButton(onClick = { viewModel.clearFilters() }) {
                                Text("Clear Filters")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recommendations) { recommendation ->
                        RecommendationCard(recommendation = recommendation)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SummaryChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.76f),
        shape = MaterialTheme.shapes.large
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Made with Bob