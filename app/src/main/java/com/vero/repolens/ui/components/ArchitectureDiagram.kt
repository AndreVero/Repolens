package com.vero.repolens.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vero.repolens.data.models.ArchitectureDiagram
import com.vero.repolens.data.models.DiagramConnection
import com.vero.repolens.data.models.DiagramNode
import kotlin.math.abs
import kotlin.math.max

private const val NodeWidth = 176f
private const val NodeHeight = 108f
private const val DiagramPadding = 72f
private const val HorizontalNodeGap = 48f
private const val VerticalNodeGap = 42f
private const val MinScale = 0.65f
private const val MaxScale = 2.4f

@Composable
fun ArchitectureDiagram(
    diagram: ArchitectureDiagram,
    selectedNodeId: String?,
    onNodeTap: (DiagramNode) -> Unit,
    onBackgroundTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val laidOutNodes = remember(diagram.nodes) { normalizeNodeLayout(diagram.nodes) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var hasFittedViewport by remember(diagram) { mutableStateOf(false) }

    val textMeasurer = rememberTextMeasurer()
    val colorScheme = MaterialTheme.colorScheme

    fun fitToViewport() {
        val viewport = canvasSize
        if (viewport.width == 0 || viewport.height == 0 || laidOutNodes.isEmpty()) return

        val bounds = diagramBounds(laidOutNodes)
        val availableWidth = viewport.width - DiagramPadding * 2
        val availableHeight = viewport.height - DiagramPadding * 2
        val fitScale = minOf(
            availableWidth / bounds.width,
            availableHeight / bounds.height,
            1.15f
        ).coerceIn(MinScale, MaxScale)

        val scaledCenter = Offset(
            x = (bounds.left + bounds.width / 2f) * fitScale,
            y = (bounds.top + bounds.height / 2f) * fitScale
        )

        scale = fitScale
        offset = Offset(
            x = viewport.width / 2f - scaledCenter.x,
            y = viewport.height / 2f - scaledCenter.y
        )
    }

    LaunchedEffect(diagram, canvasSize) {
        if (!hasFittedViewport && canvasSize != IntSize.Zero) {
            fitToViewport()
            hasFittedViewport = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .onSizeChanged { canvasSize = it }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(diagram) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val previousScale = scale
                        val newScale = (previousScale * zoom).coerceIn(MinScale, MaxScale)
                        val focusPoint = (centroid - offset) / previousScale
                        scale = newScale
                        offset = centroid - (focusPoint * newScale) + pan
                    }
                }
                .pointerInput(diagram, scale, offset) {
                    detectTapGestures { tapOffset ->
                        val diagramPoint = (tapOffset - offset) / scale
                        val tappedNode = laidOutNodes.lastOrNull { node ->
                            nodeRect(node).contains(diagramPoint)
                        }

                        if (tappedNode != null) {
                            onNodeTap(tappedNode)
                        } else {
                            onBackgroundTap()
                        }
                    }
                }
        ) {
            drawStageBackground(
                gridColor = colorScheme.outline.copy(alpha = 0.12f),
                glowColor = colorScheme.primary.copy(alpha = 0.06f)
            )

            val selectedConnections = if (selectedNodeId != null) {
                diagram.connections.filter { it.from == selectedNodeId || it.to == selectedNodeId }.toSet()
            } else {
                emptySet()
            }
            val relatedNodeIds = selectedConnections.flatMapTo(mutableSetOf()) { setOf(it.from, it.to) }

            diagram.connections.forEach { connection ->
                val fromNode = laidOutNodes.find { it.id == connection.from } ?: return@forEach
                val toNode = laidOutNodes.find { it.id == connection.to } ?: return@forEach
                drawConnection(
                    fromNode = fromNode,
                    toNode = toNode,
                    connection = connection,
                    scale = scale,
                    canvasOffset = offset,
                    textMeasurer = textMeasurer,
                    lineColor = when {
                        selectedNodeId == null -> colorScheme.outline.copy(alpha = 0.58f)
                        connection in selectedConnections -> colorScheme.primary.copy(alpha = 0.92f)
                        else -> colorScheme.outline.copy(alpha = 0.22f)
                    },
                    labelContainer = colorScheme.surface,
                    labelTextColor = colorScheme.onSurfaceVariant,
                    emphasized = connection in selectedConnections
                )
            }

            laidOutNodes.sortedBy { it.id == selectedNodeId }.forEach { node ->
                val isSelected = node.id == selectedNodeId
                val isRelated = node.id in relatedNodeIds
                drawNode(
                    node = node,
                    scale = scale,
                    canvasOffset = offset,
                    visualStyle = nodeVisualStyle(node.type, colorScheme.primary, colorScheme.secondary, colorScheme.tertiary),
                    surfaceColor = colorScheme.surface,
                    borderColor = when {
                        isSelected -> colorScheme.primary
                        isRelated -> colorScheme.secondary
                        else -> colorScheme.outline.copy(alpha = 0.55f)
                    },
                    titleColor = colorScheme.onSurface,
                    metaColor = colorScheme.onSurfaceVariant,
                    dimmed = selectedNodeId != null && !isSelected && !isRelated,
                    isSelected = isSelected,
                    textMeasurer = textMeasurer
                )
            }
        }

        DiagramControls(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            onZoomIn = { scale = (scale * 1.15f).coerceIn(MinScale, MaxScale) },
            onZoomOut = { scale = (scale / 1.15f).coerceIn(MinScale, MaxScale) },
            onRecenter = { fitToViewport() }
        )
    }
}

