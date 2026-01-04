package com.petcare.app.data.model

data class PetUpdateRequest(
    val name: String,
    val species: String,
    val breed: String? = null,
    val age: Int? = null
)
