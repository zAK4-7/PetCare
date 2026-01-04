package com.petcare.app.data.model

data class AppointmentDto(
    val id: Int,
    val userId: Int? = null,
    val title: String,
    val type: String? = null,
    val startAt: String,
    val endAt: String? = null,
    val notes: String? = null
)

