package com.petcare.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.petcare.data.local.enums.StatutSoin
import com.petcare.data.local.enums.TypeEvenement

@Entity(tableName = "evenements_sante")
data class EvenementSanteEntity(
    @PrimaryKey
    val id: String,
    val animalId: String,
    val type: TypeEvenement,
    val titre: String,
    val description: String?,
    val dateDebut: Long,
    val dateFin: Long?,
    val statut: StatutSoin,
    val creeLe: Long
)
