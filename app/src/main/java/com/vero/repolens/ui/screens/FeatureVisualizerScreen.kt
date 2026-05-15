package com.vero.repolens.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.vero.repolens.data.models.ArchitectureLayer
import com.vero.repolens.data.models.ExistingTest
import com.vero.repolens.data.models.Feature
import com.vero.repolens.data.models.Module
import com.vero.repolens.data.models.RepoLensReport
import com.vero.repolens.data.models.Risk
import com.vero.repolens.ui.components.ComplexityBadge
import com.vero.repolens.ui.components.ConfidenceBadge
import com.vero.repolens.ui.components.SeverityBadge
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class VisualizerNodeType {
    FEATURE,
    MODULE,
    FILE,
    TEST,
    RISK,
    LAYER
}

private enum class VisualizerFilter(val label: String, val type: VisualizerNodeType?) {
    ALL("All", null),
    MODULES("Modules", VisualizerNodeType.MODULE),
    FILES("Files", VisualizerNodeType.FILE),
    TESTS("Tests", VisualizerNodeType.TEST),
    RISKS("Risks", VisualizerNodeType.RISK),
    LAYERS("Layers", VisualizerNodeType.LAYER)
}

private data class VisualizerNode(
    val id: String,
    val title: String,
    val type: VisualizerNodeType,
    val subtitle: String,
    val summary: String,
    val severity: String? = null,
    val chips: List<String> = emptyList(),
    val evidence: List<String> = emptyList(),
    val actionLabel: String? = null,
    val actionTarget: String? = null
)

private data class VisualizerEdge(
    val fromId: String,
    val toId: String,
    val weight: Float = 1f,
    val inferred: Boolean = false
)

private data class SignalMetric(
    val label: String,
    val value: Int,
    val supporting: String
)

private data class FeatureVisualizerModel(
    val featureNodeId: String,
    val nodes: List<VisualizerNode>,
    val edges: List<VisualizerEdge>,
    val insightCards: List<Pair<String, String>>,
    val signals: List<SignalMetric>,
    val focusAreas: List<Pair<String, List<String>>>
)

private val OffsetSaver = listSaver<Offset, Float>(
    save = { listOf(it.x, it.y) },
    restore = { values ->
        Offset(
            x = values.getOrElse(0) { 0f },
            y = values.getOrElse(1) { 0f }
        )
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureVisualizerScreen(
    report: RepoLensReport,
    feature: Feature,
    onNavigateBack: () -> Unit,
    onNavigateToModule: (String) -> Unit
) {
    val model = remember(report, feature) { buildFeatureVisualizerModel(report, feature) }
    var activeFilter by rememberSaveable { mutableStateOf(VisualizerFilter.ALL.name) }
    var selectedNodeId by rememberSaveable { mutableStateOf(model.featureNodeId) }
    val currentFilter = remember(activeFilter) { VisualizerFilter.valueOf(activeFilter) }
    val selectedNode = remember(model, selectedNodeId) {
        model.nodes.firstOrNull { it.id == selectedNodeId } ?: model.nodes.first { it.id == model.featureNodeId }
    }
    val connectedTitles = remember(model, selectedNode) {
        val neighbors = model.edges.flatMap { edge ->
            when (selectedNode.id) {
                edge.fromId -> listOf(edge.toId)
                edge.toId -> listOf(edge.fromId)
                else -> emptyList()
            }
        }.distinct()
        neighbors.mapNotNull { neighborId -> model.nodes.firstOrNull { it.id == neighborId }?.title }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feature Visualizer") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FeatureVisualizerHero(feature = feature, model = model)
            }

            item {
                VisualizerFilterBar(
                    selectedFilter = currentFilter,
                    onFilterSelected = { activeFilter = it.name }
                )
            }

            item {
                FeatureGraphCard(
                    model = model,
                    activeFilter = currentFilter,
                    selectedNodeId = selectedNode.id,
                    onNodeSelected = { selectedNodeId = it }
                )
            }

            item {
                SignalBoard(metrics = model.signals)
            }

            item {
                FocusNodeCard(
                    node = selectedNode,
                    connections = connectedTitles,
                    onNavigateToModule = onNavigateToModule
                )
            }

            items(model.insightCards) { insight ->
                InsightCard(
                    title = insight.first,
                    body = insight.second
                )
            }

            items(model.focusAreas) { section ->
                EvidenceCard(
                    title = section.first,
                    items = section.second
                )
            }
        }
    }
}

