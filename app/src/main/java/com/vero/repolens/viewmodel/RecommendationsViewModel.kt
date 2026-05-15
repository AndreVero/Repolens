package com.vero.repolens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vero.repolens.data.engine.RecommendationEngine
import com.vero.repolens.data.models.*
import com.vero.repolens.data.repository.RepoLensRepository
import com.vero.repolens.data.repository.RepoLensResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendationsViewModel @Inject constructor(
    private val repository: RepoLensRepository,
    private val recommendationEngine: RecommendationEngine
) : ViewModel() {

    private val _recommendations = MutableStateFlow<List<SmartRecommendation>>(emptyList())
    val recommendations: StateFlow<List<SmartRecommendation>> = _recommendations.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _filterImpact = MutableStateFlow<ImpactLevel?>(null)
    val selectedImpact: StateFlow<ImpactLevel?> = _filterImpact.asStateFlow()

    private val _filterEffort = MutableStateFlow<EffortLevel?>(null)
    val selectedEffort: StateFlow<EffortLevel?> = _filterEffort.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val filteredRecommendations: StateFlow<List<SmartRecommendation>> = combine(
        _recommendations,
        _selectedCategory,
        _filterImpact,
        _filterEffort
    ) { recs, category, impact, effort ->
        recs.filter { rec ->
            val categoryMatch = category == null || rec.category == category
            val impactMatch = impact == null || rec.impact == impact
            val effortMatch = effort == null || rec.effort == effort
            categoryMatch && impactMatch && effortMatch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadRecommendations()
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = repository.loadReport()) {
                is RepoLensResult.Success -> {
                    val generated = recommendationEngine.generateRecommendations(result.report)
                    _recommendations.value = generated
                }
                is RepoLensResult.Error -> {
                    // Handle error
                }
            }
            _isLoading.value = false
        }
    }

    fun setCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setImpactFilter(impact: ImpactLevel?) {
        _filterImpact.value = impact
    }

    fun setImpact(impact: ImpactLevel?) {
        _filterImpact.value = impact
    }

    fun setEffortFilter(effort: EffortLevel?) {
        _filterEffort.value = effort
    }

    fun setEffort(effort: EffortLevel?) {
        _filterEffort.value = effort
    }

    fun clearFilters() {
        _selectedCategory.value = null
        _filterImpact.value = null
        _filterEffort.value = null
    }
}

// Made with Bob
