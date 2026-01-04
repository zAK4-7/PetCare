package com.petcare.app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petcare.app.R
import com.petcare.app.data.api.TokenStore
import com.petcare.app.data.repo.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context.applicationContext) }
    val repo = remember { AuthRepository() }
    val scope = rememberCoroutineScope()

    // mode login / signup
    var isSignup by remember { mutableStateOf(false) }

    // champs communs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // signup only
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // UI states
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.petcare_logo),
            contentDescription = "PetCare Logo",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = if (isSignup) "Créer un compte" else "Connexion",
            fontSize = 22.sp
        )

        Spacer(Modifier.height(20.dp))

        if (isSignup) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; error = null },
                label = { Text("Nom") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; error = null },
                label = { Text("Téléphone (optionnel)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; error = null },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = { Text("Mot de passe") },
            singleLine = true,
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible)
                            Icons.Filled.VisibilityOff
                        else
                            Icons.Filled.Visibility,
                        contentDescription = "Afficher / masquer le mot de passe"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                error = null

                if (!email.contains("@")) {
                    error = "Email invalide"
                    return@Button
                }
                if (password.length < 4) {
                    error = "Mot de passe trop court"
                    return@Button
                }
                if (isSignup && name.isBlank()) {
                    error = "Le nom est obligatoire"
                    return@Button
                }

                loading = true

                scope.launch {
                    runCatching {
                        if (isSignup) {
                            repo.register(
                                name = name.trim(),
                                email = email.trim(),
                                password = password,
                                phone = phone.trim().takeIf { it.isNotBlank() }
                            )
                        } else {
                            repo.login(email.trim(), password)
                        }
                    }.onSuccess { token ->
                        tokenStore.saveToken(token)
                        loading = false
                        onAuthSuccess()
                    }.onFailure {
                        loading = false
                        error = if (isSignup)
                            "Impossible de créer le compte"
                        else
                            "Email ou mot de passe incorrect"
                    }
                }
            }
        ) {
            if (loading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Veuillez patienter...")
                }
            } else {
                Text(if (isSignup) "Créer le compte" else "Se connecter")
            }
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = {
            isSignup = !isSignup
            error = null
        }) {
            Text(
                if (isSignup)
                    "Déjà un compte ? Se connecter"
                else
                    "Pas de compte ? Créer un compte"
            )
        }
    }
}
