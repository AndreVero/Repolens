package com.vero.repolens.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.vero.repolens.data.models.ArchitectureDiagram
import com.vero.repolens.data.models.DiagramNode
import kotlin.math.max
import kotlin.math.min

@Composable
fun ArchitectureDiagram(
    diagram: ArchitectureDiagram,
    onNodeTap: (DiagramNode) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var selectedNodeId by remember { mutableStateOf<String?>(null) }

    val textMeasurer = rememberTextMeasurer()
    val nodeColor = MaterialTheme.colorScheme.primary
    val selectedNodeColor = MaterialTheme.colorScheme.tertiary
    val connectionColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val textColor = MaterialTheme.colorScheme.onPrimary

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 3f)
                        offset += pan
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        // Convert tap coordinates to diagram space
                        val diagramX = (tapOffset.x - offset.x) / scale
                        val diagramY = (tapOffset.y - offset.y) / scale

                        // Find tapped node
                        val tappedNode = diagram.nodes.find { node ->
                            val nodeX = node.x
                            val nodeY = node.y
                            val nodeSize = 120f

                            diagramX >= nodeX && diagramX <= nodeX + nodeSize &&
                                    diagramY >= nodeY && diagramY <= nodeY + nodeSize
                        }

                        if (tappedNode != null) {
                            selectedNodeId = tappedNode.id
                            onNodeTap(tappedNode)
                        } else {
                            selectedNodeId = null
                        }
                    }
                }
        ) {
            // Apply transformations
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Draw connections first (behind nodes)
            diagram.connections.forEach { connection ->
                val fromNode = diagram.nodes.find { it.id == connection.from }
                val toNode = diagram.nodes.find { it.id == connection.to }

                if (fromNode != null && toNode != null) {
                    drawConnection(
                        from = Offset(
                            fromNode.x * scale + offset.x + 60f * scale,
                            fromNode.y * scale + offset.y + 60f * scale
                        ),
                        to = Offset(
                            toNode.x * scale + offset.x + 60f * scale,
                            toNode.y * scale + offset.y + 60f * scale
                        ),
                        label = connection.label,
                        color = connectionColor,
                        textMeasurer = textMeasurer,
                        scale = scale
                    )
                }
            }

            // Draw nodes
            diagram.nodes.forEach { node ->
                val isSelected = node.id == selectedNodeId
                drawNode(
                    node = node,
                    offset = Offset(
                        node.x * scale + offset.x,
                        node.y * scale + offset.y
                    ),
                    scale = scale,
                    color = if (isSelected) selectedNodeColor else nodeColor,
                    textColor = textColor,
                    textMeasurer = textMeasurer
                )
            }
        }
    }
}

private fun DrawScope.drawNode(
    node: DiagramNode,
    offset: Offset,
    scale: Float,
    color: Color,
    textColor: Color,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val nodeSize = 120f * scale
    val cornerRadius = 12f * scale

    // Draw node background
    drawRoundRect(
        color = color,
        topLeft = offset,
        size = Size(nodeSize, nodeSize),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
    )

    // Draw node border
    drawRoundRect(
        color = color.copy(alpha = 0.8f),
        topLeft = offset,
        size = Size(nodeSize, nodeSize),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
        style = Stroke(width = 2f * scale)
    )

    // Draw node label
    val textStyle = TextStyle(
        color = textColor,
        fontSize = androidx.compose.ui.unit.TextUnit(12f * scale, androidx.compose.ui.unit.TextUnitType.Sp)
    )
    
    val textLayoutResult = textMeasurer.measure(
        text = node.label,
        style = textStyle,
        constraints = androidx.compose.ui.unit.Constraints(
            maxWidth = (nodeSize * 0.9f).toInt()
        )
    )

    // Center text in node
    val textOffset = Offset(
        offset.x + (nodeSize - textLayoutResult.size.width) / 2,
        offset.y + (nodeSize - textLayoutResult.size.height) / 2
    )

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = textOffset
    )

    // Draw node type badge
    val badgeSize = 24f * scale
    val badgeOffset = Offset(
        offset.x + nodeSize - badgeSize - 4f * scale,
        offset.y + 4f * scale
    )
    
    drawCircle(
        color = color.copy(alpha = 0.3f),
        radius = badgeSize / 2,
        center = Offset(
            badgeOffset.x + badgeSize / 2,
            badgeOffset.y + badgeSize / 2
        )
    )
}

private fun DrawScope.drawConnection(
    from: Offset,
    to: Offset,
    label: String,
    color: Color,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    scale: Float
) {
    // Draw arrow line
    val path = Path().apply {
        moveTo(from.x, from.y)
        lineTo(to.x, to.y)
    }
    
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 2f * scale)
    )

    // Draw arrowhead
    val angle = kotlin.math.atan2(to.y - from.y, to.x - from.x)
    val arrowSize = 10f * scale
    
    val arrowPath = Path().apply {
        moveTo(to.x, to.y)
        lineTo(
            to.x - arrowSize * kotlin.math.cos(angle - Math.PI / 6).toFloat(),
            to.y - arrowSize * kotlin.math.sin(angle - Math.PI / 6).toFloat()
        )
        moveTo(to.x, to.y)
        lineTo(
            to.x - arrowSize * kotlin.math.cos(angle + Math.PI / 6).toFloat(),
            to.y - arrowSize * kotlin.math.sin(angle + Math.PI / 6).toFloat()
        )
    }
    
    drawPath(
        path = arrowPath,
        color = color,
        style = Stroke(width = 2f * scale)
    )

    // Draw connection label
    if (label.isNotEmpty()) {
        val midPoint = Offset(
            (from.x + to.x) / 2,
            (from.y + to.y) / 2
        )
        
        val textStyle = TextStyle(
            color = color,
            fontSize = androidx.compose.ui.unit.TextUnit(10f * scale, androidx.compose.ui.unit.TextUnitType.Sp),
            background = Color.White.copy(alpha = 0.8f)
        )
        
        val textLayoutResult = textMeasurer.measure(
            text = label,
            style = textStyle
        )
        
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                midPoint.x - textLayoutResult.size.width / 2,
                midPoint.y - textLayoutResult.size.height / 2
            )
        )
    }
}

@Composable
fun DiagramLegend(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Legend",
            style = MaterialTheme.typography.titleSmall
        )
        
        LegendItem(
            color = MaterialTheme.colorScheme.primary,
            label = "Module/Component"
        )
        
        LegendItem(
            color = MaterialTheme.colorScheme.tertiary,
            label = "Selected"
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .padding(top = 4.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawLine(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 2f
                    )
                }
            }
            Text(
                text = "Dependency",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .padding(top = 4.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = color,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

// Made with Bob