@Composable
private fun FeatureVisualizerHero(
    feature: Feature,
    model: FeatureVisualizerModel
) {
    val readiness = model.signals.firstOrNull { it.label == "Readiness" }?.value ?: 0

    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.98f),
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.88f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                        )
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = feature.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = feature.businessPurpose ?: feature.description,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                    )
                }
                SeverityBadge(severity = feature.riskLevel)
            }

            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HeroPill("$readiness readiness")
                    HeroPill("${feature.coreFiles.size + feature.entryPoints.size} key files")
                    HeroPill("${feature.risks.size} direct risks")
                    if (feature.navigationRoutes.isNotEmpty()) {
                        HeroPill("${feature.navigationRoutes.size} routes")
                    }
                    if (feature.analyticsEvents.isNotEmpty()) {
                        HeroPill("${feature.analyticsEvents.size} analytics")
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroPill(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
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

@Composable
private fun VisualizerFilterBar(
    selectedFilter: VisualizerFilter,
    onFilterSelected: (VisualizerFilter) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        VisualizerFilter.values().forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label) }
            )
        }
    }
}

@Composable
private fun FeatureGraphCard(
    model: FeatureVisualizerModel,
    activeFilter: VisualizerFilter,
    selectedNodeId: String,
    onNodeSelected: (String) -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Impact map",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Tap a node to inspect its role, connected evidence, and blast radius around this feature.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val visibleNodes = remember(model, activeFilter) {
                model.nodes.filter { node ->
                    node.type == VisualizerNodeType.FEATURE || activeFilter.type == null || node.type == activeFilter.type
                }
            }
            val visibleNodeIds = visibleNodes.map { it.id }.toSet()
            val visibleEdges = remember(model, visibleNodeIds) {
                model.edges.filter { it.fromId in visibleNodeIds && it.toId in visibleNodeIds }
            }
            val ringColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
            val selectedEdgeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.56f)
            val defaultEdgeColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
            val inferredEdgeColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            var panOffset by rememberSaveable(stateSaver = OffsetSaver) { mutableStateOf(Offset.Zero) }
            var zoomScale by rememberSaveable { mutableStateOf(1f) }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(560.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            )
                        )
                    )
            ) {
                val density = LocalDensity.current
                val viewportWidthPx = with(density) { maxWidth.toPx() }
                val viewportHeightPx = with(density) { maxHeight.toPx() }
                val graphWidthPx = max(viewportWidthPx * 1.8f, viewportWidthPx + 320f)
                val graphHeightPx = max(viewportHeightPx * 1.4f, viewportHeightPx + 220f)
                val scaledGraphWidthPx = graphWidthPx * zoomScale
                val scaledGraphHeightPx = graphHeightPx * zoomScale
                val maxPanX = max(0f, (scaledGraphWidthPx - viewportWidthPx) / 2f)
                val maxPanY = max(0f, (scaledGraphHeightPx - viewportHeightPx) / 2f)
                val clampedPanOffset = Offset(
                    x = panOffset.x.coerceIn(-maxPanX, maxPanX),
                    y = panOffset.y.coerceIn(-maxPanY, maxPanY)
                )

                val positions = remember(visibleNodes, graphWidthPx, graphHeightPx) {
                    computeGraphPositions(
                        nodes = visibleNodes,
                        widthPx = graphWidthPx,
                        heightPx = graphHeightPx
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(graphWidthPx, graphHeightPx) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                panOffset = Offset(
                                    x = (panOffset.x + dragAmount.x).coerceIn(-maxPanX, maxPanX),
                                    y = (panOffset.y + dragAmount.y).coerceIn(-maxPanY, maxPanY)
                                )
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationX = clampedPanOffset.x
                                translationY = clampedPanOffset.y
                                scaleX = zoomScale
                                scaleY = zoomScale
                                transformOrigin = TransformOrigin(0.5f, 0.5f)
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(graphWidthPx / 2f, graphHeightPx / 2f)
                            val ringBase = min(graphWidthPx, graphHeightPx) * 0.20f

                            repeat(3) { index ->
                                drawCircle(
                                    color = ringColor.copy(alpha = 0.06f - (index * 0.012f)),
                                    radius = ringBase + index * ringBase * 0.48f,
                                    center = center,
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }

                            visibleEdges.forEach { edge ->
                                val from = positions[edge.fromId] ?: return@forEach
                                val to = positions[edge.toId] ?: return@forEach
                                val isSelected = edge.fromId == selectedNodeId || edge.toId == selectedNodeId
                                drawLine(
                                    color = if (isSelected) {
                                        selectedEdgeColor
                                    } else {
                                        if (edge.inferred) inferredEdgeColor else defaultEdgeColor
                                    },
                                    start = from,
                                    end = to,
                                    strokeWidth = if (isSelected) 4.dp.toPx() else edge.weight * 2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }

                        visibleNodes.forEach { node ->
                            val position = positions[node.id] ?: return@forEach
                            val nodeWidthPx = with(density) {
                                if (node.type == VisualizerNodeType.FEATURE) 196.dp.toPx() else 148.dp.toPx()
                            }
                            val nodeHeightPx = with(density) {
                                if (node.type == VisualizerNodeType.FEATURE) 132.dp.toPx() else 86.dp.toPx()
                            }

                            GraphNodeBubble(
                                node = node,
                                selected = node.id == selectedNodeId,
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            x = (position.x - nodeWidthPx / 2f).roundToInt(),
                                            y = (position.y - nodeHeightPx / 2f).roundToInt()
                                        )
                                    }
                                    .width(if (node.type == VisualizerNodeType.FEATURE) 196.dp else 148.dp)
                                    .height(if (node.type == VisualizerNodeType.FEATURE) 132.dp else 86.dp),
                                onClick = { onNodeSelected(node.id) }
                            )
                        }
                    }

                    ZoomControls(
                        zoomScale = zoomScale,
                        onZoomIn = {
                            zoomScale = (zoomScale + 0.15f).coerceAtMost(2.1f)
                        },
                        onZoomOut = {
                            zoomScale = (zoomScale - 0.15f).coerceAtLeast(0.85f)
                            panOffset = Offset(
                                x = panOffset.x.coerceIn(-maxPanX, maxPanX),
                                y = panOffset.y.coerceIn(-maxPanY, maxPanY)
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    )
                }
            }

            LegendRow()
        }
    }
}

