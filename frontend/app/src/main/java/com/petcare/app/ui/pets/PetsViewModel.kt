package com.petcare.app.ui.pets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petcare.app.data.model.PetCreateRequest
import com.petcare.app.data.model.PetDto
import com.petcare.app.data.model.PetUpdateRequest
import com.petcare.app.data.repo.PetsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PetsUiState(
    val loading: Boolean = false,
    val pets: List<PetDto> = emptyList(),
    val error: String? = null
)

class PetsViewModel : ViewModel() {

    private val repo = PetsRepository()

    private val _uiState = MutableStateFlow(PetsUiState())
    val uiState: StateFlow<PetsUiState> = _uiState

    fun loadPets() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            runCatching { repo.fetchPets() }
                .onSuccess { pets -> _uiState.update { it.copy(loading = false, pets = pets) } }
                .onFailure { _uiState.update { it.copy(loading = false, error = "Erreur chargement animaux") } }
        }
    }

    fun addPet(
        name: String,
        species: String,
        breed: String?,
        age: Int?,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) {
        val n = name.trim()
        val sp = species.trim()

        if (n.isBlank()) { onError("Le nom est obligatoire"); return }
        if (sp.isBlank()) { onError("L'espèce est obligatoire"); return }

        viewModelScope.launch {
            runCatching {
                repo.createPet(
                    PetCreateRequest(
                        name = n,
                        species = sp,
                        breed = breed?.trim()?.takeIf { it.isNotBlank() },
                        age = age
                    )
                )
            }.onSuccess {
                loadPets()
                onDone()
            }.onFailure {
                onError("Erreur lors de l'ajout")
            }
        }
    }

    fun updatePet(
        id: Int,
        name: String,
        species: String,
        breed: String?,
        age: Int?,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) {
        val n = name.trim()
        val sp = species.trim()

        if (n.isBlank()) { onError("Le nom est obligatoire"); return }
        if (sp.isBlank()) { onError("L'espèce est obligatoire"); return }

        viewModelScope.launch {
            runCatching {
                repo.updatePet(
                    id,
                    PetUpdateRequest(
                        name = n,
                        species = sp,
                        breed = breed?.trim()?.takeIf { it.isNotBlank() },
                        age = age
                    )
                )
            }.onSuccess {
                loadPets()
                onDone()
            }.onFailure {
                onError("Erreur lors de la modification")
            }
        }
    }

    fun deletePet(
        id: Int,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            runCatching { repo.deletePet(id) }
                .onSuccess {
                    loadPets()
                    onDone()
                }
                .onFailure {
                    onError("Erreur lors de la suppression")
                }
        }
    }
}
