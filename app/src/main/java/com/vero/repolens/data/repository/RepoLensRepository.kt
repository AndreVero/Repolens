package com.vero.repolens.data.repository

import android.content.Context
import android.util.Log
import com.vero.repolens.data.models.RepoLensReport
import com.vero.repolens.data.models.SearchableItem
import com.vero.repolens.data.models.SearchItemType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

sealed class RepoLensResult {
    data class Success(val report: RepoLensReport) : RepoLensResult()
    data class Error(val message: String) : RepoLensResult()
}

@Singleton
class RepoLensRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    suspend fun loadReport(): RepoLensResult {
        return try {
            val jsonString = context.assets.open("repolens_sample_report.json")
                .bufferedReader()
                .use { it.readText() }
            
            val report = json.decodeFromString<RepoLensReport>(jsonString)
            RepoLensResult.Success(report)
        } catch (e: Exception) {
            Log.e("RepoLensRepository", "Failed to load report", e)
            RepoLensResult.Error("Failed to load report. Please check the file and try again.")
        }
    }

    /**
     * Index all searchable items from the report
     */
    fun indexSearchableItems(report: RepoLensReport): List<SearchableItem> {
        val items = mutableListOf<SearchableItem>()

        // Index modules
        report.modules.forEach { module ->
            items.add(
                SearchableItem(
                    id = module.id,
                    type = SearchItemType.MODULE,
                    title = module.name,
                    subtitle = module.responsibility,
                    category = module.type,
                    severity = module.riskLevel,
                    relatedObject = module
                )
            )
        }

        // Index features
        report.features.forEach { feature ->
            items.add(
                SearchableItem(
                    id = feature.id,
                    type = SearchItemType.FEATURE,
                    title = feature.name,
                    subtitle = feature.description,
                    category = feature.complexity,
                    severity = feature.riskLevel,
                    relatedObject = feature
                )
            )
        }

        // Index risks
        report.risks.forEach { risk ->
            items.add(
                SearchableItem(
                    id = risk.id,
                    type = SearchItemType.RISK,
                    title = risk.title,
                    subtitle = risk.whyItMatters ?: "",
                    category = risk.category,
                    severity = risk.severity,
                    relatedObject = risk
                )
            )
        }

        // Index architecture layers
        report.architecture.layers.forEach { layer ->
            items.add(
                SearchableItem(
                    id = layer.id,
                    type = SearchItemType.ARCHITECTURE_LAYER,
                    title = layer.name,
                    subtitle = layer.responsibility,
                    category = "Architecture",
                    severity = null,
                    relatedObject = layer
                )
            )
        }

        // Index test files
        report.testing?.existingTests?.forEach { test ->
            items.add(
                SearchableItem(
                    id = test.name,
                    type = SearchItemType.TEST,
                    title = test.name,
                    subtitle = test.type,
                    category = test.type,
                    severity = null,
                    relatedObject = test
                )
            )
        }

        // Index important files from modules
        report.modules.forEach { module ->
            module.importantFiles.forEach { file ->
                items.add(
                    SearchableItem(
                        id = "$file-${module.id}",
                        type = SearchItemType.FILE,
                        title = file.substringAfterLast('/'),
                        subtitle = file,
                        category = module.name,
                        severity = null,
                        relatedObject = file
                    )
                )
            }
        }

        return items
    }

    /**
     * Search items by query string with optimized indexed search
     */
    fun searchItems(items: List<SearchableItem>, query: String): List<SearchableItem> {
        if (query.isBlank()) return items

        // Pre-process query once
        val lowerQuery = query.lowercase()
        val queryTokens = lowerQuery.split(Regex("\\s+")).filter { it.isNotEmpty() }
        
        // Use indexed search with pre-computed lowercase strings
        return items.mapNotNull { item ->
            val searchText = "${item.title} ${item.subtitle} ${item.category}".lowercase()
            
            // Check if all query tokens match (AND logic for multi-word queries)
            val matchScore = queryTokens.count { token -> searchText.contains(token) }
            
            if (matchScore == queryTokens.size) {
                item to matchScore
            } else {
                null
            }
        }
        .sortedByDescending { it.second } // Sort by relevance (match score)
        .map { it.first }
    }
}

// Made with Bob
