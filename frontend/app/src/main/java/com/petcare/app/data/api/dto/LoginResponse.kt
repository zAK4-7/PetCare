package com.petcare.app.data.api.dto

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    // Format 1: { "token": "..." }
    val token: String? = null,

    // Format 2: { "accessToken": "..." }
    @SerializedName("accessToken") val accessToken: String? = null,

    // Format 3: { "data": { "token": "..." } }
    val data: TokenData? = null
) {
    data class TokenData(
        val token: String? = null,
        @SerializedName("accessToken") val accessToken: String? = null
    )

    fun pickToken(): String? =
        token?.takeIf { it.isNotBlank() }
            ?: accessToken?.takeIf { it.isNotBlank() }
            ?: data?.token?.takeIf { it.isNotBlank() }
            ?: data?.accessToken?.takeIf { it.isNotBlank() }
}