@Composable
private fun GraphNodeBubble(
    node: VisualizerNode,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(targetValue = if (selected) 1.04f else 1f, label = "nodeScale")
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) {
            nodeTone(node.type, node.severity).copy(alpha = 0.99f)
        } else {
            nodeTone(node.type, node.severity).copy(alpha = 0.94f)
        },
        label = "nodeBackground"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            Color(0xFF4B5E7A)
        } else {
            Color(0xFFB7C6D9)
        },
        label = "nodeBorder"
    )
    val nodeTextColor = Color(0xFF243242)
    val nodeSubtitleColor = Color(0xFF5B6A7A)

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(if (selected) 14.dp else 4.dp, MaterialTheme.shapes.large)
            .clickable(onClick = onClick),
        shape = if (node.type == VisualizerNodeType.FEATURE) MaterialTheme.shapes.extraLarge else MaterialTheme.shapes.large,
        color = backgroundColor,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    imageVector = iconForNode(node.type),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = node.title,
                    style = if (node.type == VisualizerNodeType.FEATURE) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.titleSmall
                    },
                    fontWeight = FontWeight.SemiBold,
                    maxLines = if (node.type == VisualizerNodeType.FEATURE) 2 else 2,
                    overflow = TextOverflow.Ellipsis,
                    color = nodeTextColor
                )
                Text(
                    text = node.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = nodeSubtitleColor
                )
            }
        }
    }
}

@Composable
private fun ZoomControls(
    zoomScale: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f))
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = onZoomIn) {
                Icon(Icons.Default.Add, contentDescription = "Zoom in")
            }
            Text(
                text = "${(zoomScale * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(onClick = onZoomOut) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom out")
            }
        }
    }
}

@Composable
private fun LegendRow() {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LegendPill("Feature", nodeTone(VisualizerNodeType.FEATURE, null))
        LegendPill("Modules", nodeTone(VisualizerNodeType.MODULE, null))
        LegendPill("Files", nodeTone(VisualizerNodeType.FILE, null))
        LegendPill("Tests", nodeTone(VisualizerNodeType.TEST, null))
        LegendPill("Risks", nodeTone(VisualizerNodeType.RISK, "high"))
        LegendPill("Layers", nodeTone(VisualizerNodeType.LAYER, null))
    }
}

@Composable
private fun LegendPill(
    text: String,
    tone: Color
) {
    Surface(
        color = tone.copy(alpha = 0.68f),
        shape = MaterialTheme.shapes.large
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SignalBoard(metrics: List<SignalMetric>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Signal board",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            metrics.forEach { metric ->
                SignalCard(metric = metric)
            }
        }
    }
}

