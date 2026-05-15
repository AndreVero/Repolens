package com.vero.repolens.data.export

import android.content.Context
import com.vero.repolens.data.models.RepoLensReport
import java.io.File

/**
 * Service interface for exporting reports in different formats
 */
interface ExportService {
    suspend fun exportToPdf(report: RepoLensReport, outputFile: File): Result<File>
    suspend fun exportToMarkdown(report: RepoLensReport, outputFile: File): Result<File>
    suspend fun exportToJson(report: RepoLensReport, outputFile: File): Result<File>
}

/**
 * Export format options
 */
enum class ExportFormat {
    PDF,
    MARKDOWN,
    JSON
}

/**
 * Export result with file information
 */
data class ExportResult(
    val format: ExportFormat,
    val file: File,
    val sizeBytes: Long
)

// Made with Bob