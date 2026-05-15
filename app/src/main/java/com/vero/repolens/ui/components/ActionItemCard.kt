package com.vero.repolens.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.vero.repolens.data.models.ActionCategory
import com.vero.repolens.data.models.ActionItem
import com.vero.repolens.data.models.ActionStatus
import com.vero.repolens.data.models.Priority
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ActionItemCard(
    actionItem: ActionItem,
    onStatusToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (actionItem.status) {
                ActionStatus.DONE -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with checkbox and menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Status checkbox
                    IconButton(onClick = onStatusToggle) {
                        Icon(
                            imageVector = when (actionItem.status) {
                                ActionStatus.TODO -> Icons.Default.CheckBoxOutlineBlank
                                ActionStatus.IN_PROGRESS -> Icons.Default.HourglassEmpty
                                ActionStatus.DONE -> Icons.Default.CheckBox
                            },
                            contentDescription = "Toggle status",
                            tint = when (actionItem.status) {
                                ActionStatus.DONE -> MaterialTheme.colorScheme.primary
                                ActionStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Title
                    Text(
                        text = actionItem.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (actionItem.status == ActionStatus.DONE) {
                            TextDecoration.LineThrough
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Menu button
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        )
                    }
                }
            }

            // Description
            if (actionItem.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = actionItem.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Chips for priority, category, and status
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Priority chip
                AssistChip(
                    onClick = { },
                    label = { Text(actionItem.priority.name, style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(
                            imageVector = getPriorityIcon(actionItem.priority),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = getPriorityColor(actionItem.priority)
                    )
                )

                // Category chip
                AssistChip(
                    onClick = { },
                    label = { Text(getCategoryLabel(actionItem.category), style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(
                            imageVector = getCategoryIcon(actionItem.category),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                // Status chip
                AssistChip(
                    onClick = { },
                    label = { Text(getStatusLabel(actionItem.status), style = MaterialTheme.typography.labelSmall) }
                )
            }

            // Related info
            if (actionItem.relatedRiskIds.isNotEmpty() || actionItem.relatedFiles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = buildString {
                        if (actionItem.relatedRiskIds.isNotEmpty()) {
                            append("${actionItem.relatedRiskIds.size} related risk(s)")
                        }
                        if (actionItem.relatedFiles.isNotEmpty()) {
                            if (isNotEmpty()) append(" • ")
                            append("${actionItem.relatedFiles.size} file(s)")
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Date
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Created ${formatDate(actionItem.createdDate)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun getPriorityColor(priority: Priority) = when (priority) {
    Priority.CRITICAL -> MaterialTheme.colorScheme.errorContainer
    Priority.HIGH -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
    Priority.MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer
    Priority.LOW -> MaterialTheme.colorScheme.secondaryContainer
}

private fun getPriorityIcon(priority: Priority) = when (priority) {
    Priority.CRITICAL -> Icons.Default.PriorityHigh
    Priority.HIGH -> Icons.Default.ArrowUpward
    Priority.MEDIUM -> Icons.Default.Remove
    Priority.LOW -> Icons.Default.ArrowDownward
}

private fun getCategoryIcon(category: ActionCategory) = when (category) {
    ActionCategory.ADD_TEST -> Icons.Default.Science
    ActionCategory.FIX_RISK -> Icons.Default.Warning
    ActionCategory.REFACTOR -> Icons.Default.Build
    ActionCategory.DOCUMENT -> Icons.Default.Description
    ActionCategory.PERFORMANCE -> Icons.Default.Speed
    ActionCategory.SECURITY -> Icons.Default.Security
    ActionCategory.OTHER -> Icons.Default.MoreHoriz
}

private fun getCategoryLabel(category: ActionCategory) = when (category) {
    ActionCategory.ADD_TEST -> "Add Test"
    ActionCategory.FIX_RISK -> "Fix Risk"
    ActionCategory.REFACTOR -> "Refactor"
    ActionCategory.DOCUMENT -> "Document"
    ActionCategory.PERFORMANCE -> "Performance"
    ActionCategory.SECURITY -> "Security"
    ActionCategory.OTHER -> "Other"
}

private fun getStatusLabel(status: ActionStatus) = when (status) {
    ActionStatus.TODO -> "To Do"
    ActionStatus.IN_PROGRESS -> "In Progress"
    ActionStatus.DONE -> "Done"
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// Made with Bob