@Composable
private fun SignalCard(metric: SignalMetric) {
    Card(
        modifier = Modifier.width(164.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = metric.label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${metric.value}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            LinearProgressIndicator(
                progress = { metric.value / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
            )
            Text(
                text = metric.supporting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FocusNodeCard(
    node: VisualizerNode,
    connections: List<String>,
    onNavigateToModule: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = node.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = node.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                node.severity?.let { SeverityBadge(severity = it) }
            }

            Text(
                text = node.summary,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (node.chips.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    node.chips.forEach { chip ->
                        HeroPill(chip)
                    }
                }
            }

            if (connections.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Connected to",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        connections.take(10).forEach { title ->
                            HeroPill(title)
                        }
                    }
                }
            }

            if (node.evidence.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Evidence",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    node.evidence.take(6).forEach { item ->
                        EvidenceLine(item)
                    }
                }
            }

            if (node.actionLabel != null && node.actionTarget != null) {
                TextButton(onClick = { onNavigateToModule(node.actionTarget) }) {
                    Text(node.actionLabel)
                }
            }
        }
    }
}

@Composable
private fun EvidenceLine(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InsightCard(
    title: String,
    body: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EvidenceCard(
    title: String,
    items: List<String>
) {
    if (items.isEmpty()) return

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            items.forEach { item ->
                EvidenceLine(item)
            }
        }
    }
}

