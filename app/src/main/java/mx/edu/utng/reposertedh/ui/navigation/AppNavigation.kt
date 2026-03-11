package mx.edu.utng.reposertedh.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.edu.utng.reposertedh.data.TokenManager
import mx.edu.utng.reposertedh.network.AuthApiService
import mx.edu.utng.reposertedh.network.MascotaApiService
import mx.edu.utng.reposertedh.network.ReporteApiService
import mx.edu.utng.reposertedh.network.RetrofitClient
import mx.edu.utng.reposertedh.ui.dashboard.DashboardScreen
import mx.edu.utng.reposertedh.ui.dashboard.DashboardViewModel
import mx.edu.utng.reposertedh.ui.dashboard.DashboardViewModelFactory
import mx.edu.utng.reposertedh.ui.login.LoginScreen
import mx.edu.utng.reposertedh.ui.login.LoginViewModel
import mx.edu.utng.reposertedh.ui.login.LoginViewModelFactory
import mx.edu.utng.reposertedh.ui.misMascotas.MisMascotasScreen
import mx.edu.utng.reposertedh.ui.misMascotas.MisMascotasViewModel
import mx.edu.utng.reposertedh.ui.misMascotas.MisMascotasViewModelFactory
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
    val context       = LocalContext.current
    val tokenManager  = TokenManager(context)
    val authApi       = RetrofitClient.create(tokenManager).create(AuthApiService::class.java)
    val reporteApi    = RetrofitClient.create(tokenManager).create(ReporteApiService::class.java)
    val mascotaApi    = RetrofitClient.create(tokenManager).create(MascotaApiService::class.java)

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            val viewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(authApi, tokenManager))
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { navController.navigate("dashboard") { popUpTo("login") { inclusive = true } } },
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
                onNuevoReporte = { navController.navigate("nuevo_reporte/0") },
                onMisReportes  = { navController.navigate("mis_reportes") },
                onChats        = { navController.navigate("chats") },
                onMisMascotas  = { navController.navigate("mis_mascotas") },
                onLogout       = { navController.navigate("login") { popUpTo(0) { inclusive = true }; launchSingleTop = true } }
            )
        }

        composable("detalle/{id}") { back ->
            val id = back.arguments?.getString("id")?.toInt() ?: return@composable
            val viewModel = viewModel<mx.edu.utng.reposertedh.ui.reporte.DetalleViewModel>(factory = DetalleViewModelFactory(reporteApi))
            DetalleReporteScreen(viewModel = viewModel, reporteId = id, onBack = { navController.popBackStack() })
        }

        // ── Nuevo reporte — idMascota = 0 significa "sin mascota vinculada" ──
        composable("nuevo_reporte/{idMascota}") { back ->
            val idMascota = back.arguments?.getString("idMascota")?.toIntOrNull()
                ?.takeIf { it > 0 }   // 0 → null (reporte genérico)
            val viewModel: NuevoReporteViewModel = viewModel(factory = NuevoReporteViewModelFactory(reporteApi, tokenManager))
            NuevoReporteScreen(
                viewModel  = viewModel,
                idMascota  = idMascota,
                onSuccess  = { navController.popBackStack() },
                onBack     = { navController.popBackStack() }
            )
        }

        composable("mis_reportes") {
            val viewModel = viewModel<mx.edu.utng.reposertedh.ui.reporte.MisReportesViewModel>(
                factory = MisReportesViewModelFactory(reporteApi, tokenManager))
            MisReportesScreen(
                viewModel = viewModel,
                onEditarReporte = { id -> navController.navigate("editar/$id") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("editar/{id}") { back ->
            val id = back.arguments?.getString("id")?.toInt() ?: return@composable
            val viewModel: EditarReporteViewModel = viewModel(factory = EditarViewModelFactory(reporteApi))
            EditarReporteScreen(viewModel = viewModel, reporteId = id,
                onSuccess = { navController.popBackStack() }, onBack = { navController.popBackStack() })
        }

        composable("chats") {
            androidx.compose.material3.Surface(
                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                color = androidx.compose.ui.graphics.Color(0xFFFFF8F0)
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.foundation.layout.Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                    ) {
                        androidx.compose.material3.Text("💬", fontSize = 52.sp)
                        androidx.compose.material3.Text("Chats", fontSize = 22.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                            color = androidx.compose.ui.graphics.Color(0xFF6B3F1F))
                        androidx.compose.material3.Text("Próximamente...",
                            color = androidx.compose.ui.graphics.Color(0xFF8B6347))
                        androidx.compose.material3.TextButton(onClick = { navController.popBackStack() }) {
                            androidx.compose.material3.Text("← Volver",
                                color = androidx.compose.ui.graphics.Color(0xFFFF6B35))
                        }
                    }
                }
            }
        }

        composable("mis_mascotas") {
            val viewModel: MisMascotasViewModel = viewModel(factory = MisMascotasViewModelFactory(tokenManager, mascotaApi))
            MisMascotasScreen(
                viewModel = viewModel,
                onBack    = { navController.popBackStack() },
                // Cuando la mascota se marca como PERDIDA → crear reporte vinculado
                onNuevoReporte = { idMascota -> navController.navigate("nuevo_reporte/$idMascota") }
            )
        }
    }
}