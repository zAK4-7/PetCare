package com.petcare.app.data.repo

import com.petcare.app.data.api.ApiClient
import com.petcare.app.data.model.MeDto
import com.petcare.app.data.model.MeUpdateRequest

class ProfileRepository {
    suspend fun fetchMe(): MeDto = ApiClient.api.getMe()
    suspend fun updateMe(body: MeUpdateRequest): MeDto = ApiClient.api.updateMe(body)
}