private fun buildFeatureVisualizerModel(
    report: RepoLensReport,
    feature: Feature
): FeatureVisualizerModel {
    val featureNodeId = "feature:${feature.id}"
    val keyFiles = (feature.entryPoints.map { "Entry point: $it" } + feature.coreFiles.map { "Core file: $it" })
        .distinct()
    val filePaths = (feature.entryPoints + feature.coreFiles).distinct()

    val moduleMatches = report.modules.mapNotNull { module ->
        val score = moduleMatchScore(module, feature)
        if (score > 0) module to score else null
    }.sortedByDescending { it.second }
        .take(4)

    val layerMatches = report.architecture.layers.mapNotNull { layer ->
        val score = layerMatchScore(layer, filePaths, feature)
        if (score > 0) layer to score else null
    }.sortedByDescending { it.second }
        .take(3)

    val matchedModuleNames = moduleMatches.map { it.first.name } + moduleMatches.map { it.first.id }
    val risks = (feature.risks + report.risks.filter { risk ->
        risk.id !in feature.risks.map { it.id }.toSet() && riskMatchesFeature(risk, feature, matchedModuleNames, filePaths)
    }).distinctBy { it.id.ifBlank { it.title } }
        .sortedByDescending { severityRank(it.severity) }
        .take(4)

    val tests = report.testing?.existingTests.orEmpty()
        .mapNotNull { test ->
            val score = testMatchScore(test, feature, filePaths)
            if (score > 0) test to score else null
        }
        .sortedByDescending { it.second }
        .take(4)

    val fileNodes = filePaths.take(5)
    val fallbackTests = if (tests.isEmpty()) feature.existingTests.take(3) else emptyList()

    val nodes = buildList {
        add(
            VisualizerNode(
                id = featureNodeId,
                title = feature.name,
                type = VisualizerNodeType.FEATURE,
                subtitle = "Feature center",
                summary = feature.description,
                severity = feature.riskLevel,
                chips = listOf(
                    feature.complexity,
                    "${(feature.confidence * 100).roundToInt()} confidence",
                    "${filePaths.size} files",
                    "${feature.existingTests.size} tests"
                ),
                evidence = buildList {
                    feature.businessPurpose?.let { add(it) }
                    addAll(feature.screens.take(3).map { "Screen: $it" })
                    addAll(feature.navigationRoutes.take(2).map { "Route: $it" })
                    addAll(feature.analyticsEvents.take(2).map { "Analytics: $it" })
                }
            )
        )

        moduleMatches.forEach { (module, score) ->
            add(
                VisualizerNode(
                    id = "module:${module.id}",
                    title = module.name,
                    type = VisualizerNodeType.MODULE,
                    subtitle = "${module.type.replaceFirstChar { it.uppercase() }} module",
                    summary = module.responsibility,
                    severity = module.riskLevel,
                    chips = listOf(
                        "$score link score",
                        "${module.dependencies.size} deps",
                        "${module.importantFiles.size} important files"
                    ),
                    evidence = buildList {
                        module.path?.let { add("Path: $it") }
                        addAll(module.keyPackages.take(2).map { "Package: $it" })
                        addAll(module.publicApis.take(2).map { "API: $it" })
                    },
                    actionLabel = "Open module",
                    actionTarget = module.id
                )
            )
        }

        layerMatches.forEach { (layer, score) ->
            add(
                VisualizerNode(
                    id = "layer:${layer.id}",
                    title = layer.name,
                    type = VisualizerNodeType.LAYER,
                    subtitle = "Architecture layer",
                    summary = layer.responsibility,
                    chips = listOf(
                        "$score link score",
                        "${layer.relatedFiles.size} related files",
                        "${layer.risks.size} concerns"
                    ),
                    evidence = layer.commonPatterns.take(4)
                )
            )
        }

        fileNodes.forEach { file ->
            add(
                VisualizerNode(
                    id = "file:$file",
                    title = file.substringAfterLast('/').substringAfterLast('\\'),
                    type = VisualizerNodeType.FILE,
                    subtitle = if (file in feature.entryPoints) "Entry file" else "Core file",
                    summary = file,
                    chips = listOf(if (file in feature.entryPoints) "entry point" else "core path"),
                    evidence = buildList {
                        moduleMatches.firstOrNull { belongsToModule(file, it.first) }?.first?.name?.let {
                            add("Most likely owned by $it")
                        }
                    }
                )
            )
        }

        tests.forEach { (test, score) ->
            add(
                VisualizerNode(
                    id = "test:${test.name}",
                    title = test.name,
                    type = VisualizerNodeType.TEST,
                    subtitle = "${test.type.replaceFirstChar { it.uppercase() }} test",
                    summary = test.path ?: "Coverage trace tied to this feature.",
                    chips = listOf(
                        "$score link score",
                        "${test.covers.size} coverage links",
                        "${test.gaps.size} gaps"
                    ),
                    evidence = buildList {
                        test.path?.let { add("Path: $it") }
                        addAll(test.covers.take(3).map { "Covers: $it" })
                        addAll(test.gaps.take(2).map { "Gap: $it" })
                    }
                )
            )
        }

        fallbackTests.forEach { testName ->
            add(
                VisualizerNode(
                    id = "test:$testName",
                    title = testName,
                    type = VisualizerNodeType.TEST,
                    subtitle = "Feature-linked test",
                    summary = "This test is listed directly on the feature, but no richer test metadata was found in the report.",
                    chips = listOf("direct mapping")
                )
            )
        }

        risks.forEach { risk ->
            add(
                VisualizerNode(
                    id = "risk:${risk.id.ifBlank { risk.title }}",
                    title = risk.title,
                    type = VisualizerNodeType.RISK,
                    subtitle = risk.category.replaceFirstChar { it.uppercase() },
                    summary = risk.whyItMatters ?: risk.recommendation ?: "Risk signal connected to this feature.",
                    severity = risk.severity,
                    chips = buildList {
                        add(risk.category)
                        risk.confidence?.let { add("${(it * 100).roundToInt()} confidence") }
                        if (risk.affectedModules.isNotEmpty()) add("${risk.affectedModules.size} modules")
                        if (risk.affectedFiles.isNotEmpty()) add("${risk.affectedFiles.size} files")
                    },
                    evidence = buildList {
                        addAll(risk.affectedModules.take(3).map { "Module: $it" })
                        addAll(risk.affectedFiles.take(3).map { "File: $it" })
                        risk.suggestedTest?.let { add("Suggested test: $it") }
                        risk.recommendation?.let { add("Recommendation: $it") }
                    }
                )
            )
        }
    }

    val edges = buildList {
        nodes.filterNot { it.id == featureNodeId }.forEach { node ->
            add(
                VisualizerEdge(
                    fromId = featureNodeId,
                    toId = node.id,
                    weight = when (node.type) {
                        VisualizerNodeType.MODULE, VisualizerNodeType.LAYER -> 1.8f
                        VisualizerNodeType.RISK -> 1.5f
                        else -> 1.2f
                    },
                    inferred = node.type == VisualizerNodeType.MODULE || node.type == VisualizerNodeType.LAYER
                )
            )
        }

        fileNodes.forEach { file ->
            moduleMatches.forEach { (module, _) ->
                if (belongsToModule(file, module)) {
                    add(VisualizerEdge("module:${module.id}", "file:$file", weight = 1.1f))
                }
            }
            layerMatches.forEach { (layer, _) ->
                if (layer.relatedFiles.any { samePath(it, file) }) {
                    add(VisualizerEdge("layer:${layer.id}", "file:$file", weight = 1.05f))
                }
            }
        }

        tests.forEach { (test, _) ->
            fileNodes.forEach { file ->
                if (test.covers.any { matchesLoose(it, file) } || samePath(test.path.orEmpty(), file)) {
                    add(VisualizerEdge("test:${test.name}", "file:$file", weight = 1f, inferred = true))
                }
            }
        }

        risks.forEach { risk ->
            val riskId = "risk:${risk.id.ifBlank { risk.title }}"
            fileNodes.forEach { file ->
                if (risk.affectedFiles.any { samePath(it, file) }) {
                    add(VisualizerEdge(riskId, "file:$file", weight = 1.25f))
                }
            }
            moduleMatches.forEach { (module, _) ->
                if (risk.affectedModules.any { matchesLoose(it, module.id) || matchesLoose(it, module.name) }) {
                    add(VisualizerEdge(riskId, "module:${module.id}", weight = 1.35f))
                }
            }
        }
    }.distinctBy { "${it.fromId}:${it.toId}" }

    val readiness = computeReadinessScore(feature, moduleMatches.size, layerMatches.size, risks.size)
    val coverage = computeCoverageScore(feature)
    val blastRadius = computeBlastRadiusScore(fileNodes.size, moduleMatches.size, layerMatches.size, risks.size)
    val confidence = (feature.confidence * 100).roundToInt()

    val focusAreas = listOf(
        "Experience Surface" to buildList {
            addAll(feature.screens.map { "Screen: $it" })
            addAll(feature.uiComponents.take(5).map { "UI: $it" })
            addAll(feature.navigationRoutes.map { "Route: $it" })
        },
        "State And Logic" to buildList {
            addAll(feature.viewModels.map { "ViewModel: $it" })
            addAll(feature.useCases.map { "Use case: $it" })
            addAll(feature.repositories.map { "Repository: $it" })
            feature.stateModel?.stateClass?.let { add("State class: $it") }
            addAll(feature.stateModel?.events.orEmpty().take(4).map { "Event: $it" })
        },
        "Validation Gaps" to buildList {
            addAll(feature.missingTests.map { "Missing test: $it" })
            addAll(
                report.testing?.missingTestAreas.orEmpty()
                    .filter { matchesLoose(it.area, feature.name) || feature.missingTests.any { missing -> matchesLoose(it.area, missing) } }
                    .take(3)
                    .map { "${it.area} (${it.priority})" }
            )
            addAll(feature.suggestedPrChecklist.take(4).map { "Checklist: $it" })
        }
    ).filter { it.second.isNotEmpty() }

    val insightCards = buildList {
        add(
            "Blast radius" to
                "This feature touches ${moduleMatches.size} modules, ${layerMatches.size} architecture layers, and ${fileNodes.size} high-signal files. Changes here are likely to ripple across both UI and data flow."
        )
        if (feature.missingTests.isNotEmpty()) {
            add(
                "Testing pressure" to
                    "There are ${feature.missingTests.size} named testing gaps for this feature. The graph highlights existing tests, but the missing set suggests confidence could still regress during larger refactors."
            )
        }
        if (risks.isNotEmpty()) {
            add(
                "Risk concentration" to
                    "The strongest risk signal is ${risks.first().title.lowercase()}. Most risk comes from ${risks.flatMap { it.affectedModules }.distinct().take(2).joinToString(", ").ifBlank { "file-level interactions" }}."
            )
        }
        if (layerMatches.size > 1) {
            add(
                "Architectural spread" to
                    "This feature crosses ${layerMatches.size} layers, which usually means more coordination cost for changes and a higher chance of subtle regressions when behavior shifts."
            )
        }
    }

    return FeatureVisualizerModel(
        featureNodeId = featureNodeId,
        nodes = nodes,
        edges = edges,
        insightCards = insightCards,
        signals = listOf(
            SignalMetric("Readiness", readiness, readinessSummary(readiness)),
            SignalMetric("Coverage", coverage, "${feature.existingTests.size} tests, ${feature.missingTests.size} gaps"),
            SignalMetric("Blast Radius", blastRadius, "${moduleMatches.size} modules and ${fileNodes.size} files in play"),
            SignalMetric("Confidence", confidence, "Based on the feature's reported confidence signal")
        ),
        focusAreas = focusAreas
    )
}

