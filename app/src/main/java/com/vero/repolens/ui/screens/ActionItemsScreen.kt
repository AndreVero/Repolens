package com.vero.repolens.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.vero.repolens.ui.components.ActionItemCard
import com.vero.repolens.ui.components.LoadingState
import com.vero.repolens.viewmodel.ActionItemsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionItemsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ActionItemsViewModel = hiltViewModel()
) {
    val filteredItems by viewModel.filteredActionItems.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()
    val filterPriority by viewModel.filterPriority.collectAsState()
    val todoCount by viewModel.todoCount.collectAsState()
    val inProgressCount by viewModel.inProgressCount.collectAsState()
    val doneCount by viewModel.doneCount.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ActionItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Action Items") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearFilters() }) {
                        Icon(Icons.Default.FilterAltOff, contentDescription = "Clear filters")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add action item")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Status counts
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatusCountChip(
                        label = "To Do",
                        count = todoCount,
                        icon = Icons.Default.CheckBoxOutlineBlank,
                        isSelected = filterStatus == ActionStatus.TODO,
                        onClick = {
                            viewModel.setStatusFilter(
                                if (filterStatus == ActionStatus.TODO) null else ActionStatus.TODO
                            )
                        }
                    )
                    StatusCountChip(
                        label = "In Progress",
                        count = inProgressCount,
                        icon = Icons.Default.HourglassEmpty,
                        isSelected = filterStatus == ActionStatus.IN_PROGRESS,
                        onClick = {
                            viewModel.setStatusFilter(
                                if (filterStatus == ActionStatus.IN_PROGRESS) null else ActionStatus.IN_PROGRESS
                            )
                        }
                    )
                    StatusCountChip(
                        label = "Done",
                        count = doneCount,
                        icon = Icons.Default.CheckBox,
                        isSelected = filterStatus == ActionStatus.DONE,
                        onClick = {
                            viewModel.setStatusFilter(
                                if (filterStatus == ActionStatus.DONE) null else ActionStatus.DONE
                            )
                        }
                    )
                }
            }

            // Priority filters
            Text(
                text = "Filter by Priority",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(Priority.values()) { priority ->
                    FilterChip(
                        selected = filterPriority == priority,
                        onClick = {
                            viewModel.setPriorityFilter(
                                if (filterPriority == priority) null else priority
                            )
                        },
                        label = { Text(priority.name) }
                    )
                }
            }

            Divider()

            // Action items list
            if (filteredItems.isEmpty()) {
                EmptyActionItemsState(
                    onAddClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        ActionItemCard(
                            actionItem = item,
                            onStatusToggle = { viewModel.toggleActionItemStatus(item) },
                            onEdit = { editingItem = item },
                            onDelete = { viewModel.deleteActionItem(item) }
                        )
                    }
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showAddDialog || editingItem != null) {
        ActionItemDialog(
            actionItem = editingItem,
            onDismiss = {
                showAddDialog = false
                editingItem = null
            },
            onSave = { title, description, priority, category ->
                if (editingItem != null) {
                    viewModel.updateActionItem(
                        editingItem!!.copy(
                            title = title,
                            description = description,
                            priority = priority,
                            category = category
                        )
                    )
                } else {
                    viewModel.createActionItem(
                        title = title,
                        description = description,
                        priority = priority,
                        category = category
                    )
                }
                showAddDialog = false
                editingItem = null
            }
        )
    }
}

@Composable
private fun StatusCountChip(
    label: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(count.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        },
        modifier = Modifier.padding(4.dp)
    )
}

@Composable
private fun EmptyActionItemsState(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TaskAlt,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No action items yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Create your first action item to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Action Item")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionItemDialog(
    actionItem: ActionItem?,
    onDismiss: () -> Unit,
    onSave: (String, String, Priority, ActionCategory) -> Unit
) {
    var title by remember { mutableStateOf(actionItem?.title ?: "") }
    var description by remember { mutableStateOf(actionItem?.description ?: "") }
    var priority by remember { mutableStateOf(actionItem?.priority ?: Priority.MEDIUM) }
    var category by remember { mutableStateOf(actionItem?.category ?: ActionCategory.OTHER) }
    var expandedPriority by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (actionItem == null) "New Action Item" else "Edit Action Item") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                // Priority dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedPriority,
                    onExpandedChange = { expandedPriority = it }
                ) {
                    OutlinedTextField(
                        value = priority.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priority") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPriority,
                        onDismissRequest = { expandedPriority = false }
                    ) {
                        Priority.values().forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.name) },
                                onClick = {
                                    priority = p
                                    expandedPriority = false
                                }
                            )
                        }
                    }
                }

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = it }
                ) {
                    OutlinedTextField(
                        value = category.name.replace('_', ' '),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        ActionCategory.values().forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.name.replace('_', ' ')) },
                                onClick = {
                                    category = c
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, description, priority, category)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Made with Bob