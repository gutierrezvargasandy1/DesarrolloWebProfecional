package mx.edu.utng.reposertedh.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.edu.utng.reposertedh.data.TokenManager
import mx.edu.utng.reposertedh.network.AuthApiService
import mx.edu.utng.reposertedh.network.ReporteApiService
import mx.edu.utng.reposertedh.network.RetrofitClient
import mx.edu.utng.reposertedh.ui.dashboard.DashboardScreen
import mx.edu.utng.reposertedh.ui.dashboard.DashboardViewModel
import mx.edu.utng.reposertedh.ui.dashboard.DashboardViewModelFactory
import mx.edu.utng.reposertedh.ui.login.LoginScreen
import mx.edu.utng.reposertedh.ui.login.LoginViewModel
import mx.edu.utng.reposertedh.ui.login.LoginViewModelFactory
import mx.edu.utng.reposertedh.ui.recovery.RecoveryScreen
import mx.edu.utng.reposertedh.ui.recovery.RecoveryViewModel
import mx.edu.utng.reposertedh.ui.recovery.RecoveryViewModelFactory
import mx.edu.utng.reposertedh.ui.register.RegisterScreen
import mx.edu.utng.reposertedh.ui.register.RegisterViewModel
import mx.edu.utng.reposertedh.ui.register.RegisterViewModelFactory
import mx.edu.utng.reposertedh.ui.reporte.DetalleReporteScreen
import mx.edu.utng.reposertedh.ui.reporte.DetalleViewModelFactory
import mx.edu.utng.reposertedh.ui.reporte.EditarReporteScreen
import mx.edu.utng.reposertedh.ui.reporte.EditarReporteViewModel
import mx.edu.utng.reposertedh.ui.reporte.EditarViewModelFactory
import mx.edu.utng.reposertedh.ui.reporte.MisReportesScreen
import mx.edu.utng.reposertedh.ui.reporte.MisReportesViewModelFactory
import mx.edu.utng.reposertedh.ui.reporte.NuevoReporteScreen
import mx.edu.utng.reposertedh.ui.reporte.NuevoReporteViewModel
import mx.edu.utng.reposertedh.ui.reporte.NuevoReporteViewModelFactory

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val tokenManager = TokenManager(context)
    val authApi = RetrofitClient.create(tokenManager).create(AuthApiService::class.java)
    val reporteApi = RetrofitClient.create(tokenManager).create(ReporteApiService::class.java)

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            val viewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(authApi, tokenManager))
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToRecovery = { navController.navigate("recovery") }
            )
        }

        composable("register") {
            val viewModel: RegisterViewModel = viewModel(factory = RegisterViewModelFactory(authApi))
            RegisterScreen(
                viewModel = viewModel,
                onRegisterSuccess = { navController.navigate("login") { popUpTo("register") { inclusive = true } } },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable("recovery") {
            val viewModel: RecoveryViewModel = viewModel(factory = RecoveryViewModelFactory(authApi))
            RecoveryScreen(
                viewModel = viewModel,
                onPasswordChanged = { navController.navigate("login") { popUpTo("recovery") { inclusive = true } } },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable("dashboard") {
            val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModelFactory(reporteApi))
            DashboardScreen(
                viewModel = viewModel,
                onReporteClick = { id -> navController.navigate("detalle/$id") },
                onNuevoReporte = { navController.navigate("nuevo_reporte") },
                onMisReportes = { navController.navigate("mis_reportes") },
                onLogout = {
                    // ── LOGOUT LOGIC ────────────────────────────────────────
                    navController.navigate("login") {
                        // Esto borra TODO el historial de navegación
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("detalle/{id}") { backStack ->
            val id = backStack.arguments?.getString("id")?.toInt() ?: return@composable
            val viewModel = viewModel<mx.edu.utng.reposertedh.ui.reporte.DetalleViewModel>(
                factory = DetalleViewModelFactory(reporteApi)
            )
            DetalleReporteScreen(
                viewModel = viewModel,
                reporteId = id,
                onBack = { navController.popBackStack() }
            )
        }

        composable("nuevo_reporte") {
            // Usamos la Factory que acabas de mostrarme
            val viewModel: NuevoReporteViewModel = viewModel(
                factory = NuevoReporteViewModelFactory(reporteApi, tokenManager)
            )

            NuevoReporteScreen(
                viewModel = viewModel,
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable("mis_reportes") {
            val viewModel = viewModel<mx.edu.utng.reposertedh.ui.reporte.MisReportesViewModel>(
                factory = MisReportesViewModelFactory(reporteApi, tokenManager)
            )
            MisReportesScreen(
                viewModel = viewModel,
                onEditarReporte = { id -> navController.navigate("editar/$id") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("editar/{id}") { backStack ->
            val id = backStack.arguments?.getString("id")?.toInt() ?: return@composable
            val viewModel: EditarReporteViewModel = viewModel(
                factory = EditarViewModelFactory(reporteApi)
            )

            EditarReporteScreen(
                viewModel = viewModel,
                reporteId = id,
                onSuccess = {
                    navController.popBackStack() // Regresa a "Mis Reportes"
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}