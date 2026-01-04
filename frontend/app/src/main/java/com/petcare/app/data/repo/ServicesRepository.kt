package com.petcare.app.data.repo

import com.petcare.app.data.api.ApiClient
import com.petcare.app.data.model.ServiceDto

class ServicesRepository {
    suspend fun fetchServices(type: String? = null): List<ServiceDto> =
        ApiClient.api.getServices(type)
}

