package com.vero.repolens.data.repository

import android.content.Context
import com.vero.repolens.data.models.RepoLensReport
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
            RepoLensResult.Error("Failed to load report: ${e.message}")
        }
    }
}

// Made with Bob
