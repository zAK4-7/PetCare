package com.petcare.app.data.model

data class MeUpdateRequest(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val timezone: String? = null,
    val language: String? = null,
    val password: String? = null
)
