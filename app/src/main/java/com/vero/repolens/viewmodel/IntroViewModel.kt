package com.vero.repolens.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vero.repolens.data.models.AssetReportSource
import com.vero.repolens.data.models.DeviceReportSource
import com.vero.repolens.data.models.ReportSource
import com.vero.repolens.data.repository.RepoLensRepository
import com.vero.repolens.data.repository.RepoLensResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IntroUiState(
    val assetReports: List<AssetReportSource> = emptyList(),
    val selectedSource: ReportSource? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val canContinue: Boolean
        get() = selectedSource != null && !isLoading
}

@HiltViewModel
class IntroViewModel @Inject constructor(
    private val repository: RepoLensRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        IntroUiState(
            assetReports = repository.getAvailableAssetReports(),
            selectedSource = repository.getSelectedReportSource()
                ?: repository.getAvailableAssetReports().firstOrNull()
        )
    )
    val uiState: StateFlow<IntroUiState> = _uiState.asStateFlow()

    fun selectAsset(source: AssetReportSource) {
        _uiState.value = _uiState.value.copy(
            selectedSource = source,
            errorMessage = null
        )
    }

    fun selectDeviceFile(uri: Uri, displayName: String) {
        _uiState.value = _uiState.value.copy(
            selectedSource = DeviceReportSource(uri = uri, displayName = displayName),
            errorMessage = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun confirmSelection(onSuccess: () -> Unit) {
        val source = _uiState.value.selectedSource ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (repository.loadReportFromSource(source)) {
                is RepoLensResult.Success -> {
                    repository.setSelectedReportSource(source)
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
                    onSuccess()
                }
                is RepoLensResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "This file could not be parsed as a RepoLens report."
                    )
                }
            }
        }
    }
}

// Made with Bob
