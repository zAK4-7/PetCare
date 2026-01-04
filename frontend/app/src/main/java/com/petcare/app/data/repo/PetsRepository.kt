package com.petcare.app.data.repo

import com.petcare.app.data.api.ApiClient
import com.petcare.app.data.model.PetCreateRequest
import com.petcare.app.data.model.PetDto
import com.petcare.app.data.model.PetUpdateRequest

class PetsRepository {

    suspend fun fetchPets(): List<PetDto> =
        ApiClient.api.getPets()

    suspend fun createPet(body: PetCreateRequest): PetDto =
        ApiClient.api.createPet(body)

    suspend fun updatePet(id: Int, body: PetUpdateRequest): PetDto =
        ApiClient.api.updatePet(id, body)

    suspend fun deletePet(id: Int) {
        ApiClient.api.deletePet(id)
    }
}