@Composable
private fun DiagramControls(
    modifier: Modifier = Modifier,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onRecenter: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = onZoomIn) {
                Icon(Icons.Default.Add, contentDescription = "Zoom in")
            }
            IconButton(onClick = onZoomOut) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom out")
            }
            IconButton(onClick = onRecenter) {
                Icon(Icons.Default.CenterFocusStrong, contentDescription = "Recenter diagram")
            }
        }
    }
}

private fun DrawScope.drawStageBackground(
    gridColor: Color,
    glowColor: Color
) {
    val spacing = 48f
    val columns = (size.width / spacing).toInt() + 2
    val rows = (size.height / spacing).toInt() + 2

    drawRect(color = Color.Transparent)

    repeat(columns) { index ->
        val x = index * spacing
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f
        )
    }
    repeat(rows) { index ->
        val y = index * spacing
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
    }

    drawCircle(
        color = glowColor,
        radius = size.minDimension * 0.42f,
        center = Offset(size.width * 0.5f, size.height * 0.28f)
    )
}

private fun DrawScope.drawNode(
    node: DiagramNode,
    scale: Float,
    canvasOffset: Offset,
    visualStyle: NodeVisualStyle,
    surfaceColor: Color,
    borderColor: Color,
    titleColor: Color,
    metaColor: Color,
    dimmed: Boolean,
    isSelected: Boolean,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val rect = scaledNodeRect(node, scale, canvasOffset)
    val cornerRadius = 24f * scale
    val alpha = if (dimmed) 0.42f else 1f
    val contentPadding = 14f * scale

    drawRoundRect(
        color = Color.Black.copy(alpha = if (isSelected) 0.26f else 0.18f * alpha),
        topLeft = rect.topLeft + Offset(0f, 10f * scale),
        size = rect.size,
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )

    drawRoundRect(
        color = surfaceColor.copy(alpha = 0.96f * alpha),
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )

    drawRoundRect(
        color = visualStyle.accent.copy(alpha = if (dimmed) 0.2f else 0.32f),
        topLeft = rect.topLeft,
        size = Size(rect.width, 12f * scale),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )

    drawRoundRect(
        color = borderColor.copy(alpha = if (isSelected) 1f else alpha),
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
        style = Stroke(width = if (isSelected) 3f * scale else 2f * scale)
    )

    val badgeWidth = max(100f * scale, rect.width * 0.28f)
    val badgeHeight = 22f * scale
    val badgeTopLeft = Offset(rect.left + contentPadding, rect.top + 12f * scale)
    drawRoundRect(
        color = visualStyle.accent.copy(alpha = if (dimmed) 0.14f else 0.18f),
        topLeft = badgeTopLeft,
        size = Size(badgeWidth, badgeHeight),
        cornerRadius = CornerRadius(999f, 999f)
    )

    val badgeText = textMeasurer.measure(
        text = visualStyle.badge,
        style = TextStyle(
            color = visualStyle.accent.copy(alpha = alpha),
            fontWeight = FontWeight.SemiBold,
            fontSize = (5.2f * scale).coerceAtLeast(5f).sp
        ),
        constraints = Constraints(maxWidth = (badgeWidth - 14f * scale).toInt()),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    drawText(
        textLayoutResult = badgeText,
        topLeft = Offset(
            x = badgeTopLeft.x + (badgeWidth - badgeText.size.width) / 2f,
            y = badgeTopLeft.y + (badgeHeight - badgeText.size.height) / 2f
        )
    )

    val titleLayout = textMeasurer.measure(
        text = node.label,
        style = TextStyle(
            color = titleColor.copy(alpha = alpha),
            fontWeight = FontWeight.Bold,
            fontSize = (8.2f * scale).coerceAtLeast(8f).sp,
            lineHeight = (12.2f * scale).coerceAtLeast(10.5f).sp
        ),
        constraints = Constraints(maxWidth = (rect.width - contentPadding * 2).toInt()),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
    val availableTitleTop = badgeTopLeft.y + badgeHeight + 14f * scale
    val availableTitleBottom = rect.bottom - 16f * scale
    val titleY = (availableTitleTop + availableTitleBottom - titleLayout.size.height) / 2f
    drawText(
        textLayoutResult = titleLayout,
        topLeft = Offset(
            x = rect.left + contentPadding,
            y = titleY
        )
    )
}

private fun DrawScope.drawConnection(
    fromNode: DiagramNode,
    toNode: DiagramNode,
    connection: DiagramConnection,
    scale: Float,
    canvasOffset: Offset,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    lineColor: Color,
    labelContainer: Color,
    labelTextColor: Color,
    emphasized: Boolean
) {
    val fromRect = scaledNodeRect(fromNode, scale, canvasOffset)
    val toRect = scaledNodeRect(toNode, scale, canvasOffset)
    val start = anchorPoint(fromRect, toRect)
    val end = anchorPoint(toRect, fromRect)
    val horizontal = abs(end.x - start.x) >= abs(end.y - start.y)
    val mid = if (horizontal) {
        Offset((start.x + end.x) / 2f, start.y)
    } else {
        Offset(start.x, (start.y + end.y) / 2f)
    }

    val path = Path().apply {
        moveTo(start.x, start.y)
        lineTo(mid.x, mid.y)
        lineTo(end.x, end.y)
    }

    drawPath(
        path = path,
        color = Color.Black.copy(alpha = if (emphasized) 0.18f else 0.1f),
        style = Stroke(width = if (emphasized) 7f * scale else 5f * scale)
    )
    drawPath(
        path = path,
        color = lineColor,
        style = Stroke(width = if (emphasized) 3.5f * scale else 2.5f * scale)
    )

    drawArrowHead(start = mid, end = end, color = lineColor, scale = scale)

    if (connection.label.isNotBlank()) {
        val labelLayout = textMeasurer.measure(
            text = connection.label,
            style = TextStyle(
                color = labelTextColor,
                fontSize = (10f * scale).coerceAtLeast(9f).sp,
                fontWeight = FontWeight.Medium
            )
        )
        val labelPaddingX = 10f * scale
        val labelPaddingY = 6f * scale
        val labelSize = Size(
            width = labelLayout.size.width + labelPaddingX * 2,
            height = labelLayout.size.height + labelPaddingY * 2
        )
        val labelTopLeft = Offset(
            x = mid.x - labelSize.width / 2f,
            y = mid.y - labelSize.height / 2f
        )

        drawRoundRect(
            color = labelContainer.copy(alpha = 0.96f),
            topLeft = labelTopLeft,
            size = labelSize,
            cornerRadius = CornerRadius(999f, 999f)
        )
        drawRoundRect(
            color = lineColor.copy(alpha = 0.18f),
            topLeft = labelTopLeft,
            size = labelSize,
            cornerRadius = CornerRadius(999f, 999f),
            style = Stroke(width = 1.5f * scale)
        )
        drawText(
            textLayoutResult = labelLayout,
            topLeft = Offset(
                x = labelTopLeft.x + labelPaddingX,
                y = labelTopLeft.y + labelPaddingY
            )
        )
    }
}

private fun DrawScope.drawArrowHead(
    start: Offset,
    end: Offset,
    color: Color,
    scale: Float
) {
    val angle = kotlin.math.atan2(end.y - start.y, end.x - start.x)
    val arrowSize = 11f * scale
    val arrowPath = Path().apply {
        moveTo(end.x, end.y)
        lineTo(
            end.x - arrowSize * kotlin.math.cos(angle - Math.PI / 6).toFloat(),
            end.y - arrowSize * kotlin.math.sin(angle - Math.PI / 6).toFloat()
        )
        moveTo(end.x, end.y)
        lineTo(
            end.x - arrowSize * kotlin.math.cos(angle + Math.PI / 6).toFloat(),
            end.y - arrowSize * kotlin.math.sin(angle + Math.PI / 6).toFloat()
        )
    }
    drawPath(
        path = arrowPath,
        color = color,
        style = Stroke(width = 2.5f * scale)
    )
}

@Composable
fun DiagramLegend(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        LegendPill(
            label = "Layer",
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
            textColor = MaterialTheme.colorScheme.primary
        )
        LegendPill(
            label = "Selected",
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
            textColor = MaterialTheme.colorScheme.secondary
        )
        LegendPill(
            label = "Dependency",
            color = MaterialTheme.colorScheme.surfaceVariant,
            textColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LegendPill(
    label: String,
    color: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = textColor.copy(alpha = 0.16f),
                shape = RoundedCornerShape(999.dp)
            )
            .background(color = color, shape = RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(textColor, RoundedCornerShape(999.dp))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

private data class NodeVisualStyle(
    val accent: Color,
    val badge: String
)

private fun nodeVisualStyle(
    type: String,
    primary: Color,
    secondary: Color,
    tertiary: Color
): NodeVisualStyle {
    val normalized = type.lowercase()
    return when {
        "ui" in normalized -> NodeVisualStyle(primary, "UI")
        "view" in normalized -> NodeVisualStyle(secondary, "VM")
        "use" in normalized || "domain" in normalized -> NodeVisualStyle(tertiary, "DOMAIN")
        "repo" in normalized || "data" in normalized -> NodeVisualStyle(primary.copy(alpha = 0.85f), "DATA")
        "network" in normalized -> NodeVisualStyle(secondary.copy(alpha = 0.92f), "API")
        "database" in normalized || "storage" in normalized -> NodeVisualStyle(tertiary.copy(alpha = 0.92f), "DB")
        else -> NodeVisualStyle(primary, normalized.take(8).uppercase())
    }
}

private fun diagramBounds(nodes: List<DiagramNode>): Rect {
    val minX = nodes.minOf { it.x }
    val minY = nodes.minOf { it.y }
    val maxX = nodes.maxOf { it.x + NodeWidth }
    val maxY = nodes.maxOf { it.y + NodeHeight }
    return Rect(
        left = minX - DiagramPadding,
        top = minY - DiagramPadding,
        right = maxX + DiagramPadding,
        bottom = maxY + DiagramPadding
    )
}

private fun normalizeNodeLayout(nodes: List<DiagramNode>): List<DiagramNode> {
    if (nodes.isEmpty()) return emptyList()

    val sortedX = nodes.map { it.x }.distinct().sorted()
    val sortedY = nodes.map { it.y }.distinct().sorted()

    val xIndex = sortedX.withIndex().associate { it.value to it.index }
    val yIndex = sortedY.withIndex().associate { it.value to it.index }

    val originX = sortedX.first()
    val originY = sortedY.first()

    return nodes.map { node ->
        val column = xIndex.getValue(node.x)
        val row = yIndex.getValue(node.y)
        node.copy(
            x = originX + column * (NodeWidth + HorizontalNodeGap),
            y = originY + row * (NodeHeight + VerticalNodeGap)
        )
    }
}

private fun nodeRect(node: DiagramNode): Rect = Rect(
    left = node.x,
    top = node.y,
    right = node.x + NodeWidth,
    bottom = node.y + NodeHeight
)

private fun scaledNodeRect(
    node: DiagramNode,
    scale: Float,
    canvasOffset: Offset
): Rect {
    val rect = nodeRect(node)
    return Rect(
        left = rect.left * scale + canvasOffset.x,
        top = rect.top * scale + canvasOffset.y,
        right = rect.right * scale + canvasOffset.x,
        bottom = rect.bottom * scale + canvasOffset.y
    )
}

private fun anchorPoint(source: Rect, target: Rect): Offset {
    val sourceCenter = source.center
    val targetCenter = target.center
    val horizontal = abs(targetCenter.x - sourceCenter.x) >= abs(targetCenter.y - sourceCenter.y)

    return if (horizontal) {
        if (targetCenter.x >= sourceCenter.x) {
            Offset(source.right, sourceCenter.y)
        } else {
            Offset(source.left, sourceCenter.y)
        }
    } else {
        if (targetCenter.y >= sourceCenter.y) {
            Offset(sourceCenter.x, source.bottom)
        } else {
            Offset(sourceCenter.x, source.top)
        }
    }
}
