package com.petcare.app.data.repo

import com.petcare.app.data.api.ApiClient
import com.petcare.app.data.model.*

class AgendaRepository {

    suspend fun fetchAppointments(): List<AppointmentDto> =
        ApiClient.api.getAppointments()

    suspend fun createAppointment(body: AppointmentCreateRequest): AppointmentDto =
        ApiClient.api.createAppointment(body)

    suspend fun updateAppointment(id: Int, body: AppointmentUpdateRequest): AppointmentDto =
        ApiClient.api.updateAppointment(id, body)

    suspend fun deleteAppointment(id: Int) {
        ApiClient.api.deleteAppointment(id)
    }
}
