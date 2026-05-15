package com.vero.repolens.data.local

import androidx.room.*
import com.vero.repolens.data.models.ActionItem
import com.vero.repolens.data.models.ActionStatus
import com.vero.repolens.data.models.Priority
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for ActionItem entities
 */
@Dao
interface ActionItemDao {
    
    /**
     * Get all action items as a Flow
     */
    @Query("SELECT * FROM action_items ORDER BY createdDate DESC")
    fun getAllActionItems(): Flow<List<ActionItem>>
    
    /**
     * Get action items by status
     */
    @Query("SELECT * FROM action_items WHERE status = :status ORDER BY priority DESC, createdDate DESC")
    fun getActionItemsByStatus(status: ActionStatus): Flow<List<ActionItem>>
    
    /**
     * Get action items by priority
     */
    @Query("SELECT * FROM action_items WHERE priority = :priority ORDER BY createdDate DESC")
    fun getActionItemsByPriority(priority: Priority): Flow<List<ActionItem>>
    
    /**
     * Get a single action item by ID
     */
    @Query("SELECT * FROM action_items WHERE id = :id")
    suspend fun getActionItemById(id: String): ActionItem?
    
    /**
     * Get action items related to a specific risk
     */
    @Query("SELECT * FROM action_items WHERE relatedRiskIds LIKE '%' || :riskId || '%'")
    fun getActionItemsByRiskId(riskId: String): Flow<List<ActionItem>>
    
    /**
     * Get count of action items by status
     */
    @Query("SELECT COUNT(*) FROM action_items WHERE status = :status")
    fun getCountByStatus(status: ActionStatus): Flow<Int>
    
    /**
     * Insert a new action item
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActionItem(actionItem: ActionItem)
    
    /**
     * Insert multiple action items
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActionItems(actionItems: List<ActionItem>)
    
    /**
     * Update an existing action item
     */
    @Update
    suspend fun updateActionItem(actionItem: ActionItem)
    
    /**
     * Delete an action item
     */
    @Delete
    suspend fun deleteActionItem(actionItem: ActionItem)
    
    /**
     * Delete action item by ID
     */
    @Query("DELETE FROM action_items WHERE id = :id")
    suspend fun deleteActionItemById(id: String)
    
    /**
     * Delete all action items
     */
    @Query("DELETE FROM action_items")
    suspend fun deleteAllActionItems()
    
    /**
     * Update action item status
     */
    @Query("UPDATE action_items SET status = :status, updatedDate = :updatedDate WHERE id = :id")
    suspend fun updateActionItemStatus(id: String, status: ActionStatus, updatedDate: Long)
}

// Made with Bob