package com.petcare.app.ui.agenda

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.petcare.app.data.model.AppointmentDto
import java.time.*
import java.time.format.DateTimeFormatter

@Composable
fun AgendaScreen(
    vm: AgendaViewModel,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit
) {
    val state by vm.uiState.collectAsState()
    var deleteTarget by remember { mutableStateOf<AppointmentDto?>(null) }

    LaunchedEffect(Unit) { vm.loadAgenda() }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Supprimer") },
            text = { Text("Supprimer ce rendez-vous ?") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteAppointment(
                        id = deleteTarget!!.id,
                        onDone = { deleteTarget = null },
                        onError = { deleteTarget = null }
                    )
                }) { Text("Oui") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Non") }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Agenda", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { vm.loadAgenda() }) { Text("Rafraîchir") }
                Button(onClick = onAdd) { Text("Ajouter") }
            }
        }

        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())

        if (!state.loading && state.items.isEmpty()) {
            Text("Aucun rendez-vous.")
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.items) { appt ->
                AgendaCard(
                    appt = appt,
                    onEdit = { onEdit(appt.id) },
                    onDelete = { deleteTarget = appt }
                )
            }
        }
    }
}

@Composable
private fun AgendaCard(
    appt: AppointmentDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(appt.title, style = MaterialTheme.typography.titleMedium)
                    Text(formatDateTime(appt.startAt), style = MaterialTheme.typography.bodySmall)
                    appt.endAt?.let { Text("Fin: ${formatDateTime(it)}", style = MaterialTheme.typography.bodySmall) }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onEdit) { Text("Modifier") }
                    TextButton(onClick = onDelete) { Text("Supprimer") }
                }
            }

            appt.type?.takeIf { it.isNotBlank() }?.let {
                AssistChip(onClick = {}, label = { Text(it) })
            }

            appt.notes?.takeIf { it.isNotBlank() }?.let { Text(it) }
        }
    }
}

private fun formatDateTime(iso: String): String {
    // supporte 2025-..+01:00 et 2025-..Z
    val zdt = try {
        OffsetDateTime.parse(iso).atZoneSameInstant(ZoneId.systemDefault())
    } catch (_: Throwable) {
        try { Instant.parse(iso).atZone(ZoneId.systemDefault()) }
        catch (_: Throwable) { return iso }
    }
    val d = zdt.toLocalDate()
    val t = zdt.toLocalTime().withSecond(0).withNano(0)
    return "${d} • ${t.format(DateTimeFormatter.ofPattern("HH:mm"))}"
}