private fun computeGraphPositions(
    nodes: List<VisualizerNode>,
    widthPx: Float,
    heightPx: Float
): Map<String, Offset> {
    val center = Offset(widthPx / 2f, heightPx / 2f)
    val ringOne = min(widthPx, heightPx) * 0.27f
    val ringTwo = min(widthPx, heightPx) * 0.42f
    val ringThree = min(widthPx, heightPx) * 0.56f

    val groups = nodes.groupBy { it.type }
    val positions = mutableMapOf<String, Offset>()
    groups[VisualizerNodeType.FEATURE]?.firstOrNull()?.let { positions[it.id] = center }

    placeSector(groups[VisualizerNodeType.MODULE].orEmpty(), 190f, 310f, ringOne, center, positions)
    placeSector(groups[VisualizerNodeType.LAYER].orEmpty(), 320f, 40f, ringOne, center, positions)
    placeSector(groups[VisualizerNodeType.FILE].orEmpty(), 115f, 240f, ringTwo, center, positions)
    placeSector(groups[VisualizerNodeType.TEST].orEmpty(), 10f, 110f, ringTwo, center, positions)
    placeSector(groups[VisualizerNodeType.RISK].orEmpty(), 110f, 430f, ringThree, center, positions)

    return positions
}

private fun placeSector(
    nodes: List<VisualizerNode>,
    startAngle: Float,
    endAngle: Float,
    radius: Float,
    center: Offset,
    positions: MutableMap<String, Offset>
) {
    if (nodes.isEmpty()) return
    val adjustedEnd = if (endAngle <= startAngle) endAngle + 360f else endAngle
    val sweep = adjustedEnd - startAngle
    val step = if (nodes.size == 1) 0f else sweep / (nodes.size - 1)

    nodes.forEachIndexed { index, node ->
        val angle = startAngle + (index * step)
        val radians = angle / 180f * PI.toFloat()
        positions[node.id] = Offset(
            x = center.x + cos(radians) * radius,
            y = center.y + sin(radians) * radius
        )
    }
}

