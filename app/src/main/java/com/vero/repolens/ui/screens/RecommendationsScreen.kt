package com.vero.repolens.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vero.repolens.data.models.*
import com.vero.repolens.ui.components.RecommendationCard
import com.vero.repolens.viewmodel.RecommendationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsScreen(
    onNavigateBack: () -> Unit,
    viewModel: RecommendationsViewModel = hiltViewModel()
) {
    val recommendations by viewModel.filteredRecommendations.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedImpact by viewModel.selectedImpact.collectAsState()
    val selectedEffort by viewModel.selectedEffort.collectAsState()
    
    var showFilters by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Recommendations") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter chips
            if (showFilters) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Filters",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // Category filter
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { viewModel.setCategory(null) },
                                label = { Text("All") }
                            )
                            listOf("Testing", "Performance", "Security", "Architecture", "Code Quality").forEach { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { viewModel.setCategory(category) },
                                    label = { Text(category) }
                                )
                            }
                        }

                        // Impact filter
                        Text(
                            text = "Impact",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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

                        // Effort filter
                        Text(
                            text = "Effort",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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

                        // Clear filters button
                        if (selectedCategory != null || selectedImpact != null || selectedEffort != null) {
                            TextButton(
                                onClick = { viewModel.clearFilters() },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Clear Filters")
                            }
                        }
                    }
                }
            }

            // Summary card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${recommendations.size} Recommendations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Based on your codebase analysis",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Recommendations list
            if (recommendations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "No recommendations match your filters",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (selectedCategory != null || selectedImpact != null || selectedEffort != null) {
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

// Made with Bob
