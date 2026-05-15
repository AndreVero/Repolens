package com.vero.repolens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vero.repolens.data.models.RepoLensReport
import com.vero.repolens.data.repository.RepoLensRepository
import com.vero.repolens.data.repository.RepoLensResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiState {
    object Loading : UiState()
    data class Success(val report: RepoLensReport) : UiState()
    data class Error(val message: String) : UiState()
}

@HiltViewModel
class RepoLensViewModel @Inject constructor(
    private val repository: RepoLensRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadReport() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = repository.loadReport()) {
                is RepoLensResult.Success -> {
                    _uiState.value = UiState.Success(result.report)
                }
                is RepoLensResult.Error -> {
                    _uiState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun retry() {
        loadReport()
    }
}

// Made with Bob
