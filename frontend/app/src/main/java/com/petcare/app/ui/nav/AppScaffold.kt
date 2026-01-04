package com.petcare.app.ui.nav

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.petcare.app.R
import com.petcare.app.ui.agenda.*
import com.petcare.app.ui.home.HomeScreen
import com.petcare.app.ui.pets.AddPetScreen
import com.petcare.app.ui.pets.PetsScreen
import com.petcare.app.ui.pets.PetsViewModel
import com.petcare.app.ui.profile.ProfileScreen
import com.petcare.app.ui.services.ServicesScreen
import com.petcare.app.ui.services.ServicesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    onLogout: () -> Unit
) {
    val nav = rememberNavController()

    val petsVm = remember { PetsViewModel() }
    val agendaVm = remember { AgendaViewModel() }
    val servicesVm = remember { ServicesViewModel() }

    val currentRoute = nav.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(

        /* =======================
           🔝 TOP BAR AVEC LOGO
           ======================= */
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.petcare_logo),
                        contentDescription = "PetCare Logo",
                        modifier = Modifier
                            .height(36.dp)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },

        /* =======================
           ➕ FAB
           ======================= */
        floatingActionButton = {
            when (currentRoute) {
                Routes.Pets ->
                    FloatingActionButton(onClick = { nav.navigate(Routes.AddPet) }) {
                        Text("+")
                    }

                Routes.Agenda ->
                    FloatingActionButton(onClick = { nav.navigate(Routes.AddAppointment) }) {
                        Text("+")
                    }
            }
        },

        /* =======================
           🔽 BOTTOM NAV
           ======================= */
        bottomBar = {
            NavigationBar {
                fun go(route: String) {
                    nav.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(Routes.Home) { saveState = true }
                    }
                }

                NavigationBarItem(
                    selected = currentRoute == Routes.Home,
                    onClick = { go(Routes.Home) },
                    icon = { Text("🏠") },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = currentRoute == Routes.Agenda,
                    onClick = { go(Routes.Agenda) },
                    icon = { Text("📅") },
                    label = { Text("Agenda") }
                )

                NavigationBarItem(
                    selected = currentRoute == Routes.Pets || currentRoute == Routes.AddPet || currentRoute?.startsWith("pets/edit") == true,
                    onClick = { go(Routes.Pets) },
                    icon = { Text("🐾") },
                    label = { Text("Pets") }
                )

                NavigationBarItem(
                    selected = currentRoute == Routes.Services,
                    onClick = { go(Routes.Services) },
                    icon = { Text("🧰") },
                    label = { Text("Services") }
                )

                NavigationBarItem(
                    selected = currentRoute == Routes.Profile,
                    onClick = { go(Routes.Profile) },
                    icon = { Text("👤") },
                    label = { Text("Profil") }
                )
            }
        }

    ) { padding ->

        /* =======================
           🧭 NAV HOST
           ======================= */
        NavHost(
            navController = nav,
            startDestination = Routes.Home,
            modifier = Modifier
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {

            composable(Routes.Home) {
                val petsState by petsVm.uiState.collectAsState()
                val agendaState by agendaVm.uiState.collectAsState()

                LaunchedEffect(Unit) {
                    petsVm.loadPets()
                    agendaVm.loadAgenda()
                }

                HomeScreen(
                    petsCount = petsState.pets.size,
                    appointments = agendaState.items,
                    loadingPets = petsState.loading,
                    loadingAgenda = agendaState.loading,
                    errorPets = petsState.error,
                    errorAgenda = agendaState.error,
                    onRefresh = {
                        petsVm.loadPets()
                        agendaVm.loadAgenda()
                    },
                    onGoAgenda = { nav.navigate(Routes.Agenda) },
                    onAddAppointment = { nav.navigate(Routes.AddAppointment) },
                    onGoPets = { nav.navigate(Routes.Pets) },
                    onAddPet = { nav.navigate(Routes.AddPet) },
                    onGoServices = { nav.navigate(Routes.Services) },
                    onGoProfile = { nav.navigate(Routes.Profile) }
                )
            }

            // AGENDA
            composable(Routes.Agenda) {
                AgendaScreen(
                    vm = agendaVm,
                    onAdd = { nav.navigate(Routes.AddAppointment) },
                    onEdit = { id -> nav.navigate(Routes.editAppointment(id)) }
                )
            }

            composable(Routes.AddAppointment) {
                AddAppointmentScreen(
                    vm = agendaVm,
                    appointmentId = null,
                    onBack = { nav.popBackStack() }
                )
            }

            composable(
                route = Routes.EditAppointment,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) {
                val id = it.arguments?.getInt("id") ?: return@composable
                AddAppointmentScreen(
                    vm = agendaVm,
                    appointmentId = id,
                    onBack = { nav.popBackStack() }
                )
            }

            // PETS
            composable(Routes.Pets) {
                PetsScreen(
                    vm = petsVm,
                    onAdd = { nav.navigate(Routes.AddPet) },
                    onEdit = { id -> nav.navigate("pets/edit/$id") }
                )
            }

            composable(Routes.AddPet) {
                AddPetScreen(
                    vm = petsVm,
                    petId = null,
                    onBack = { nav.popBackStack() }
                )
            }

            composable(
                route = "pets/edit/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) {
                val id = it.arguments?.getInt("id") ?: return@composable
                AddPetScreen(
                    vm = petsVm,
                    petId = id,
                    onBack = { nav.popBackStack() }
                )
            }

            // SERVICES
            composable(Routes.Services) {
                ServicesScreen(vm = servicesVm, onBook = {})
            }

            // PROFILE
            composable(Routes.Profile) {
                ProfileScreen(onLoggedOut = onLogout)
            }
        }
    }
}
