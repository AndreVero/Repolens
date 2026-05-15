package com.vero.repolens.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vero.repolens.data.models.AssetReportSource
import com.vero.repolens.data.models.DeviceReportSource
import com.vero.repolens.data.models.ReportSource
import com.vero.repolens.ui.theme.InkPrimary
import com.vero.repolens.ui.theme.InkText
import com.vero.repolens.ui.theme.InkTextMuted
import com.vero.repolens.ui.theme.SandBackground
import com.vero.repolens.viewmodel.IntroViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun IntroScreen(
    onContinue: () -> Unit,
    viewModel: IntroViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val assetReports = uiState.assetReports
    val selectedAssetIndex = remember(uiState.selectedSource, assetReports) {
        val selectedAsset = uiState.selectedSource as? AssetReportSource
        selectedAsset?.let(assetReports::indexOf)?.takeIf { it >= 0 } ?: 0
    }
    val pagerState = rememberPagerState(
        initialPage = selectedAssetIndex,
        pageCount = { assetReports.size }
    )

    val documentLauncher = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.selectDeviceFile(uri, resolveDisplayName(context, uri))
        }
    }

    LaunchedEffect(selectedAssetIndex, assetReports.size) {
        if (assetReports.isNotEmpty() && pagerState.currentPage != selectedAssetIndex) {
            pagerState.animateScrollToPage(selectedAssetIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage, assetReports) {
        if (assetReports.isNotEmpty()) {
            val currentAsset = assetReports[pagerState.currentPage]
            if (uiState.selectedSource != currentAsset) {
                viewModel.selectAsset(currentAsset)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Report") }
            )
        },
        bottomBar = {
            SelectionFooter(
                selectedSource = uiState.selectedSource,
                isLoading = uiState.isLoading,
                enabled = uiState.canContinue,
                onContinue = { viewModel.confirmSelection(onContinue) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                IntroHeaderCard(assetCount = assetReports.size)
            }

            if (assetReports.isNotEmpty()) {
                item {
                    SectionLabel(text = "Bundled reports")
                }

                item {
                    BundledReportsPager(
                        reports = assetReports,
                        selectedIndex = selectedAssetIndex,
                        pagerState = pagerState,
                        onSelect = viewModel::selectAsset
                    )
                }
            }

            item {
                SectionLabel(text = "Import from device")
            }

            item {
                OutlinedButton(
                    onClick = { documentLauncher.launch(arrayOf("application/json", "text/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.FileOpen,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose JSON From Device")
                }
            }

            val selectedDevice = uiState.selectedSource as? DeviceReportSource
            if (selectedDevice != null) {
                item {
                    ReportSourceRow(
                        title = prettifyReportName(selectedDevice.displayName),
                        subtitle = "Imported file",
                        supporting = selectedDevice.displayName,
                        selected = true,
                        leadingIcon = Icons.Default.Description,
                        onClick = {}
                    )
                }
            }

            uiState.errorMessage?.let { message ->
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun IntroHeaderCard(assetCount: Int) {
    Surface(
        tonalElevation = 0.dp,
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Open a RepoLens report",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Choose a bundled sample or import your own JSON report to begin exploring the analysis.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
            )
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "$assetCount bundled reports available",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 6.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BundledReportsPager(
    reports: List<AssetReportSource>,
    selectedIndex: Int,
    pagerState: androidx.compose.foundation.pager.PagerState,
    onSelect: (AssetReportSource) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val asset = reports[page]
            Box(modifier = Modifier.padding(horizontal = 2.dp)) {
                ReportSourceRow(
                    title = prettifyReportName(asset.displayName),
                    subtitle = "Bundled sample",
                    supporting = asset.displayName,
                    selected = selectedIndex == page,
                    leadingIcon = Icons.Default.FolderZip,
                    onClick = { onSelect(asset) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            reports.indices.forEach { index ->
                val isSelected = index == selectedIndex
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = if (isSelected) 18.dp else 8.dp, height = 8.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    },
                    shape = MaterialTheme.shapes.small
                ) {}
            }
        }
    }
}

@Composable
private fun ReportSourceRow(
    title: String,
    subtitle: String,
    supporting: String,
    selected: Boolean,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val accentColor = MaterialTheme.colorScheme.primary
    val borderColor = if (selected) accentColor else MaterialTheme.colorScheme.outline
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, borderColor),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SelectionIndicator(selected = selected)

            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SelectionIndicator(selected: Boolean) {
    val accentColor = MaterialTheme.colorScheme.primary
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (selected) accentColor else Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (selected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
        ),
        modifier = Modifier.size(20.dp)
    ) {
        if (selected) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier.size(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.White,
                        shape = MaterialTheme.shapes.small
                    ) {}
                }
            }
        }
    }
}

@Composable
private fun SelectionFooter(
    selectedSource: ReportSource?,
    isLoading: Boolean,
    enabled: Boolean,
    onContinue: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (selectedSource is DeviceReportSource) {
                            Icons.AutoMirrored.Filled.OpenInNew
                        } else {
                            Icons.Default.Description
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Selected report",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = selectedSource?.let { prettifyReportName(it.displayName) } ?: "Choose a report to continue",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Button(
                onClick = onContinue,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isLoading) "Opening report..." else "Open Report")
            }
        }
    }
}

private fun resolveDisplayName(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && it.moveToFirst()) {
            return it.getString(nameIndex)
        }
    }
    return uri.lastPathSegment ?: "selected_report.json"
}

private fun prettifyReportName(fileName: String): String {
    return fileName
        .removeSuffix(".json")
        .split('_', '-', ' ')
        .filter { it.isNotBlank() }
        .joinToString(" ") { token ->
            token.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
        }
}

// Made with Bob
