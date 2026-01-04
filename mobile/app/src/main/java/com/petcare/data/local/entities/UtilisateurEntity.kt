package com.petcare.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "utilisateurs")
data class UtilisateurEntity(
    @PrimaryKey
    val id: String,
    val nom: String,
    val email: String,
    val motDePasseHash: String?,
    val photoProfil: String?,
    val dateCreation: Long,
    val actif: Boolean,
    val sourceServeurId: String?,
    val pendingSync: Boolean,
    val derniereModification: Long?
)
