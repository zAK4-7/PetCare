package com.petcare.data.repository

import com.petcare.data.local.database.PetCareDatabase
import com.petcare.data.local.entities.AnimalEntity
import com.petcare.data.local.entities.EvenementSanteEntity
import com.petcare.data.local.entities.RappelEntity
import com.petcare.data.local.entities.UtilisateurEntity
import kotlinx.coroutines.flow.Flow

class LocalDataRepository(
    private val database: PetCareDatabase
) {

    private val utilisateurDao = database.utilisateurDao()
    private val animalDao = database.animalDao()
    private val evenementSanteDao = database.evenementSanteDao()
    private val rappelDao = database.rappelDao()

    // Utilisateur
    suspend fun saveUtilisateur(user: UtilisateurEntity) {
        utilisateurDao.insertOrUpdate(user)
    }

    fun observeCurrentUser(): Flow<UtilisateurEntity?> =
        utilisateurDao.observeCurrentUser()

    // Animal
    suspend fun saveAnimal(animal: AnimalEntity) {
        animalDao.insertOrUpdate(animal)
    }

    fun observeAnimauxByUser(userId: String): Flow<List<AnimalEntity>> =
        animalDao.observeAnimauxByUser(userId)

    // EvenementSante
    suspend fun saveEvenement(event: EvenementSanteEntity) {
        evenementSanteDao.insertOrUpdate(event)
    }

    fun observeEvenementsByAnimal(animalId: String): Flow<List<EvenementSanteEntity>> =
        evenementSanteDao.observeEvenementsByAnimal(animalId)

    // Rappel
    suspend fun saveRappel(reminder: RappelEntity) {
        rappelDao.insertOrUpdate(reminder)
    }

    fun observeRappelsByEvent(eventId: String): Flow<List<RappelEntity>> =
        rappelDao.observeRappelsByEvent(eventId)
}
