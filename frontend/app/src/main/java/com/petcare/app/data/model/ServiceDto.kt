package com.petcare.app.data.model

data class ServiceDto(
    val id: Int,
    val name: String,
    val type: String,
    val address: String? = null,
    val phone: String? = null,
    val hours: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
)