private fun computeReadinessScore(
    feature: Feature,
    moduleCount: Int,
    layerCount: Int,
    riskCount: Int
): Int {
    val directPenalty = feature.risks.sumOf { riskPenalty(it.severity) }
    val indirectPenalty = riskCount * 4
    val missingPenalty = feature.missingTests.size * 6
    val spreadPenalty = maxOf(0, moduleCount - 2) * 5 + maxOf(0, layerCount - 1) * 4
    val confidenceBoost = (feature.confidence * 12).roundToInt()
    val testBoost = min(12, feature.existingTests.size * 3)

    return (100 - directPenalty - indirectPenalty - missingPenalty - spreadPenalty + confidenceBoost + testBoost)
        .coerceIn(18, 98)
}

private fun computeCoverageScore(feature: Feature): Int {
    val total = feature.existingTests.size + feature.missingTests.size
    if (total == 0) return 42
    return ((feature.existingTests.size.toFloat() / total.toFloat()) * 100f).roundToInt().coerceIn(12, 100)
}

private fun computeBlastRadiusScore(
    fileCount: Int,
    moduleCount: Int,
    layerCount: Int,
    riskCount: Int
): Int {
    return (fileCount * 8 + moduleCount * 16 + layerCount * 12 + riskCount * 10).coerceIn(20, 100)
}

private fun readinessSummary(score: Int): String = when {
    score >= 80 -> "Healthy release posture"
    score >= 60 -> "Good, but worth a review pass"
    score >= 40 -> "Caution: change surface is wide"
    else -> "Risky: needs focused hardening"
}

private fun moduleMatchScore(
    module: Module,
    feature: Feature
): Int {
    val fileMatches = (feature.coreFiles + feature.entryPoints).count { file ->
        belongsToModule(file, module) || module.importantFiles.any { samePath(it, file) }
    }
    val namedSignals = (feature.dependencies + feature.viewModels + feature.useCases + feature.repositories + feature.screens)
        .count { token ->
            matchesLoose(module.name, token) ||
                matchesLoose(module.id, token) ||
                matchesLoose(module.path.orEmpty(), token) ||
                module.keyPackages.any { matchesLoose(it, token) } ||
                module.publicApis.any { matchesLoose(it, token) }
        }
    val riskSignals = feature.risks.count { risk ->
        risk.affectedModules.any { matchesLoose(it, module.id) || matchesLoose(it, module.name) }
    }
    return fileMatches * 4 + namedSignals * 2 + riskSignals * 3
}

private fun layerMatchScore(
    layer: ArchitectureLayer,
    filePaths: List<String>,
    feature: Feature
): Int {
    val fileMatches = filePaths.count { file -> layer.relatedFiles.any { samePath(it, file) } }
    val nameSignals = (feature.screens + feature.viewModels + feature.repositories + feature.useCases)
        .count { token -> matchesLoose(layer.name, token) || matchesLoose(layer.responsibility, token) }
    val riskSignals = layer.risks.count { risk -> feature.risks.any { matchesLoose(risk, it.title) } }
    return fileMatches * 4 + nameSignals * 2 + riskSignals * 2
}

