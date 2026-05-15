package com.vero.repolens.data.models

import kotlinx.serialization.Serializable

/**
 * Represents a searchable item in the RepoLens report
 */
data class SearchableItem(
    val id: String,
    val type: SearchItemType,
    val title: String,
    val subtitle: String,
    val category: String,
    val severity: String? = null,
    val relatedObject: Any // Module, Feature, Risk, etc.
)

/**
 * Types of items that can be searched
 */
enum class SearchItemType {
    MODULE,
    FEATURE,
    RISK,
    FILE,
    TEST,
    ARCHITECTURE_LAYER
}

/**
 * Filter criteria for search results
 */
data class SearchFilter(
    val types: Set<SearchItemType> = SearchItemType.values().toSet(),
    val severities: Set<String> = emptySet(),
    val categories: Set<String> = emptySet()
) {
    fun matches(item: SearchableItem): Boolean {
        val typeMatch = types.contains(item.type)
        val severityMatch = severities.isEmpty() || (item.severity != null && severities.contains(item.severity))
        val categoryMatch = categories.isEmpty() || categories.contains(item.category)
        
        return typeMatch && severityMatch && categoryMatch
    }
}

// Made with Bob