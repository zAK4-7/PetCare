package com.petcare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.petcare.data.local.entities.AnimalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(animal: AnimalEntity)

    @Delete
    suspend fun delete(animal: AnimalEntity)

    @Query("SELECT * FROM animaux WHERE utilisateurId = :userId ORDER BY dateCreation DESC")
    fun observeAnimauxByUser(userId: String): Flow<List<AnimalEntity>>

    @Query("SELECT * FROM animaux WHERE utilisateurId = :userId ORDER BY dateCreation DESC")
    suspend fun getAnimauxByUser(userId: String): List<AnimalEntity>
}
