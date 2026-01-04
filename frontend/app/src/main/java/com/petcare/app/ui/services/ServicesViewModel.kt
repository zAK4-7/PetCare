package com.petcare.app.ui.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petcare.app.data.model.ServiceDto
import com.petcare.app.data.repo.ServicesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ServicesUiState(
    val loading: Boolean = false,
    val items: List<ServiceDto> = emptyList(),
    val selectedType: String? = null,
    val error: String? = null
)

class ServicesViewModel(
    private val repo: ServicesRepository = ServicesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServicesUiState())
    val uiState: StateFlow<ServicesUiState> = _uiState.asStateFlow()

    fun load(type: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                loading = true,
                error = null,
                selectedType = type
            )

            runCatching {
                repo.fetchServices(type)
            }.onSuccess { list ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    items = list
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    items = emptyList(),
                    error = e.message ?: "Erreur chargement services"
                )
            }
        }
    }

    fun setType(type: String?) {
        load(type)
    }

    fun refresh() {
        load(_uiState.value.selectedType)
    }
}
