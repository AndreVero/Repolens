package com.vero.repolens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vero.repolens.data.local.ActionItemDao
import com.vero.repolens.data.models.ActionCategory
import com.vero.repolens.data.models.ActionItem
import com.vero.repolens.data.models.ActionStatus
import com.vero.repolens.data.models.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing action items
 */
@HiltViewModel
class ActionItemsViewModel @Inject constructor(
    private val actionItemDao: ActionItemDao
) : ViewModel() {

    // All action items from database
    val allActionItems: StateFlow<List<ActionItem>> = actionItemDao.getAllActionItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filter state
    private val _filterStatus = MutableStateFlow<ActionStatus?>(null)
    val filterStatus: StateFlow<ActionStatus?> = _filterStatus.asStateFlow()

    private val _filterPriority = MutableStateFlow<Priority?>(null)
    val filterPriority: StateFlow<Priority?> = _filterPriority.asStateFlow()

    // Filtered action items
    val filteredActionItems: StateFlow<List<ActionItem>> = combine(
        allActionItems,
        _filterStatus,
        _filterPriority
    ) { items, status, priority ->
        items.filter { item ->
            val statusMatch = status == null || item.status == status
            val priorityMatch = priority == null || item.priority == priority
            statusMatch && priorityMatch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Counts by status
    val todoCount: StateFlow<Int> = actionItemDao.getCountByStatus(ActionStatus.TODO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val inProgressCount: StateFlow<Int> = actionItemDao.getCountByStatus(ActionStatus.IN_PROGRESS)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val doneCount: StateFlow<Int> = actionItemDao.getCountByStatus(ActionStatus.DONE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /**
     * Create a new action item
     */
    fun createActionItem(
        title: String,
        description: String,
        priority: Priority,
        category: ActionCategory,
        relatedRiskIds: List<String> = emptyList(),
        relatedFiles: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val actionItem = ActionItem(
                title = title,
                description = description,
                priority = priority,
                category = category,
                status = ActionStatus.TODO,
                relatedRiskIds = relatedRiskIds,
                relatedFiles = relatedFiles
            )
            actionItemDao.insertActionItem(actionItem)
        }
    }

    /**
     * Update an existing action item
     */
    fun updateActionItem(actionItem: ActionItem) {
        viewModelScope.launch {
            val updated = actionItem.copy(updatedDate = System.currentTimeMillis())
            actionItemDao.updateActionItem(updated)
        }
    }

    /**
     * Update action item status
     */
    fun updateActionItemStatus(id: String, status: ActionStatus) {
        viewModelScope.launch {
            actionItemDao.updateActionItemStatus(id, status, System.currentTimeMillis())
        }
    }

    /**
     * Toggle action item status (TODO -> IN_PROGRESS -> DONE -> TODO)
     */
    fun toggleActionItemStatus(actionItem: ActionItem) {
        val newStatus = when (actionItem.status) {
            ActionStatus.TODO -> ActionStatus.IN_PROGRESS
            ActionStatus.IN_PROGRESS -> ActionStatus.DONE
            ActionStatus.DONE -> ActionStatus.TODO
        }
        updateActionItemStatus(actionItem.id, newStatus)
    }

    /**
     * Delete an action item
     */
    fun deleteActionItem(actionItem: ActionItem) {
        viewModelScope.launch {
            actionItemDao.deleteActionItem(actionItem)
        }
    }

    /**
     * Delete action item by ID
     */
    fun deleteActionItemById(id: String) {
        viewModelScope.launch {
            actionItemDao.deleteActionItemById(id)
        }
    }

    /**
     * Set status filter
     */
    fun setStatusFilter(status: ActionStatus?) {
        _filterStatus.value = status
    }

    /**
     * Set priority filter
     */
    fun setPriorityFilter(priority: Priority?) {
        _filterPriority.value = priority
    }

    /**
     * Clear all filters
     */
    fun clearFilters() {
        _filterStatus.value = null
        _filterPriority.value = null
    }

    /**
     * Get action items for a specific risk
     */
    fun getActionItemsForRisk(riskId: String): Flow<List<ActionItem>> {
        return actionItemDao.getActionItemsByRiskId(riskId)
    }
}

// Made with Bob