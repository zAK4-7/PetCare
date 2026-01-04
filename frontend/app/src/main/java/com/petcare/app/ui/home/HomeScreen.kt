package com.petcare.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.petcare.app.data.model.AppointmentDto
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    petsCount: Int,
    appointments: List<AppointmentDto>,
    loadingPets: Boolean,
    loadingAgenda: Boolean,
    errorPets: String?,
    errorAgenda: String?,
    onRefresh: () -> Unit,
    onGoAgenda: () -> Unit,
    onAddAppointment: () -> Unit,
    onGoPets: () -> Unit,
    onAddPet: () -> Unit,
    onGoServices: () -> Unit,
    onGoProfile: () -> Unit
) {
    val nextAppt = remember(appointments) { findNextAppointment(appointments) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Accueil", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onRefresh) { Text("Rafraîchir") }
        }

        // ✅ Stats rapides
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                title = "Animaux",
                value = petsCount.toString(),
                subtitle = if (loadingPets) "Chargement..." else "Enregistrés",
                modifier = Modifier.weight(1f),
                onClick = onGoPets
            )
            StatCard(
                title = "Rendez-vous",
                value = appointments.size.toString(),
                subtitle = if (loadingAgenda) "Chargement..." else "Total",
                modifier = Modifier.weight(1f),
                onClick = onGoAgenda
            )
        }

        // ✅ Prochain RDV
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Prochain rendez-vous", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onGoAgenda) { Text("Voir tout") }
                }

                if (loadingAgenda) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                } else if (nextAppt == null) {
                    Text("Aucun rendez-vous à venir.")
                    Button(onClick = onAddAppointment) { Text("Ajouter un rendez-vous") }
                } else {
                    Text(nextAppt.title, style = MaterialTheme.typography.titleSmall)
                    Text(formatStartAt(nextAppt.startAt), style = MaterialTheme.typography.bodyMedium)
                    nextAppt.type?.takeIf { it.isNotBlank() }?.let {
                        AssistChip(onClick = {}, label = { Text(it) })
                    }
                    nextAppt.notes?.takeIf { it.isNotBlank() }?.let { Text(it) }
                }
            }
        }

        // ✅ Actions rapides
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Actions rapides", style = MaterialTheme.typography.titleMedium)

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onAddAppointment, modifier = Modifier.weight(1f)) { Text("➕ RDV") }
                    OutlinedButton(onClick = onAddPet, modifier = Modifier.weight(1f)) { Text("➕ Animal") }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onGoServices, modifier = Modifier.weight(1f)) { Text("🧰 Services") }
                    OutlinedButton(onClick = onGoProfile, modifier = Modifier.weight(1f)) { Text("👤 Profil") }
                }
            }
        }

        errorPets?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        errorAgenda?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun findNextAppointment(items: List<AppointmentDto>): AppointmentDto? {
    val now = System.currentTimeMillis()
    return items
        .mapNotNull { appt ->
            val millis = parseIsoToMillis(appt.startAt) ?: return@mapNotNull null
            appt to millis
        }
        .filter { it.second >= now }
        .minByOrNull { it.second }
        ?.first
}

private fun formatStartAt(startAt: String): String {
    return try {
        val odt = OffsetDateTime.parse(startAt)
        val date = odt.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val time = odt.toLocalTime().withSecond(0).withNano(0)
            .format(DateTimeFormatter.ofPattern("HH:mm"))
        "$date • $time"
    } catch (_: Throwable) {
        try {
            val ins = Instant.parse(startAt)
            val zdt = ins.atZone(ZoneId.systemDefault())
            val date = zdt.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val time = zdt.toLocalTime().withSecond(0).withNano(0)
                .format(DateTimeFormatter.ofPattern("HH:mm"))
            "$date • $time"
        } catch (_: Throwable) {
            startAt
        }
    }
}

private fun parseIsoToMillis(iso: String): Long? {
    return try {
        OffsetDateTime.parse(iso).toInstant().toEpochMilli()
    } catch (_: Throwable) {
        try { Instant.parse(iso).toEpochMilli() } catch (_: Throwable) { null }
    }
}
