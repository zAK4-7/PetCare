package com.petcare.sync

import com.petcare.data.local.entities.AnimalEntity
import com.petcare.data.local.entities.EvenementSanteEntity
import com.petcare.data.local.entities.RappelEntity
import com.petcare.data.local.enums.EtatRappel
import com.petcare.data.local.enums.StatutSoin
import com.petcare.data.local.enums.TypeEvenement
import com.petcare.data.remote.api.PetCareApi
import com.petcare.data.repository.LocalDataRepository
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Minimal sync layer (Local SQLite <-> Salma API).
 *
 * Notes:
 * - This project currently keeps local IDs as Strings. For remote objects (Int ids),
 *   we store them locally with a deterministic "srv_<id>" string key.
 * - You can expand this with a proper mapping table and a pending operations queue.
 */
class SyncManager(
    private val api: PetCareApi,
    private val local: LocalDataRepository
) {

    suspend fun pullAll(currentLocalUserId: String) {
        // 1) Pull pets
        val pets = api.listPets()
        pets.forEach { p ->
            local.saveAnimal(
                AnimalEntity(
                    id = "srv_${p.id}",
                    utilisateurId = currentLocalUserId,
                    nom = p.name,
                    espece = p.species,
                    race = p.breed,
                    dateNaissance = p.birthDate?.let { parseIsoToMillis(it) },
                    poidsKg = null,
                    photo = p.photoUrl,
                    dateCreation = System.currentTimeMillis()
                )
            )

            // 2) Pull health events per pet
            val events = api.listHealthEvents(p.id)
            events.forEach { e ->
                val typeEnum = when (e.type.uppercase()) {
                    "VACCIN" -> TypeEvenement.VACCIN
                    "TRAITEMENT" -> TypeEvenement.TRAITEMENT
                    "CONSULTATION", "RENDEZ_VOUS", "RDV" -> TypeEvenement.RENDEZ_VOUS
                    else -> TypeEvenement.SOIN_AUTRE
                }
                local.saveEvenement(
                    EvenementSanteEntity(
                        id = "srv_${e.id}",
                        animalId = "srv_${p.id}",
                        type = typeEnum,
                        titre = e.title,
                        description = e.description,
                        dateDebut = parseIsoToMillis(e.eventDate),
                        dateFin = null,
                        statut = StatutSoin.EN_ATTENTE,
                        creeLe = System.currentTimeMillis()
                    )
                )

                // 3) Pull reminders
                val reminders = api.listReminders(e.id)
                reminders.forEach { r ->
                    local.saveRappel(
                        RappelEntity(
                            id = "srv_${r.id}",
                            evenementId = "srv_${e.id}",
                            dateRappel = parseIsoToMillis(r.remindAt),
                            message = null,
                            etat = if (r.sent) EtatRappel.ENVOYE else EtatRappel.PROGRAMME,
                            creeLe = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    /**
     * Helper for generating local IDs for new objects created offline.
     */
    fun newLocalId(): String = UUID.randomUUID().toString()

    private fun parseIsoToMillis(iso: String): Long {
        // Accept either full OffsetDateTime or Instant
        return try {
            OffsetDateTime.parse(iso).toInstant().toEpochMilli()
        } catch (_: Exception) {
            Instant.parse(iso).toEpochMilli()
        }
    }

    fun formatMillisToIso(millis: Long): String {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(Instant.ofEpochMilli(millis).atOffset(java.time.ZoneOffset.UTC))
    }
}
