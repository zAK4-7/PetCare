package com.petcare.app.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null
)

/**
 * Compatible avec backend qui renvoie "token" ou "accessToken"
 */
data class LoginResponse(
    val token: String? = null,
    val accessToken: String? = null
) {
    val bearer: String
        get() = token ?: accessToken ?: ""
}
