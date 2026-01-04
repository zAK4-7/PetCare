package com.petcare.app.data.model

data class AppointmentUpdateRequest(
    val title: String? = null,
    val type: String? = null,
    val startAt: String? = null,
    val endAt: String? = null,
    val notes: String? = null
)
