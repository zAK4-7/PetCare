package com.petcare.app.data.api

import com.petcare.app.data.model.*
import retrofit2.http.*

interface ApiService {

    // ===== Auth =====
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): LoginResponse

    // ===== Agenda =====
    @GET("agenda")
    suspend fun getAppointments(): List<AppointmentDto>

    @POST("agenda")
    suspend fun createAppointment(@Body body: AppointmentCreateRequest): AppointmentDto

    @PATCH("agenda/{id}")
    suspend fun updateAppointment(
        @Path("id") id: Int,
        @Body body: AppointmentUpdateRequest
    ): AppointmentDto

    @DELETE("agenda/{id}")
    suspend fun deleteAppointment(@Path("id") id: Int)

    // ===== Services =====
    @GET("services")
    suspend fun getServices(@Query("type") type: String? = null): List<ServiceDto>

    // ===== Me =====
    @GET("me")
    suspend fun getMe(): MeDto

    @PATCH("me")
    suspend fun updateMe(@Body body: MeUpdateRequest): MeDto

    // ===== Pets =====
    @GET("pets")
    suspend fun getPets(): List<PetDto>

    @POST("pets")
    suspend fun createPet(@Body body: PetCreateRequest): PetDto

    @PUT("pets/{id}")
    suspend fun updatePet(
        @Path("id") id: Int,
        @Body body: PetUpdateRequest
    ): PetDto

    @DELETE("pets/{id}")
    suspend fun deletePet(@Path("id") id: Int)
}
