package com.petcare.app.data.repo

import com.petcare.app.data.model.Pet

/**
 * fetchPets() retourne une LIST (pas Result).
 * Donc on ne fait PAS fold(onSuccess/onFailure).
 */
suspend fun PetsRepository.fetchPet(petId: String): Result<Pet> {
    return try {
        val anyList = fetchPets() // <- LIST
        val pets = anyList as List<Pet>

        val pet = pets.firstOrNull { it.resolvedId() == petId }
            ?: return Result.failure(IllegalArgumentException("Animal introuvable (id=$petId)"))

        Result.success(pet)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
