package com.petcare.app.ui.agenda

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.petcare.app.data.model.AppointmentCreateRequest
import com.petcare.app.data.model.AppointmentUpdateRequest
import java.time.*
import java.time.format.DateTimeFormatter

@Composable
fun AddAppointmentScreen(
    vm: AgendaViewModel,
    appointmentId: Int?,
    onBack: () -> Unit,
    prefillTitle: String? = null,
    prefillType: String? = null,
    prefillNotes: String? = null
) {
    val state by vm.uiState.collectAsState()

    val existing = remember(state.items, appointmentId) {
        appointmentId?.let { id -> state.items.firstOrNull { it.id == id } }
    }

    val isEdit = appointmentId != null

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var type by remember { mutableStateOf(existing?.type ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }

    // ✅ applique le prefill seulement si création (pas edit) et champs vides
    LaunchedEffect(prefillTitle, prefillType, prefillNotes, appointmentId) {
        if (appointmentId == null) {
            if (title.isBlank()) title = prefillTitle ?: title
            if (type.isBlank()) type = prefillType ?: type
            if (notes.isBlank()) notes = prefillNotes ?: notes
        }
    }


    // champs séparés
    var date by remember { mutableStateOf(existing?.startAt?.let { isoToLocalDate(it) } ?: "") } // yyyy-MM-dd
    var time by remember { mutableStateOf(existing?.startAt?.let { isoToLocalTime(it) } ?: "") } // HH:mm

    var endDate by remember { mutableStateOf(existing?.endAt?.let { isoToLocalDate(it) } ?: "") }
    var endTime by remember { mutableStateOf(existing?.endAt?.let { isoToLocalTime(it) } ?: "") }

    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            if (isEdit) "Modifier rendez-vous" else "Ajouter rendez-vous",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it; error = null },
            label = { Text("Titre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = type,
            onValueChange = { type = it; error = null },
            label = { Text("Type (optionnel)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Début: Date + Heure
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = date,
                onValueChange = { date = it; error = null },
                label = { Text("Date (yyyy-MM-dd)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = time,
                onValueChange = { time = it; error = null },
                label = { Text("Heure (HH:mm)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        // Fin: Date + Heure (optionnel)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it; error = null },
                label = { Text("Fin date (optionnel)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = endTime,
                onValueChange = { endTime = it; error = null },
                label = { Text("Fin heure (optionnel)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it; error = null },
            label = { Text("Notes (optionnel)") },
            modifier = Modifier.fillMaxWidth()
        )

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onBack, enabled = !saving) { Text("Retour") }

            Button(
                enabled = !saving,
                onClick = {
                    val t = title.trim()
                    if (t.isEmpty()) { error = "Titre obligatoire"; return@Button }

                    if (!isValidDate(date)) { error = "Date invalide (yyyy-MM-dd)"; return@Button }
                    if (!isValidTime(time)) { error = "Heure invalide (HH:mm)"; return@Button }

                    // ✅ ISO UTC ...Z (plus compatible avec zod datetime)
                    val startAtIso = toIsoUtcZ(date.trim(), time.trim())

                    val endAtIso = if (endDate.trim().isEmpty() && endTime.trim().isEmpty()) {
                        null
                    } else {
                        if (!isValidDate(endDate)) { error = "Fin date invalide"; return@Button }
                        if (!isValidTime(endTime)) { error = "Fin heure invalide"; return@Button }
                        toIsoUtcZ(endDate.trim(), endTime.trim())
                    }

                    val typeClean = type.trim().takeIf { it.isNotEmpty() }
                    val notesClean = notes.trim().takeIf { it.isNotEmpty() }

                    saving = true
                    error = null

                    if (!isEdit) {
                        vm.createAppointment(
                            body = AppointmentCreateRequest(
                                title = t,
                                type = typeClean,
                                startAt = startAtIso,
                                endAt = endAtIso,
                                notes = notesClean
                            ),
                            onDone = {
                                saving = false
                                onBack()
                            },
                            onError = { msg ->
                                saving = false
                                error = msg
                            }
                        )
                    } else {
                        vm.updateAppointment(
                            id = appointmentId!!,
                            body = AppointmentUpdateRequest(
                                title = t,
                                type = typeClean,
                                startAt = startAtIso,
                                endAt = endAtIso,
                                notes = notesClean
                            ),
                            onDone = {
                                saving = false
                                onBack()
                            },
                            onError = { msg ->
                                saving = false
                                error = msg
                            }
                        )
                    }
                }
            ) {
                if (saving) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        Text("Enregistrement...")
                    }
                } else {
                    Text(if (isEdit) "Enregistrer" else "Créer")
                }
            }
        }
    }
}

private fun isValidDate(s: String): Boolean = try {
    LocalDate.parse(s.trim(), DateTimeFormatter.ISO_LOCAL_DATE); true
} catch (_: Throwable) { false }

private fun isValidTime(s: String): Boolean = try {
    LocalTime.parse(s.trim(), DateTimeFormatter.ofPattern("HH:mm")); true
} catch (_: Throwable) { false }

/**
 * ✅ Format ultra compatible: 2025-12-14T10:30:00Z
 * (UTC, suffixe Z) -> accepté par zod datetime() dans tous les cas
 */
private fun toIsoUtcZ(date: String, time: String): String {
    val d = LocalDate.parse(date.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
    val t = LocalTime.parse(time.trim(), DateTimeFormatter.ofPattern("HH:mm"))
        .withSecond(0).withNano(0)

    val zdtLocal = ZonedDateTime.of(d, t, ZoneId.systemDefault())
    val instant = zdtLocal.toInstant()

    return DateTimeFormatter.ISO_INSTANT.format(instant)
}

/**
 * Supporte aussi ...Z (UTC) ou avec offset
 */
private fun isoToLocalDate(iso: String): String = try {
    OffsetDateTime.parse(iso).toLocalDate().toString()
} catch (_: Throwable) {
    try { Instant.parse(iso).atZone(ZoneId.systemDefault()).toLocalDate().toString() }
    catch (_: Throwable) { "" }
}

private fun isoToLocalTime(iso: String): String = try {
    val lt = OffsetDateTime.parse(iso).toLocalTime().withSecond(0).withNano(0)
    lt.format(DateTimeFormatter.ofPattern("HH:mm"))
} catch (_: Throwable) {
    try {
        val lt = Instant.parse(iso).atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0)
        lt.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (_: Throwable) { "" }
}
