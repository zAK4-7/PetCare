package com.petcare.app.ui.pets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDetailScreen(
    petId: String,
    onBack: () -> Unit,
    vm: PetDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(petId) {
        vm.load(petId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détail") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Retour") } }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (val s = state) {
                is PetDetailUiState.Loading -> CircularProgressIndicator()
                is PetDetailUiState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error)
                is PetDetailUiState.Success -> {
                    val pet = s.pet
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(pet.name.ifBlank { "Sans nom" }, style = MaterialTheme.typography.headlineSmall)

                        val sp = pet.resolvedSpecies()
                        if (sp.isNotBlank()) Text("Espèce: $sp")

                        pet.breed?.takeIf { it.isNotBlank() }?.let { Text("Race: $it") }
                        pet.age?.let { Text("Âge: $it") }
                        pet.notes?.takeIf { it.isNotBlank() }?.let { Text("Notes: $it") }
                    }
                }
            }
        }
    }
}
