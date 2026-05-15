package com.vero.repolens.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vero.repolens.data.models.SearchFilter
import com.vero.repolens.data.models.SearchItemType
import com.vero.repolens.data.models.SearchableItem
import com.vero.repolens.ui.components.LoadingState
import com.vero.repolens.viewmodel.SearchViewModel

private val SearchFieldShape = RoundedCornerShape(18.dp)
private val SearchFilterChipShape = RoundedCornerShape(18.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onItemClick: (SearchableItem) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val activeFilter by viewModel.activeFilter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val availableSeverities by viewModel.availableSeverities.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (searchQuery.isNotEmpty() || activeFilter != SearchFilter.DEFAULT) {
                        IconButton(onClick = {
                            viewModel.clearSearch()
                            viewModel.clearFilters()
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear all")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchFieldCard(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) }
            )

            FilterGroupCard(
                activeFilter = activeFilter,
                availableSeverities = availableSeverities.toList(),
                onTypeToggle = viewModel::toggleTypeFilter,
                onSeverityToggle = viewModel::toggleSeverityFilter
            )

            if (isLoading) {
                LoadingState()
            } else if (searchResults.isEmpty() && searchQuery.isNotEmpty()) {
                EmptySearchState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Text(
                            text = "${searchResults.size} results",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(searchResults) { item ->
                        SearchResultCard(
                            item = item,
                            onClick = { onItemClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchFieldCard(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.26f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            placeholder = { Text("Search modules, features, risks...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = SearchFieldShape
        )
    }
}

@Composable
private fun FilterGroupCard(
    activeFilter: SearchFilter,
    availableSeverities: List<String>,
    onTypeToggle: (SearchItemType) -> Unit,
    onSeverityToggle: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Filter by Type",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SearchItemType.values().forEach { type ->
                        FilterChip(
                            selected = activeFilter.types.contains(type),
                            onClick = { onTypeToggle(type) },
                            label = { Text(type.name.replace('_', ' ')) },
                            leadingIcon = {
                                Icon(
                                    imageVector = getIconForType(type),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            shape = SearchFilterChipShape
                        )
                    }
                }
            }

            if (availableSeverities.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Filter by Severity",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableSeverities) { severity ->
                            FilterChip(
                                selected = activeFilter.severities.contains(severity),
                                onClick = { onSeverityToggle(severity) },
                                label = { Text(severity) },
                                shape = SearchFilterChipShape
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    item: SearchableItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    imageVector = getIconForType(item.type),
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SearchMetaChip(item.type.name.replace('_', ' '))
                    item.severity?.let {
                        SearchMetaChip(
                            text = it,
                            containerColor = getSeverityColor(it).copy(alpha = 0.22f),
                            contentColor = getSeverityColor(it)
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SearchMetaChip(
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.large
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge,
            color = contentColor
        )
    }
}

@Composable
private fun EmptySearchState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No results found",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Try adjusting your search or filters",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getIconForType(type: SearchItemType) = when (type) {
    SearchItemType.MODULE -> Icons.Default.Folder
    SearchItemType.FEATURE -> Icons.Default.Star
    SearchItemType.RISK -> Icons.Default.Warning
    SearchItemType.FILE -> Icons.Default.Description
    SearchItemType.TEST -> Icons.Default.Science
    SearchItemType.ARCHITECTURE_LAYER -> Icons.Default.Layers
}

@Composable
private fun getSeverityColor(severity: String) = when (severity.lowercase()) {
    "critical" -> MaterialTheme.colorScheme.error
    "high" -> MaterialTheme.colorScheme.error.copy(alpha = 0.86f)
    "medium" -> MaterialTheme.colorScheme.tertiary
    "low" -> MaterialTheme.colorScheme.secondary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

// Made with Bob
