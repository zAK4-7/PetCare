package com.petcare.app.ui.pets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petcare.app.data.model.Pet
import com.petcare.app.data.repo.PetsRepository
import com.petcare.app.data.repo.fetchPet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PetDetailUiState {
    object Loading : PetDetailUiState()
    data class Error(val message: String) : PetDetailUiState()
    data class Success(val pet: Pet) : PetDetailUiState()
}

class PetDetailViewModel(
    private val repo: PetsRepository = PetsRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<PetDetailUiState>(PetDetailUiState.Loading)
    val state: StateFlow<PetDetailUiState> = _state.asStateFlow()

    fun load(petId: String) {
        _state.value = PetDetailUiState.Loading
        viewModelScope.launch {
            repo.fetchPet(petId)
                .onSuccess { pet -> _state.value = PetDetailUiState.Success(pet) }
                .onFailure { e -> _state.value = PetDetailUiState.Error(e.message ?: "Erreur chargement détail") }
        }
    }
}
