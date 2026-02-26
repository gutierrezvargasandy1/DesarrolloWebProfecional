package mx.edu.utng.reposertedh.ui.recovery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun RecoveryScreen(
    viewModel: RecoveryViewModel,
    onPasswordChanged: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Navegar al login cuando el password fue cambiado exitosamente
    LaunchedEffect(state) {
        if (state is RecoveryState.PasswordCambiado) onPasswordChanged()
    }

    when (state) {
        is RecoveryState.CodigoValido -> PantallaNewPassword(viewModel)
        is RecoveryState.CodigoEnviado -> PantallaValidarCodigo(viewModel)
        else -> PantallaEnviarCodigo(viewModel, onBackToLogin)
    }
}

// =====================
// PANTALLA 1: Ingresar correo
// =====================
@Composable
fun PantallaEnviarCodigo(
    viewModel: RecoveryViewModel,
    onBackToLogin: () -> Unit
) {
    var correo by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Recuperar contraseña", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(12.dp))

        Text(
            "Ingresa tu correo y te enviaremos un código de verificación.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(24.dp))

        if (state is RecoveryState.Error) {
            Text(
                text = (state as RecoveryState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Button(
            onClick = { viewModel.enviarCodigo(correo) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is RecoveryState.Loading
        ) {
            if (state is RecoveryState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Enviar código")
            }
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onBackToLogin) {
            Text("Volver al login")
        }
    }
}

// =====================
// PANTALLA 2: Ingresar código
// =====================
@Composable
fun PantallaValidarCodigo(viewModel: RecoveryViewModel) {
    var codigo by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Verificar código", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(12.dp))

        Text(
            "Ingresa el código que enviamos a ${viewModel.correoGuardado}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = codigo,
            onValueChange = { codigo = it },
            label = { Text("Código de verificación") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(24.dp))

        if (state is RecoveryState.Error) {
            Text(
                text = (state as RecoveryState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Button(
            onClick = { viewModel.validarCodigo(codigo) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is RecoveryState.Loading
        ) {
            if (state is RecoveryState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Verificar código")
            }
        }
    }
}

// =====================
// PANTALLA 3: Nueva contraseña
// =====================
@Composable
fun PantallaNewPassword(viewModel: RecoveryViewModel) {
    var nuevaPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorLocal by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Nueva contraseña", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(12.dp))

        Text(
            "Ingresa tu nueva contraseña.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = nuevaPassword,
            onValueChange = { nuevaPassword = it },
            label = { Text("Nueva contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(24.dp))

        if (errorLocal.isNotEmpty()) {
            Text(
                text = errorLocal,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (state is RecoveryState.Error) {
            Text(
                text = (state as RecoveryState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Button(
            onClick = {
                if (nuevaPassword != confirmPassword) {
                    errorLocal = "Las contraseñas no coinciden"
                } else if (nuevaPassword.length < 6) {
                    errorLocal = "Mínimo 6 caracteres"
                } else {
                    errorLocal = ""
                    viewModel.cambiarPassword(nuevaPassword)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is RecoveryState.Loading
        ) {
            if (state is RecoveryState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Cambiar contraseña")
            }
        }
    }
}