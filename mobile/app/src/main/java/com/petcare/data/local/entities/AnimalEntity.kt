package com.petcare.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animaux")
data class AnimalEntity(
    @PrimaryKey
    val id: String,
    val utilisateurId: String,
    val nom: String,
    val espece: String,
    val race: String?,
    val dateNaissance: Long?,
    val poidsKg: Double?,
    val photo: String?,
    val dateCreation: Long
)
