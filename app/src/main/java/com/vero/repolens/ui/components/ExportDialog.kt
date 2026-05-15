package com.vero.repolens.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vero.repolens.data.export.ExportFormat
import com.vero.repolens.data.models.RepoLensReport
import com.vero.repolens.viewmodel.ExportState
import com.vero.repolens.viewmodel.ExportViewModel

@Composable
fun ExportDialog(
    report: RepoLensReport,
    onDismiss: () -> Unit,
    viewModel: ExportViewModel = hiltViewModel()
) {
    val exportState by viewModel.exportState.collectAsState()
    var selectedFormat by remember { mutableStateOf(ExportFormat.PDF) }

    LaunchedEffect(exportState) {
        if (exportState is ExportState.Success) {
            // Auto-dismiss after successful export
            kotlinx.coroutines.delay(2000)
            viewModel.resetState()
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (exportState !is ExportState.Exporting) {
                viewModel.resetState()
                onDismiss()
            }
        },
        title = { Text("Export Report") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                when (val state = exportState) {
                    is ExportState.Idle -> {
                        Text("Choose export format:")
                        
                        // Format selection
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ExportFormatOption(
                                format = ExportFormat.PDF,
                                icon = Icons.Default.PictureAsPdf,
                                label = "PDF Document",
                                description = "Professional formatted report",
                                isSelected = selectedFormat == ExportFormat.PDF,
                                onClick = { selectedFormat = ExportFormat.PDF }
                            )
                            
                            ExportFormatOption(
                                format = ExportFormat.MARKDOWN,
                                icon = Icons.Default.Description,
                                label = "Markdown",
                                description = "Plain text with formatting",
                                isSelected = selectedFormat == ExportFormat.MARKDOWN,
                                onClick = { selectedFormat = ExportFormat.MARKDOWN }
                            )
                            
                            ExportFormatOption(
                                format = ExportFormat.JSON,
                                icon = Icons.Default.DataObject,
                                label = "JSON",
                                description = "Raw data format",
                                isSelected = selectedFormat == ExportFormat.JSON,
                                onClick = { selectedFormat = ExportFormat.JSON }
                            )
                        }
                    }
                    
                    is ExportState.Exporting -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Generating ${selectedFormat.name} report...")
                        }
                    }
                    
                    is ExportState.Success -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Export successful!")
                            Text(
                                text = "Size: ${formatFileSize(state.result.sizeBytes)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    is ExportState.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Export failed")
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            when (val state = exportState) {
                is ExportState.Idle -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            viewModel.resetState()
                            onDismiss()
                        }) {
                            Text("Cancel")
                        }
                        Button(onClick = {
                            viewModel.exportReport(report, selectedFormat)
                        }) {
                            Icon(Icons.Default.FileDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export")
                        }
                    }
                }
                
                is ExportState.Success -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            viewModel.resetState()
                            onDismiss()
                        }) {
                            Text("Close")
                        }
                        Button(onClick = {
                            viewModel.shareFile(state.result.file)
                        }) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share")
                        }
                    }
                }
                
                is ExportState.Error -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            viewModel.resetState()
                            onDismiss()
                        }) {
                            Text("Close")
                        }
                        Button(onClick = {
                            viewModel.resetState()
                        }) {
                            Text("Retry")
                        }
                    }
                }
                
                is ExportState.Exporting -> {
                    // No buttons while exporting
                }
            }
        },
        dismissButton = null
    )
}

@Composable
private fun ExportFormatOption(
    format: ExportFormat,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder()
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}

// Made with Bob