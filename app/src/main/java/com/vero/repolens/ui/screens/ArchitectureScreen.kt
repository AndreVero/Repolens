package com.vero.repolens.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vero.repolens.data.models.Architecture
import com.vero.repolens.data.models.ArchitectureDiagram
import com.vero.repolens.data.models.ArchitectureLayer
import com.vero.repolens.data.models.DataFlow
import com.vero.repolens.data.models.DiagramConnection
import com.vero.repolens.data.models.DiagramNode
import com.vero.repolens.ui.components.ArchitectureDiagram
import com.vero.repolens.ui.components.DiagramLegend
import com.vero.repolens.ui.components.FilePathChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchitectureScreen(
    architecture: Architecture,
    diagram: ArchitectureDiagram? = null,
    onNavigateBack: () -> Unit
) {
    var showDiagram by remember { mutableStateOf(false) }
    var selectedNode by remember { mutableStateOf<DiagramNode?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Architecture") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (diagram != null) {
                        IconButton(onClick = { showDiagram = !showDiagram }) {
                            Icon(
                                imageVector = if (showDiagram) Icons.AutoMirrored.Filled.List else Icons.Default.AccountTree,
                                contentDescription = if (showDiagram) "Show list" else "Show diagram"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (showDiagram && diagram != null) {
            ArchitectureDiagramMode(
                architecture = architecture,
                diagram = diagram,
                selectedNode = selectedNode,
                onNodeTap = { selectedNode = it },
                onClearNode = { selectedNode = null },
                paddingValues = paddingValues
            )
        } else {
            ArchitectureListMode(
                architecture = architecture,
                paddingValues = paddingValues
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchitectureDiagramMode(
    architecture: Architecture,
    diagram: ArchitectureDiagram,
    selectedNode: DiagramNode?,
    onNodeTap: (DiagramNode) -> Unit,
    onClearNode: () -> Unit,
    paddingValues: PaddingValues
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val nodeIndex = remember(diagram.nodes) { diagram.nodes.associateBy { it.id } }
    val selectedLayer = remember(selectedNode, architecture.layers) {
        selectedNode?.let { node ->
            architecture.layers.find { it.id.equals(node.id, ignoreCase = true) }
                ?: architecture.layers.find { it.name.equals(node.label, ignoreCase = true) }
        }
    }
    val incomingConnections = remember(selectedNode, diagram.connections, nodeIndex) {
        selectedNode?.let { node ->
            diagram.connections
                .filter { it.to == node.id }
                .map { connection ->
                    GraphConnectionDetail(
                        nodeLabel = nodeIndex[connection.from]?.label ?: connection.from,
                        label = connection.label
                    )
                }
        }.orEmpty()
    }
    val outgoingConnections = remember(selectedNode, diagram.connections, nodeIndex) {
        selectedNode?.let { node ->
            diagram.connections
                .filter { it.from == node.id }
                .map { connection ->
                    GraphConnectionDetail(
                        nodeLabel = nodeIndex[connection.to]?.label ?: connection.to,
                        label = connection.label
                    )
                }
        }.orEmpty()
    }

    LaunchedEffect(selectedNode) {
        if (selectedNode != null) {
            sheetState.show()
        } else if (sheetState.isVisible) {
            sheetState.hide()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = architecture.style,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = architecture.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DiagramStatChip("${diagram.nodes.size} nodes")
                    DiagramStatChip("${diagram.connections.size} dependencies")
                    DiagramStatChip("${architecture.layers.size} layers")
                    architecture.dependencyDirection?.let { DiagramStatChip(it) }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                ArchitectureDiagram(
                    diagram = diagram,
                    selectedNodeId = selectedNode?.id,
                    onNodeTap = onNodeTap,
                    onBackgroundTap = onClearNode
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DiagramLegend()
                Text(
                    text = "Tap a layer to inspect it. Pinch or use the controls to explore the graph.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    selectedNode?.let { node ->
        SelectedNodeBottomSheet(
            node = node,
            layer = selectedLayer,
            incomingConnections = incomingConnections,
            outgoingConnections = outgoingConnections,
            sheetState = sheetState,
            onDismiss = onClearNode
        )
    }
}

@Composable
private fun DiagramStatChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectedNodeBottomSheet(
    node: DiagramNode,
    layer: ArchitectureLayer?,
    incomingConnections: List<GraphConnectionDetail>,
    outgoingConnections: List<GraphConnectionDetail>,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = node.label,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = node.type.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close selection"
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DiagramStatChip("${incomingConnections.size} incoming")
                DiagramStatChip("${outgoingConnections.size} outgoing")
                layer?.let { DiagramStatChip("${it.commonPatterns.size} patterns") }
                layer?.let { DiagramStatChip("${it.relatedFiles.size} files") }
                layer?.let { DiagramStatChip("${it.risks.size} risks") }
            }

            NodeOverviewCard(
                layer = layer,
                fallbackText = "This node is part of the architecture graph. Explore its dependencies and connected layers below."
            )

            if (layer?.commonPatterns?.isNotEmpty() == true) {
                DetailSectionCard(title = "Patterns") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        layer.commonPatterns.forEach { pattern ->
                            DetailBullet(text = pattern, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            if (incomingConnections.isNotEmpty() || outgoingConnections.isNotEmpty()) {
                DetailSectionCard(title = "Connected Layers") {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        if (incomingConnections.isNotEmpty()) {
                            ArchitectureSubsection(title = "Incoming Dependencies")
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                incomingConnections.forEach { connection ->
                                    GraphConnectionRow(connection = connection)
                                }
                            }
                        }

                        if (outgoingConnections.isNotEmpty()) {
                            ArchitectureSubsection(title = "Outgoing Dependencies")
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                outgoingConnections.forEach { connection ->
                                    GraphConnectionRow(connection = connection)
                                }
                            }
                        }
                    }
                }
            }

            if (layer?.relatedFiles?.isNotEmpty() == true) {
                DetailSectionCard(title = "Related Files") {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        layer.relatedFiles.forEach { file ->
                            FilePathChip(path = file)
                        }
                    }
                }
            }

            if (layer?.risks?.isNotEmpty() == true) {
                DetailSectionCard(title = "Risk Signals") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        layer.risks.forEach { risk ->
                            DetailBullet(text = risk, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NodeOverviewCard(
    layer: ArchitectureLayer?,
    fallbackText: String
) {
    DetailSectionCard(title = "Overview") {
        Text(
            text = layer?.responsibility ?: fallbackText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f)
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ArchitectureSubsection(title = title)
            content()
        }
    }
}

@Composable
private fun DetailBullet(
    text: String,
    color: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun GraphConnectionRow(connection: GraphConnectionDetail) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = connection.nodeLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (connection.label.isNotBlank()) {
                Text(
                    text = connection.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class GraphConnectionDetail(
    val nodeLabel: String,
    val label: String
)

@Composable
private fun ArchitectureListMode(
    architecture: Architecture,
    paddingValues: PaddingValues
) {
    var heroExpanded by remember { mutableStateOf(false) }
    val insightTabs = remember(architecture.strengths, architecture.concerns, architecture.recommendations) {
        buildList {
            if (architecture.strengths.isNotEmpty()) {
                add(ArchitectureInsightTab("Strengths", ArchitectureInsightType.Strengths))
            }
            if (architecture.concerns.isNotEmpty()) {
                add(ArchitectureInsightTab("Concerns", ArchitectureInsightType.Concerns))
            }
            if (architecture.recommendations.isNotEmpty()) {
                add(ArchitectureInsightTab("Recommendations", ArchitectureInsightType.Recommendations))
            }
        }
    }
    var selectedInsightTab by remember(insightTabs) { mutableStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            ArchitectureHeroCard(
                architecture = architecture,
                expanded = heroExpanded,
                onToggleExpanded = { heroExpanded = !heroExpanded }
            )
        }

        architecture.dependencyDirection?.let { dependencyDirection ->
            item {
                ArchitectureMetaCard(
                    title = "Dependency Direction",
                    content = dependencyDirection
                )
            }
        }

        if (architecture.layers.isNotEmpty()) {
            item { ArchitectureSectionTitle("Architecture Layers") }
            items(architecture.layers) { layer ->
                ArchitectureLayerCard(layer = layer)
            }
        }

        if (architecture.dataFlow.isNotEmpty()) {
            item { ArchitectureSectionTitle("Data Flow") }
            items(architecture.dataFlow) { flow ->
                DataFlowCard(flow = flow)
            }
        }

        if (insightTabs.isNotEmpty()) {
            item { ArchitectureSectionTitle("Insights") }
            item {
                ArchitectureInsightsTabs(
                    tabs = insightTabs,
                    selectedIndex = selectedInsightTab,
                    onTabSelected = { selectedInsightTab = it },
                    architecture = architecture
                )
            }
        }
    }
}

@Composable
private fun ArchitectureInsightsTabs(
    tabs: List<ArchitectureInsightTab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    architecture: Architecture
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SecondaryTabRow(
                selectedTabIndex = selectedIndex,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = index == selectedIndex,
                        onClick = { onTabSelected(index) },
                        text = {
                            Text(
                                text = tab.title,
                                maxLines = 1
                            )
                        }
                    )
                }
            }

            when (tabs.getOrNull(selectedIndex)?.type) {
                ArchitectureInsightType.Strengths -> HighlightListCard(
                    items = architecture.strengths,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    icon = Icons.Default.CheckCircle
                )

                ArchitectureInsightType.Concerns -> HighlightListCard(
                    items = architecture.concerns,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    icon = Icons.Default.Warning
                )

                ArchitectureInsightType.Recommendations -> Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    architecture.recommendations.forEach { recommendation ->
                        RecommendationStrip(text = recommendation)
                    }
                }

                null -> Unit
            }
        }
    }
}

private data class ArchitectureInsightTab(
    val title: String,
    val type: ArchitectureInsightType
)

private enum class ArchitectureInsightType {
    Strengths,
    Concerns,
    Recommendations
}

@Composable
private fun ArchitectureHeroCard(
    architecture: Architecture,
    expanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    Card(
        onClick = onToggleExpanded,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(
                        imageVector = Icons.Default.Architecture,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = architecture.style,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (expanded) "Tap to collapse summary" else "Tap to expand summary",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.74f)
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = architecture.summary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.86f)
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.58f),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = "${architecture.layers.size} layers • ${architecture.dataFlow.size} flow paths",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ArchitectureMetaCard(
    title: String,
    content: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ArchitectureSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun ArchitectureLayerCard(layer: ArchitectureLayer) {
    var expanded by remember(layer.name) { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(
                        imageVector = Icons.Default.Architecture,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = layer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = layer.responsibility,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (expanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = !expanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "Tap to view patterns, related files, and risk signals",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (layer.commonPatterns.isNotEmpty()) {
                        ArchitectureSubsection(title = "Common Patterns")
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            layer.commonPatterns.forEach { pattern ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = pattern,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    if (layer.relatedFiles.isNotEmpty()) {
                        ArchitectureSubsection(title = "Related Files")
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            layer.relatedFiles.take(6).forEach { file ->
                                FilePathChip(path = file)
                            }
                        }
                    }

                    if (layer.risks.isNotEmpty()) {
                        ArchitectureSubsection(title = "Risk Signals")
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            layer.risks.forEach { risk ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .size(16.dp)
                                    )
                                    Text(
                                        text = risk,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchitectureSubsection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun DataFlowCard(flow: DataFlow) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                FlowEndpoint(
                    title = flow.from,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                FlowArrow(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
                FlowEndpoint(
                    title = flow.to,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = flow.description,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun FlowEndpoint(
    title: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = accentColor.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Circle,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(10.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FlowArrow(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "->",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = "Flow",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun HighlightListCard(
    items: List<String>,
    containerColor: Color,
    contentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items.forEach { item ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(18.dp)
                    )
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyLarge,
                        color = contentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendationStrip(text: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(18.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Made with Bob

