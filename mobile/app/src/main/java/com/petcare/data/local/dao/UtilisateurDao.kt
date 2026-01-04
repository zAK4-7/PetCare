package com.petcare.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.petcare.data.local.entities.UtilisateurEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UtilisateurDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UtilisateurEntity)

    @Delete
    suspend fun delete(user: UtilisateurEntity)

    @Query("SELECT * FROM utilisateurs LIMIT 1")
    fun observeCurrentUser(): Flow<UtilisateurEntity?>

    @Query("SELECT * FROM utilisateurs LIMIT 1")
    suspend fun getCurrentUser(): UtilisateurEntity?
}
