package com.petcare.data.remote.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATH
import retrofit2.http.POST

interface PetCareApi {

    // Auth
    @POST("auth/register")
    suspend fun register(@Body body: AuthRegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: AuthLoginRequest): AuthResponse

    // Me
    @GET("me")
    suspend fun me(): UserDto

    // Pets
    @GET("pets")
    suspend fun listPets(): List<PetDto>

    @POST("pets")
    suspend fun createPet(@Body body: CreatePetRequest): PetDto

    @DELETE("pets/{id}")
    suspend fun deletePet(@PATH("id") id: Int)

    // Health events
    @GET("pets/{petId}/health-events")
    suspend fun listHealthEvents(@PATH("petId") petId: Int): List<HealthEventDto>

    @POST("pets/{petId}/health-events")
    suspend fun createHealthEvent(
        @PATH("petId") petId: Int,
        @Body body: CreateHealthEventRequest
    ): HealthEventDto

    @DELETE("health-events/{id}")
    suspend fun deleteHealthEvent(@PATH("id") id: Int)

    // Reminders
    @GET("health-events/{eventId}/reminders")
    suspend fun listReminders(@PATH("eventId") eventId: Int): List<ReminderDto>

    @POST("health-events/{eventId}/reminders")
    suspend fun createReminder(
        @PATH("eventId") eventId: Int,
        @Body body: CreateReminderRequest
    ): ReminderDto

    @DELETE("reminders/{id}")
    suspend fun deleteReminder(@PATH("id") id: Int)
}
