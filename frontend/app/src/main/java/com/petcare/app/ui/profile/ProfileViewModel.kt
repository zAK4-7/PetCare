package com.petcare.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petcare.app.data.model.MeDto
import com.petcare.app.data.model.MeUpdateRequest
import com.petcare.app.data.repo.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val loading: Boolean = false,
    val saving: Boolean = false,
    val me: MeDto? = null,
    val error: String? = null,
    val success: String? = null
)

class ProfileViewModel(
    private val repo: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    fun loadMe() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null, success = null) }
            runCatching { repo.fetchMe() }
                .onSuccess { me -> _uiState.update { it.copy(loading = false, me = me) } }
                .onFailure { e -> _uiState.update { it.copy(loading = false, error = e.message ?: "Erreur chargement") } }
        }
    }

    fun saveMe(body: MeUpdateRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true, error = null, success = null) }
            runCatching { repo.updateMe(body) }
                .onSuccess { me ->
                    _uiState.update { it.copy(saving = false, me = me, success = "Profil mis à jour ✅") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(saving = false, error = e.message ?: "Erreur mise à jour") }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, success = null) }
    }
}
