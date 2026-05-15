package com.vero.repolens.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * Represents an action item or todo for the developer
 */
@Entity(tableName = "action_items")
@TypeConverters(ActionItemConverters::class)
data class ActionItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val priority: Priority,
    val category: ActionCategory,
    val status: ActionStatus,
    val relatedRiskIds: List<String> = emptyList(),
    val relatedFiles: List<String> = emptyList(),
    val createdDate: Long = System.currentTimeMillis(),
    val updatedDate: Long = System.currentTimeMillis()
)

/**
 * Priority levels for action items
 */
enum class Priority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Categories of action items
 */
enum class ActionCategory {
    ADD_TEST,
    FIX_RISK,
    REFACTOR,
    DOCUMENT,
    PERFORMANCE,
    SECURITY,
    OTHER
}

/**
 * Status of action items
 */
enum class ActionStatus {
    TODO,
    IN_PROGRESS,
    DONE
}

/**
 * Type converters for Room database
 */
class ActionItemConverters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun fromPriority(value: Priority): String {
        return value.name
    }

    @TypeConverter
    fun toPriority(value: String): Priority {
        return Priority.valueOf(value)
    }

    @TypeConverter
    fun fromCategory(value: ActionCategory): String {
        return value.name
    }

    @TypeConverter
    fun toCategory(value: String): ActionCategory {
        return ActionCategory.valueOf(value)
    }

    @TypeConverter
    fun fromStatus(value: ActionStatus): String {
        return value.name
    }

    @TypeConverter
    fun toStatus(value: String): ActionStatus {
        return ActionStatus.valueOf(value)
    }
}

// Made with Bob