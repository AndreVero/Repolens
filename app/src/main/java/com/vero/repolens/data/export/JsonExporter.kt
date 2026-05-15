package com.vero.repolens.data.export

import com.vero.repolens.data.models.RepoLensReport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Exports reports to JSON format
 */
@Singleton
class JsonExporter @Inject constructor() {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun export(report: RepoLensReport, outputFile: File): Result<File> {
        return try {
            val jsonString = json.encodeToString(report)
            outputFile.writeText(jsonString)
            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Made with Bob