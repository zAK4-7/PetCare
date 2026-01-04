package com.petcare.app.data.model

data class PetCreateRequest(
    val name: String,
    val species: String,
    val breed: String? = null,
    val age: Int? = null
)
