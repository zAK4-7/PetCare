package com.petcare.app.data.repo

import retrofit2.HttpException
import com.petcare.app.data.api.ApiClient
import com.petcare.app.data.model.LoginRequest
import com.petcare.app.data.model.RegisterRequest

class AuthRepository {

    private val api = ApiClient.api

    suspend fun login(email: String, password: String): String {
        return runCatching {
            val res = api.login(LoginRequest(email = email, password = password))
            val token = res.bearer
            if (token.isBlank()) throw Exception("Réponse invalide (token manquant)")
            token
        }.getOrElse { e ->
            throw Exception(userFriendlyAuthError(e))
        }
    }

    suspend fun register(name: String, email: String, password: String, phone: String?): String {
        return runCatching {
            val res = api.register(
                RegisterRequest(
                    name = name,
                    email = email,
                    password = password,
                    phone = phone
                )
            )

            // ✅ si backend renvoie token -> parfait
            val token = res.bearer
            if (token.isNotBlank()) return@runCatching token

            // ✅ sinon: le compte est créé mais pas de token => login auto
            val loginRes = api.login(LoginRequest(email = email, password = password))
            val loginToken = loginRes.bearer
            if (loginToken.isBlank()) throw Exception("Compte créé, mais connexion automatique impossible")
            loginToken
        }.getOrElse { e ->
            throw Exception(userFriendlyAuthError(e))
        }
    }

    private fun userFriendlyAuthError(e: Throwable): String {
        // Messages clairs sans 401/409
        if (e is HttpException) {
            return when (e.code()) {
                400 -> "Données invalides. Vérifie les champs."
                401 -> "Email ou mot de passe incorrect."
                409 -> "Cet email est déjà utilisé."
                else -> "Impossible de contacter le serveur. Réessaie."
            }
        }
        val msg = e.message.orEmpty().lowercase()
        return when {
            msg.contains("failed to connect") || msg.contains("timeout") -> "Impossible de se connecter au serveur."
            msg.contains("token") -> "Réponse serveur invalide."
            else -> "Une erreur est survenue. Réessaie."
        }
    }
}
