package com.petcare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.petcare.data.local.entities.RappelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RappelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(reminder: RappelEntity)

    @Delete
    suspend fun delete(reminder: RappelEntity)

    @Query("SELECT * FROM rappels WHERE evenementId = :eventId ORDER BY dateRappel ASC")
    fun observeRappelsByEvent(eventId: String): Flow<List<RappelEntity>>

    @Query("SELECT * FROM rappels WHERE evenementId = :eventId ORDER BY dateRappel ASC")
    suspend fun getRappelsByEvent(eventId: String): List<RappelEntity>
}
