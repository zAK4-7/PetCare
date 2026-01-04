package com.petcare.app.data.model

data class AppointmentCreateRequest(
    val title: String,
    val type: String? = null,
    val startAt: String,
    val endAt: String? = null,
    val notes: String? = null
)