private fun testMatchScore(
    test: ExistingTest,
    feature: Feature,
    filePaths: List<String>
): Int {
    val directMatch = feature.existingTests.count { matchesLoose(it, test.name) || matchesLoose(it, test.path.orEmpty()) }
    val coverMatch = test.covers.count { cover ->
        matchesLoose(cover, feature.name) ||
            feature.screens.any { matchesLoose(cover, it) } ||
            filePaths.any { matchesLoose(cover, it) }
    }
    val pathMatch = filePaths.count { file -> samePath(file, test.path.orEmpty()) }
    return directMatch * 5 + coverMatch * 3 + pathMatch * 4
}

private fun riskMatchesFeature(
    risk: Risk,
    feature: Feature,
    matchedModuleNames: List<String>,
    filePaths: List<String>
): Boolean {
    return risk.affectedFiles.any { riskFile -> filePaths.any { samePath(riskFile, it) } } ||
        risk.affectedModules.any { affectedModule ->
            matchedModuleNames.any { matchedModule -> matchesLoose(affectedModule, matchedModule) }
        } ||
        matchesLoose(risk.title, feature.name) ||
        feature.screens.any { matchesLoose(risk.title, it) } ||
        feature.dependencies.any { matchesLoose(risk.whyItMatters.orEmpty(), it) }
}

private fun iconForNode(type: VisualizerNodeType) = when (type) {
    VisualizerNodeType.FEATURE -> Icons.Default.Star
    VisualizerNodeType.MODULE -> Icons.Default.Folder
    VisualizerNodeType.FILE -> Icons.Default.Description
    VisualizerNodeType.TEST -> Icons.Default.Science
    VisualizerNodeType.RISK -> Icons.Default.Warning
    VisualizerNodeType.LAYER -> Icons.Default.Layers
}

private fun nodeTone(
    type: VisualizerNodeType,
    severity: String?
): Color = when (type) {
    VisualizerNodeType.FEATURE -> Color(0xFFD6E7FF)
    VisualizerNodeType.MODULE -> Color(0xFFDDE7FF)
    VisualizerNodeType.FILE -> Color(0xFFE7EBF5)
    VisualizerNodeType.TEST -> Color(0xFFD8F4E4)
    VisualizerNodeType.RISK -> when {
        severity.equals("critical", true) -> Color(0xFFFFC9BF)
        severity.equals("high", true) -> Color(0xFFFFDAB3)
        severity.equals("medium", true) -> Color(0xFFFFEAB9)
        else -> Color(0xFFEFE3CC)
    }
    VisualizerNodeType.LAYER -> Color(0xFFE3D9FF)
}

private fun severityRank(severity: String): Int = when {
    severity.equals("critical", true) -> 4
    severity.equals("high", true) -> 3
    severity.equals("medium", true) -> 2
    severity.equals("low", true) -> 1
    else -> 0
}

private fun riskPenalty(severity: String): Int = when {
    severity.equals("critical", true) -> 18
    severity.equals("high", true) -> 12
    severity.equals("medium", true) -> 6
    severity.equals("low", true) -> 3
    else -> 2
}

private fun belongsToModule(
    file: String,
    module: Module
): Boolean {
    val normalizedFile = normalizeToken(file)
    val modulePath = normalizeToken(module.path.orEmpty())
    return modulePath.isNotBlank() && normalizedFile.contains(modulePath) ||
        matchesLoose(file, module.name) ||
        matchesLoose(file, module.id)
}

private fun samePath(
    left: String,
    right: String
): Boolean {
    val normalizedLeft = normalizeToken(left)
    val normalizedRight = normalizeToken(right)
    return normalizedLeft.isNotBlank() &&
        normalizedRight.isNotBlank() &&
        (normalizedLeft == normalizedRight ||
            normalizedLeft.endsWith(normalizedRight) ||
            normalizedRight.endsWith(normalizedLeft))
}

private fun matchesLoose(
    source: String,
    it: String
): Boolean {
    val left = normalizeToken(source)
    val right = normalizeToken(it)
    if (left.length < 3 || right.length < 3) return false
    return left.contains(right) || right.contains(left)
}

private fun normalizeToken(value: String): String {
    return value
        .lowercase()
        .replace("\\", "/")
        .replace("_", " ")
        .replace("-", " ")
        .trim()
}
