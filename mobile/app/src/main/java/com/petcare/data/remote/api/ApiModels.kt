package com.petcare.data.remote.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthRegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val timezone: String? = null,
    val language: String? = null
)

@JsonClass(generateAdapter = true)
data class AuthLoginRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    val token: String,
    val user: UserDto
)

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String? = null,
    val timezone: String? = null,
    val language: String? = null,
    val role: String
)

@JsonClass(generateAdapter = true)
data class PetDto(
    val id: Int,
    val userId: Int,
    val name: String,
    val species: String,
    val breed: String? = null,
    val sex: String? = null,
    val birthDate: String? = null,
    val photoUrl: String? = null,
    val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class CreatePetRequest(
    val name: String,
    val species: String,
    val breed: String? = null,
    val sex: String? = null,
    val birthDate: String? = null,
    val photoUrl: String? = null,
    val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class HealthEventDto(
    val id: Int,
    val petId: Int,
    val type: String,
    val title: String,
    val description: String? = null,
    val eventDate: String
)

@JsonClass(generateAdapter = true)
data class CreateHealthEventRequest(
    val type: String,
    val title: String,
    val description: String? = null,
    val eventDate: String
)

@JsonClass(generateAdapter = true)
data class ReminderDto(
    val id: Int,
    val healthEventId: Int,
    val remindAt: String,
    val sent: Boolean
)

@JsonClass(generateAdapter = true)
data class CreateReminderRequest(
    val remindAt: String
)
