package com.petcare.app.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.petcare.app.data.api.TokenStore
import com.petcare.app.data.model.MeUpdateRequest
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit
) {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context.applicationContext) }
    val vm = remember { ProfileViewModel() }
    val state by vm.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { vm.loadMe() }

    // form states
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var timezone by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // remplir une seule fois quand me arrive
    LaunchedEffect(state.me) {
        val me = state.me ?: return@LaunchedEffect
        name = me.name.orEmpty()
        email = me.email.orEmpty()
        phone = me.phone.orEmpty()
        timezone = me.timezone.orEmpty()
        language = me.language.orEmpty()
        password = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Profil", style = MaterialTheme.typography.titleLarge)

        if (state.loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        state.error?.let {
            AssistChip(onClick = { vm.clearMessages() }, label = { Text(it) })
        }
        state.success?.let {
            AssistChip(onClick = { vm.clearMessages() }, label = { Text(it) })
        }

        Card(Modifier.fillMaxWidth()) {
            Column(
                Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ✅ Aucun affichage ID ici
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Téléphone (optionnel)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                /*Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = timezone,
                        onValueChange = { timezone = it },
                        label = { Text("Timezone") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = language,
                        onValueChange = { language = it },
                        label = { Text("Langue") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }*/

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Nouveau mot de passe (optionnel)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    enabled = !state.saving,
                    onClick = {
                        val body = MeUpdateRequest(
                            name = name.trim().takeIf { it.isNotEmpty() },
                            email = email.trim().takeIf { it.isNotEmpty() },
                            phone = phone.trim().takeIf { it.isNotEmpty() },
                            timezone = timezone.trim().takeIf { it.isNotEmpty() },
                            language = language.trim().takeIf { it.isNotEmpty() },
                            password = password.takeIf { it.length >= 6 }
                        )
                        vm.saveMe(body)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.saving) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Text("Enregistrement…")
                        }
                    } else {
                        Text("Enregistrer")
                    }
                }
            }
        }

        Divider()

        Button(
            onClick = {
                scope.launch {
                    tokenStore.saveToken("") // clear token
                    onLoggedOut()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Se déconnecter")
        }
    }
}
