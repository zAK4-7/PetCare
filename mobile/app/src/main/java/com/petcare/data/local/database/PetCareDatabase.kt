package com.petcare.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.petcare.data.local.dao.AnimalDao
import com.petcare.data.local.dao.EvenementSanteDao
import com.petcare.data.local.dao.RappelDao
import com.petcare.data.local.dao.UtilisateurDao
import com.petcare.data.local.entities.AnimalEntity
import com.petcare.data.local.entities.EvenementSanteEntity
import com.petcare.data.local.entities.RappelEntity
import com.petcare.data.local.entities.UtilisateurEntity

@Database(
    entities = [
        UtilisateurEntity::class,
        AnimalEntity::class,
        EvenementSanteEntity::class,
        RappelEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(EnumConverters::class)
abstract class PetCareDatabase : RoomDatabase() {

    abstract fun utilisateurDao(): UtilisateurDao
    abstract fun animalDao(): AnimalDao
    abstract fun evenementSanteDao(): EvenementSanteDao
    abstract fun rappelDao(): RappelDao
}
