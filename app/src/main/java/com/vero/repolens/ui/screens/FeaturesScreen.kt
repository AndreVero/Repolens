package com.vero.repolens.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vero.repolens.data.models.Feature
import com.vero.repolens.ui.components.ComplexityBadge
import com.vero.repolens.ui.components.ConfidenceBadge
import com.vero.repolens.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeaturesScreen(
    features: List<Feature>,
    onNavigateBack: () -> Unit,
    onFeatureClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Features") },
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
        if (features.isEmpty()) {
            EmptyState(
                message = "No features found",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    FeaturesSummaryCard(features = features)
                }

                items(features) { feature ->
                    FeatureCard(
                        feature = feature,
                        onClick = { onFeatureClick(feature.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturesSummaryCard(features: List<Feature>) {
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
                text = "${features.size} features",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeatureInfoChip("${features.count { it.riskLevel.equals("low", true) }} low risk")
                FeatureInfoChip("${features.count { it.riskLevel.equals("medium", true) }} medium risk")
                FeatureInfoChip("${features.count { it.missingTests.isNotEmpty() }} need tests")
                FeatureInfoChip("${features.count { it.viewModels.isNotEmpty() }} with state")
            }
        }
    }
}

@Composable
private fun FeatureCard(
    feature: Feature,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = feature.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        feature.businessPurpose?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ComplexityBadge(
                    complexity = feature.complexity,
                    label = "Complexity"
                )
                ConfidenceBadge(
                    confidence = feature.confidence,
                    label = "Confidence"
                )
            }

        }
    }
}

@Composable
private fun FeatureInfoChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.14f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(10.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ) {}
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Made with Bob
