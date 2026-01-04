package com.petcare.app.data.model

data class PetDto(
    val id: Int,
    val name: String,
    val species: String,
    val breed: String? = null,
    val age: Int? = null
)
