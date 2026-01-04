package com.petcare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.petcare.data.local.entities.EvenementSanteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EvenementSanteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(event: EvenementSanteEntity)

    @Delete
    suspend fun delete(event: EvenementSanteEntity)

    @Query("SELECT * FROM evenements_sante WHERE animalId = :animalId ORDER BY dateDebut DESC")
    fun observeEvenementsByAnimal(animalId: String): Flow<List<EvenementSanteEntity>>

    @Query("SELECT * FROM evenements_sante WHERE animalId = :animalId ORDER BY dateDebut DESC")
    suspend fun getEvenementsByAnimal(animalId: String): List<EvenementSanteEntity>
}
