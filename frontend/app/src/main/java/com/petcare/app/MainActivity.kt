package com.petcare.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.petcare.app.data.api.TokenStore
import com.petcare.app.ui.auth.LoginScreen
import com.petcare.app.ui.nav.AppScaffold
import com.petcare.app.ui.nav.Routes
import com.petcare.app.ui.theme.PetCareTheme
import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Init ApiClient une seule fois
        com.petcare.app.data.api.ApiClient.init(this)

        setContent {
            PetCareTheme {
                AppRoot(appContext = applicationContext)
            }
        }
    }
    private val requestNotifPerm = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* ok/ko */ }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun AppRoot(appContext: Context) {
    val nav = rememberNavController()
    val tokenStore = remember { TokenStore(appContext) }

    val token by tokenStore.tokenFlow.collectAsState(initial = null)

    LaunchedEffect(token) {
        if (token.isNullOrBlank()) {
            nav.navigate(Routes.Login) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        } else {
            nav.navigate(Routes.App) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = nav,
        startDestination = Routes.Login
    ) {
        composable(Routes.Login) {
            LoginScreen(
                onAuthSuccess = {
                    // rien ici, redirection auto via token
                }
            )
        }

        composable(Routes.App) {
            AppScaffold(onLogout = {})
        }
    }
}

