package com.vero.repolens.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vero.repolens.data.models.ActionCategory
import com.vero.repolens.data.models.ActionItem
import com.vero.repolens.data.models.ActionStatus
import com.vero.repolens.data.models.Priority
import com.vero.repolens.ui.components.ActionItemCard
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

    var showEditorSheet by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ActionItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Action Items") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearFilters() }) {
                        Icon(Icons.Default.FilterAltOff, contentDescription = "Clear filters")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ActionItemsOverviewCard(
                todoCount = todoCount,
                inProgressCount = inProgressCount,
                doneCount = doneCount,
                filterStatus = filterStatus,
                onStatusSelected = { selectedStatus ->
                    viewModel.setStatusFilter(
                        if (filterStatus == selectedStatus) null else selectedStatus
                    )
                }
            )

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

            if (filteredItems.isEmpty()) {
                EmptyActionItemsState(
                    onAddClick = {
                        editingItem = null
                        showEditorSheet = true
                    }
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
                            onEdit = {
                                editingItem = item
                                showEditorSheet = true
                            },
                            onDelete = { viewModel.deleteActionItem(item) }
                        )
                    }
                }
            }
        }
    }

    if (showEditorSheet || editingItem != null) {
        ActionItemEditorSheet(
            actionItem = editingItem,
            onDismiss = {
                showEditorSheet = false
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
                showEditorSheet = false
                editingItem = null
            }
        )
    }
}

@Composable
private fun ActionItemsOverviewCard(
    todoCount: Int,
    inProgressCount: Int,
    doneCount: Int,
    filterStatus: ActionStatus?,
    onStatusSelected: (ActionStatus) -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusCountChip(
                label = "To Do",
                modifier = Modifier.weight(1f),
                count = todoCount,
                icon = Icons.Default.CheckBoxOutlineBlank,
                isSelected = filterStatus == ActionStatus.TODO,
                onClick = { onStatusSelected(ActionStatus.TODO) }
            )
            StatusCountChip(
                label = "In Progress",
                modifier = Modifier.weight(1f),
                count = inProgressCount,
                icon = Icons.Default.HourglassEmpty,
                isSelected = filterStatus == ActionStatus.IN_PROGRESS,
                onClick = { onStatusSelected(ActionStatus.IN_PROGRESS) }
            )
            StatusCountChip(
                label = "Done",
                modifier = Modifier.weight(1f),
                count = doneCount,
                icon = Icons.Default.CheckBox,
                isSelected = filterStatus == ActionStatus.DONE,
                onClick = { onStatusSelected(ActionStatus.DONE) }
            )
        }
    }
}

@Composable
private fun StatusCountChip(
    label: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.42f)
        },
        border = BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.42f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
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
fun ActionItemEditorSheet(
    actionItem: ActionItem?,
    onDismiss: () -> Unit,
    onSave: (String, String, Priority, ActionCategory) -> Unit
) {
    var title by remember(actionItem?.id) { mutableStateOf(actionItem?.title ?: "") }
    var description by remember(actionItem?.id) { mutableStateOf(actionItem?.description ?: "") }
    var priority by remember(actionItem?.id) { mutableStateOf(actionItem?.priority ?: Priority.MEDIUM) }
    var category by remember(actionItem?.id) { mutableStateOf(actionItem?.category ?: ActionCategory.OTHER) }
    var expandedPriority by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (actionItem == null) "New Action Item" else "Edit Action Item",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(18.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(18.dp)
            )

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
                        .menuAnchor(),
                    shape = RoundedCornerShape(18.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedPriority,
                    onDismissRequest = { expandedPriority = false }
                ) {
                    Priority.values().forEach { value ->
                        DropdownMenuItem(
                            text = { Text(value.name) },
                            onClick = {
                                priority = value
                                expandedPriority = false
                            }
                        )
                    }
                }
            }

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
                        .menuAnchor(),
                    shape = RoundedCornerShape(18.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    ActionCategory.values().forEach { value ->
                        DropdownMenuItem(
                            text = { Text(value.name.replace('_', ' ')) },
                            onClick = {
                                category = value
                                expandedCategory = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
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
            }
        }
    }
}

// Made with Bob
