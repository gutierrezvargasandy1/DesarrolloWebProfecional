package mx.edu.utng.petfinder.Navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.edu.utng.petfinder.data.local.TokenManager
import mx.edu.utng.petfinder.data.remote.AuthModule.Service.AuthApiService
import mx.edu.utng.petfinder.data.remote.ConfigClient.RetrofitClient
import mx.edu.utng.petfinder.data.remote.MascotasModule.Service.MascotaApiService
import mx.edu.utng.petfinder.data.remote.ReportesModule.Service.ReporteApiService
import mx.edu.utng.petfinder.ui.AuthModule.PantallaLogin.LoginScreen
import mx.edu.utng.petfinder.ui.AuthModule.PantallaLogin.LoginViewModel
import mx.edu.utng.petfinder.ui.AuthModule.PantallaLogin.LoginViewModelFactory
import mx.edu.utng.petfinder.ui.Home.HomeScreen
import mx.edu.utng.petfinder.ui.Home.HomeViewModel
import mx.edu.utng.petfinder.ui.Home.HomeViewModelFactory
import mx.edu.utng.petfinder.ui.MascotaModule.PantallaAgregarMascota.AgregarMascotaScreen
import mx.edu.utng.petfinder.ui.MascotaModule.PantallaAgregarMascota.AgregarMascotaViewModel
import mx.edu.utng.petfinder.ui.MascotaModule.PantallaAgregarMascota.AgregarMascotaViewModelFactory
import mx.edu.utng.petfinder.ui.MascotaModule.PantallaAgregarMascota.LocationRepository
import mx.edu.utng.petfinder.ui.MascotaModule.PantallaMisMascotas.MascotasViewModel
import mx.edu.utng.petfinder.ui.MascotaModule.PantallaMisMascotas.MascotasViewModelFactory
import mx.edu.utng.petfinder.ui.MascotaModule.PantallaMisMascotas.MisMascotasScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    val context = LocalContext.current

    val tokenManager = remember {
        TokenManager(context)
    }

    val retrofit = remember {
        RetrofitClient.create(tokenManager)
    }

    val authApi = remember {
        retrofit.create(AuthApiService::class.java)
    }

    val reporteApi = remember {
        retrofit.create(ReporteApiService::class.java)
    }

    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(authApi, tokenManager)
    )

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(reporteApi)
    )

    val mascotaApi = remember {
        retrofit.create(MascotaApiService::class.java)
    }

    val mascotasViewModel: MascotasViewModel = viewModel(
        factory = MascotasViewModelFactory(mascotaApi)
    )

    // Repositorio de ubicación para agregar mascota
    val locationRepository = remember {
        LocationRepository(context)
    }

    val agregarMascotaViewModel: AgregarMascotaViewModel = viewModel(
        factory = AgregarMascotaViewModelFactory(mascotaApi, locationRepository)
    )

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(
                navController = navController,
                viewModel = loginViewModel
            )
        }

        composable("home") {
            HomeScreen(
                navController = navController,
                viewModel = homeViewModel
            )
        }

        composable("reporte_detalle/{reporteId}") {
            val reporteId = it.arguments?.getString("reporteId")?.toIntOrNull() ?: 0
            // Aquí irá la pantalla de detalle
        }

        composable("perfil") {
            // Pantalla de perfil
        }

        composable("mis_mascotas") {
            MisMascotasScreen(
                navController = navController,
                viewModel = mascotasViewModel,
                onAgregarMascota = {
                    navController.navigate("agregar_mascota")
                },
                onEditarMascota = { mascotaId ->
                    navController.navigate("editar_mascota/$mascotaId")
                },
                onCrearReporte = { mascotaId, estado ->
                    navController.navigate("crear_reporte/$mascotaId?estado=$estado")
                }
            )
        }

        composable("mis_reportes") {
            // Pantalla de mis reportes
        }

        composable("conversaciones") {
            // Pantalla de conversaciones
        }

        composable("agregar_mascota") {
            AgregarMascotaScreen(
                navController = navController,
                viewModel = agregarMascotaViewModel
            )
        }

        composable("editar_mascota/{mascotaId}") {
            val mascotaId = it.arguments?.getString("mascotaId")?.toIntOrNull() ?: 0
            // Pantalla para editar mascota
        }

        composable("crear_reporte/{mascotaId}") {
            val mascotaId = it.arguments?.getString("mascotaId")?.toIntOrNull() ?: 0
            val estado = it.arguments?.getString("estado") ?: "PERDIDA"
            // Pantalla para crear/actualizar reporte
        }

        composable("mascota_detalle/{mascotaId}") {
            val mascotaId = it.arguments?.getString("mascotaId")?.toIntOrNull() ?: 0
            // Pantalla de detalle de mascota
        }
    }
}