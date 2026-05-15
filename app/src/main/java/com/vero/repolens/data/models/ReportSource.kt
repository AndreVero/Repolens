package com.vero.repolens.data.models

import android.net.Uri

sealed interface ReportSource {
    val displayName: String
}

data class AssetReportSource(
    val assetPath: String
) : ReportSource {
    override val displayName: String
        get() = assetPath.substringAfterLast('/')
}

data class DeviceReportSource(
    val uri: Uri,
    override val displayName: String
) : ReportSource

// Made with Bob
