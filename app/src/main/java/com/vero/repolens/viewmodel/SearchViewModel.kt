package com.vero.repolens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vero.repolens.data.models.SearchableItem
import com.vero.repolens.data.models.SearchFilter
import com.vero.repolens.data.models.SearchItemType
import com.vero.repolens.data.repository.RepoLensRepository
import com.vero.repolens.data.repository.RepoLensResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: RepoLensRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeFilter = MutableStateFlow(SearchFilter())
    val activeFilter: StateFlow<SearchFilter> = _activeFilter.asStateFlow()

    private val _allItems = MutableStateFlow<List<SearchableItem>>(emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Debounced search results
    val searchResults: StateFlow<List<SearchableItem>> = combine(
        _searchQuery.debounce(300), // 300ms debounce
        _activeFilter,
        _allItems
    ) { query, filter, items ->
        if (items.isEmpty()) return@combine emptyList()
        
        val searched = repository.searchItems(items, query)
        searched.filter { filter.matches(it) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Available filter options
    val availableSeverities: StateFlow<Set<String>> = _allItems.map { items ->
        items.mapNotNull { it.severity }.toSet()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    val availableCategories: StateFlow<Set<String>> = _allItems.map { items ->
        items.map { it.category }.toSet()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    init {
        loadSearchableItems()
    }

    private fun loadSearchableItems() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = repository.loadReport()) {
                is RepoLensResult.Success -> {
                    _allItems.value = repository.indexSearchableItems(result.report)
                }
                is RepoLensResult.Error -> {
                    // Handle error - items remain empty
                }
            }
            _isLoading.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleTypeFilter(type: SearchItemType) {
        val currentTypes = _activeFilter.value.types.toMutableSet()
        if (currentTypes.contains(type)) {
            currentTypes.remove(type)
        } else {
            currentTypes.add(type)
        }
        _activeFilter.value = _activeFilter.value.copy(types = currentTypes)
    }

    fun toggleSeverityFilter(severity: String) {
        val currentSeverities = _activeFilter.value.severities.toMutableSet()
        if (currentSeverities.contains(severity)) {
            currentSeverities.remove(severity)
        } else {
            currentSeverities.add(severity)
        }
        _activeFilter.value = _activeFilter.value.copy(severities = currentSeverities)
    }

    fun toggleCategoryFilter(category: String) {
        val currentCategories = _activeFilter.value.categories.toMutableSet()
        if (currentCategories.contains(category)) {
            currentCategories.remove(category)
        } else {
            currentCategories.add(category)
        }
        _activeFilter.value = _activeFilter.value.copy(categories = currentCategories)
    }

    fun clearFilters() {
        _activeFilter.value = SearchFilter()
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }
}

// Made with Bob