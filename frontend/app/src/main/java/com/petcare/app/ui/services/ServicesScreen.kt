package com.petcare.app.ui.services

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.petcare.app.data.model.ServiceDto

@Composable
fun ServicesScreen(
    vm: ServicesViewModel,
    onBook: (ServiceDto) -> Unit
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vm.load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text("Services", style = MaterialTheme.typography.titleLarge)

        // 🔎 Filtres
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ServiceFilter("Tous", state.selectedType == null) {
                vm.setType(null)
            }
            ServiceFilter("Vétérinaire", state.selectedType == "VETERINAIRE") {
                vm.setType("VETERINAIRE")
            }
            ServiceFilter("Toiletteur", state.selectedType == "TOILETTEUR") {
                vm.setType("TOILETTEUR")
            }
            ServiceFilter("Autre", state.selectedType == "AUTRE") {
                vm.setType("AUTRE")
            }
        }

        if (state.loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        if (!state.loading && state.items.isEmpty()) {
            Text("Aucun service trouvé.")
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.items) { service ->
                ServiceCard(service = service, onBook = { onBook(service) })
            }
        }
    }
}

@Composable
private fun ServiceFilter(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}

@Composable
private fun ServiceCard(
    service: ServiceDto,
    onBook: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(service.name, style = MaterialTheme.typography.titleMedium)
                    Text(service.typeLabel(), style = MaterialTheme.typography.bodySmall)
                }
                Button(onClick = onBook) {
                    Text("Réserver")
                }
            }

            service.address?.let { Text("📍 $it") }
            service.phone?.let { Text("📞 $it") }
            service.hours?.let { Text("🕒 $it") }
        }
    }
}

private fun ServiceDto.typeLabel(): String = when (type) {
    "VETERINAIRE" -> "Vétérinaire"
    "TOILETTEUR" -> "Toiletteur"
    "AUTRE" -> "Autre"
    else -> type
}
