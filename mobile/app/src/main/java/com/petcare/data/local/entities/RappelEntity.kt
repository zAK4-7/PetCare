package com.petcare.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.petcare.data.local.enums.EtatRappel

@Entity(tableName = "rappels")
data class RappelEntity(
    @PrimaryKey
    val id: String,
    val evenementId: String,
    val dateRappel: Long,
    val message: String?,
    val etat: EtatRappel,
    val creeLe: Long
)
