package com.vero.repolens.viewmodel

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vero.repolens.data.export.*
import com.vero.repolens.data.models.RepoLensReport
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

sealed class ExportState {
    object Idle : ExportState()
    object Exporting : ExportState()
    data class Success(val result: ExportResult) : ExportState()
    data class Error(val message: String) : ExportState()
}

@HiltViewModel
class ExportViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfGenerator: PdfGenerator,
    private val markdownGenerator: MarkdownGenerator,
    private val jsonExporter: JsonExporter
) : ViewModel() {

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    fun exportReport(report: RepoLensReport, format: ExportFormat) {
        viewModelScope.launch {
            _exportState.value = ExportState.Exporting
            
            try {
                val result = withContext(Dispatchers.IO) {
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val fileName = "repolens_report_$timestamp"
                    
                    val outputFile = when (format) {
                        ExportFormat.PDF -> {
                            val file = File(context.cacheDir, "$fileName.pdf")
                            pdfGenerator.generate(report, file)
                        }
                        ExportFormat.MARKDOWN -> {
                            val file = File(context.cacheDir, "$fileName.md")
                            markdownGenerator.generate(report, file)
                        }
                        ExportFormat.JSON -> {
                            val file = File(context.cacheDir, "$fileName.json")
                            jsonExporter.export(report, file)
                        }
                    }
                    
                    outputFile.getOrThrow()
                }
                
                _exportState.value = ExportState.Success(
                    ExportResult(
                        format = format,
                        file = result,
                        sizeBytes = result.length()
                    )
                )
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Export failed")
            }
        }
    }

    fun shareFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val mimeType = when (file.extension) {
                "pdf" -> "application/pdf"
                "md" -> "text/markdown"
                "json" -> "application/json"
                else -> "*/*"
            }
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val chooserIntent = Intent.createChooser(shareIntent, "Share Report")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            _exportState.value = ExportState.Error("Failed to share file: ${e.message}")
        }
    }

    fun resetState() {
        _exportState.value = ExportState.Idle
    }
}

// Made with Bob