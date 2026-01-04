package com.petcare.app.ui.pets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.petcare.app.data.model.PetDto

@Composable
fun PetsScreen(
    vm: PetsViewModel,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) { vm.loadPets() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Mes animaux", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = { vm.loadPets() }) { Text("Rafraîchir") }
        }

        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())

        if (!state.loading && state.pets.isEmpty()) {
            Text("Aucun animal. Clique sur + pour ajouter.")
            Spacer(Modifier.height(8.dp))
            Button(onClick = onAdd) { Text("Ajouter un animal") }
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.pets) { pet ->
                PetCard(pet = pet, onEdit = { onEdit(pet.id) })
            }
        }
    }
}

@Composable
private fun PetCard(
    pet: PetDto,
    onEdit: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(pet.name, style = MaterialTheme.typography.titleMedium)
                    val details = listOfNotNull(
                        pet.species.takeIf { it.isNotBlank() },
                        pet.breed?.takeIf { it.isNotBlank() },
                        pet.age?.let { "Âge: $it" }
                    ).joinToString(" • ")
                    if (details.isNotBlank()) Text(details)
                }

                TextButton(onClick = onEdit) { Text("Modifier") }
            }
        }
    }
}
