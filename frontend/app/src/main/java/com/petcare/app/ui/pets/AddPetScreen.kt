package com.petcare.app.ui.pets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetScreen(
    vm: PetsViewModel,
    petId: Int? = null,
    onBack: () -> Unit
) {
    val state by vm.uiState.collectAsState()

    val existing = remember(state.pets, petId) {
        petId?.let { id -> state.pets.firstOrNull { it.id == id } }
    }

    val isEdit = petId != null

    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Prefill si edit
    LaunchedEffect(existing?.id) {
        if (existing != null) {
            name = existing.name
            species = existing.species
            breed = existing.breed.orEmpty()
            ageText = existing.age?.toString().orEmpty()
        }
    }

    if (showDeleteConfirm && petId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer") },
            text = { Text("Supprimer cet animal ?") },
            confirmButton = {
                TextButton(onClick = {
                    loading = true
                    error = null
                    vm.deletePet(
                        id = petId,
                        onDone = {
                            loading = false
                            showDeleteConfirm = false
                            onBack()
                        },
                        onError = { msg ->
                            loading = false
                            showDeleteConfirm = false
                            error = msg
                        }
                    )
                }) { Text("Oui") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Non") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Modifier un animal" else "Ajouter un animal") },
                navigationIcon = { IconButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; error = null },
                label = { Text("Nom *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = species,
                onValueChange = { species = it; error = null },
                label = { Text("Espèce *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = breed,
                onValueChange = { breed = it; error = null },
                label = { Text("Race (optionnel)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ageText,
                onValueChange = { ageText = it.filter { ch -> ch.isDigit() }; error = null },
                label = { Text("Âge (optionnel)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val age = ageText.toIntOrNull()

                    loading = true
                    error = null

                    if (!isEdit) {
                        vm.addPet(
                            name = name,
                            species = species,
                            breed = breed.takeIf { it.isNotBlank() },
                            age = age,
                            onDone = { loading = false; onBack() },
                            onError = { msg -> loading = false; error = msg }
                        )
                    } else {
                        vm.updatePet(
                            id = petId!!,
                            name = name,
                            species = species,
                            breed = breed.takeIf { it.isNotBlank() },
                            age = age,
                            onDone = { loading = false; onBack() },
                            onError = { msg -> loading = false; error = msg }
                        )
                    }
                }
            ) {
                Text(if (loading) "Enregistrement..." else if (isEdit) "Enregistrer" else "Ajouter")
            }

            if (isEdit) {
                OutlinedButton(
                    enabled = !loading,
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Supprimer")
                }
            }
        }
    }
}
