package com.petcare.app.data.model

data class Pet(
    val id: String? = null,
    val _id: String? = null,

    val name: String = "",

    // Certains écrans/utilisateurs utilisent "species"
    val species: String? = null,

    // D'autres utilisent "type"
    val type: String? = null,

    val breed: String? = null,
    val age: Int? = null,
    val photoUrl: String? = null,
    val notes: String? = null
) {
    fun resolvedId(): String =
        (id?.takeIf { it.isNotBlank() }
            ?: _id?.takeIf { it.isNotBlank() }
            ?: "")

    fun resolvedSpecies(): String =
        (species?.takeIf { it.isNotBlank() }
            ?: type?.takeIf { it.isNotBlank() }
            ?: "")
}
