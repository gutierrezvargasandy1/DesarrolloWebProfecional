package mx.edu.utng.reposertedh.ui.recovery

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Paleta compartida ──────────────────────────────────────────────────────────
private val PetOrange      = Color(0xFFFF6B35)
private val PetOrangeLight = Color(0xFFFF8C5A)
private val PetYellow      = Color(0xFFFFD166)
private val PetCream       = Color(0xFFFFF8F0)
private val PetBrown       = Color(0xFF6B3F1F)
private val PetGreen       = Color(0xFF06D6A0)
private val SurfaceWhite   = Color(0xFFFFFFFF)
private val TextPrimary    = Color(0xFF2D1B0E)
private val TextSecondary  = Color(0xFF8B6347)

@Composable
fun RecoveryScreen(
    viewModel: RecoveryViewModel,
    onPasswordChanged: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is RecoveryState.PasswordCambiado) onPasswordChanged()
    }

    when (state) {
        is RecoveryState.CodigoValido  -> PantallaNewPassword(viewModel)
        is RecoveryState.CodigoEnviado -> PantallaValidarCodigo(viewModel)
        else                           -> PantallaEnviarCodigo(viewModel, onBackToLogin)
    }
}

// ── Fondo común ───────────────────────────────────────────────────────────────
@Composable
private fun PetBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFEDD5), Color(0xFFFFF3E0), PetCream)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = PetOrange.copy(alpha = 0.07f),
                radius = 260.dp.toPx(),
                center = Offset(size.width * 0.85f, size.height * 0.08f)
            )
            drawCircle(
                color = PetYellow.copy(alpha = 0.10f),
                radius = 180.dp.toPx(),
                center = Offset(size.width * 0.1f, size.height * 0.88f)
            )
        }
        content()
    }
}

// ── Hero icon animado ──────────────────────────────────────────────────────────
@Composable
private fun PetHeroIcon(emoji: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "float"
    )
    Box(
        modifier = Modifier
            .offset(y = offsetY.dp)
            .size(88.dp)
            .shadow(14.dp, CircleShape)
            .background(
                Brush.radialGradient(colors = listOf(PetOrangeLight, PetOrange)),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 40.sp)
    }
}

// ── Campo de texto reutilizable ────────────────────────────────────────────────
@Composable
private fun PetTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PetOrange,
            unfocusedBorderColor = Color(0xFFE0C8B0),
            focusedLabelColor = PetOrange,
            unfocusedLabelColor = TextSecondary,
            cursorColor = PetOrange,
            focusedContainerColor = Color(0xFFFFFAF5),
            unfocusedContainerColor = Color(0xFFFFFAF5)
        )
    )
}

// ── Bloque de error ────────────────────────────────────────────────────────────
@Composable
private fun ErrorBanner(message: String) {
    AnimatedVisibility(visible = message.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFEBEE))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("⚠️", fontSize = 15.sp)
            Text(message, color = Color(0xFFB71C1C), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ── Botón principal degradado ──────────────────────────────────────────────────
@Composable
private fun PetPrimaryButton(
    label: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        enabled = !isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = if (!isLoading)
                            listOf(PetOrange, PetOrangeLight)
                        else
                            listOf(PetOrange.copy(alpha = 0.5f), PetOrangeLight.copy(alpha = 0.5f))
                    ),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp,
                    color = SurfaceWhite
                )
            } else {
                Text(label, color = SurfaceWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

// =====================================================================
// PANTALLA 1: Ingresar correo
// =====================================================================
@Composable
fun PantallaEnviarCodigo(
    viewModel: RecoveryViewModel,
    onBackToLogin: () -> Unit
) {
    var correo by remember { mutableStateOf("") }
    val state  by viewModel.state.collectAsState()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    PetBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))
            PetHeroIcon("📧")
            Spacer(Modifier.height(20.dp))

            Text("PetFinder", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = PetBrown)
            Text(
                "Recupera tu acceso",
                fontSize = 13.sp, color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(28.dp))

            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(600)) + fadeIn()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("¿Olvidaste tu contraseña?", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Ingresa tu correo y te enviaremos un código para recuperar tu cuenta.",
                            fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 22.dp)
                        )

                        PetTextField(
                            value = correo,
                            onValueChange = { correo = it },
                            label = "Correo electrónico",
                            leadingIcon = { Icon(Icons.Default.Email, null, tint = PetOrange) },
                            keyboardType = KeyboardType.Email
                        )

                        if (state is RecoveryState.Error)
                            ErrorBanner((state as RecoveryState.Error).message)

                        Spacer(Modifier.height(22.dp))

                        PetPrimaryButton(
                            label = "📨  Enviar código",
                            isLoading = state is RecoveryState.Loading,
                            onClick = { viewModel.enviarCodigo(correo) }
                        )

                        Spacer(Modifier.height(8.dp))

                        TextButton(onClick = onBackToLogin) {
                            Text("← Volver al inicio de sesión", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(PetGreen.copy(alpha = 0.15f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("🔒", fontSize = 13.sp)
                Text("Tu cuenta está protegida", fontSize = 11.sp, color = Color(0xFF065F46), fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

// =====================================================================
// PANTALLA 2: Ingresar código
// =====================================================================
@Composable
fun PantallaValidarCodigo(viewModel: RecoveryViewModel) {
    var codigo  by remember { mutableStateOf("") }
    val state   by viewModel.state.collectAsState()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    PetBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))
            PetHeroIcon("🔑")
            Spacer(Modifier.height(20.dp))

            Text("PetFinder", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = PetBrown)
            Text("Verifica tu identidad", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))

            Spacer(Modifier.height(28.dp))

            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(600)) + fadeIn()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Código de verificación", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(Modifier.height(6.dp))

                        // Badge de correo
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = PetYellow.copy(alpha = 0.30f),
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            Text(
                                text = "📬  ${viewModel.correoGuardado}",
                                fontSize = 12.sp, color = PetBrown, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        PetTextField(
                            value = codigo,
                            onValueChange = { if (it.length <= 6) codigo = it },
                            label = "Código de 6 dígitos",
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = PetOrange) },
                            keyboardType = KeyboardType.Number
                        )

                        if (state is RecoveryState.Error)
                            ErrorBanner((state as RecoveryState.Error).message)

                        Spacer(Modifier.height(22.dp))

                        PetPrimaryButton(
                            label = "✅  Verificar código",
                            isLoading = state is RecoveryState.Loading,
                            onClick = { viewModel.validarCodigo(codigo) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

// =====================================================================
// PANTALLA 3: Nueva contraseña
// =====================================================================
@Composable
fun PantallaNewPassword(viewModel: RecoveryViewModel) {
    var nuevaPassword   by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showNew         by remember { mutableStateOf(false) }
    var showConfirm     by remember { mutableStateOf(false) }
    var errorLocal      by remember { mutableStateOf("") }
    val state           by viewModel.state.collectAsState()
    var visible         by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Indicador de fortaleza
    val strength = when {
        nuevaPassword.length >= 10 && nuevaPassword.any { it.isDigit() } && nuevaPassword.any { it.isUpperCase() } -> 3
        nuevaPassword.length >= 6 -> 2
        nuevaPassword.isNotEmpty() -> 1
        else -> 0
    }
    val strengthColor = listOf(Color.Transparent, Color(0xFFEF5350), PetYellow, PetGreen)[strength]
    val strengthLabel = listOf("", "Débil", "Media", "Fuerte")[strength]

    PetBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))
            PetHeroIcon("🔐")
            Spacer(Modifier.height(20.dp))

            Text("PetFinder", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = PetBrown)
            Text("Crea tu nueva contraseña", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))

            Spacer(Modifier.height(28.dp))

            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(600)) + fadeIn()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Nueva contraseña", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            "Elige una contraseña segura para proteger tu cuenta.",
                            fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 22.dp)
                        )

                        PetTextField(
                            value = nuevaPassword,
                            onValueChange = { nuevaPassword = it; errorLocal = "" },
                            label = "Nueva contraseña",
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = PetOrange) },
                            trailingIcon = {
                                IconButton(onClick = { showNew = !showNew }) {
                                    Icon(if (showNew) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = TextSecondary)
                                }
                            },
                            keyboardType = KeyboardType.Password,
                            visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation()
                        )

                        // Barra de fortaleza
                        if (nuevaPassword.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(3) { i ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(if (i < strength) strengthColor else Color(0xFFE0C8B0))
                                    )
                                }
                                Text(strengthLabel, fontSize = 11.sp, color = strengthColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 6.dp))
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        PetTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; errorLocal = "" },
                            label = "Confirmar contraseña",
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = PetOrange) },
                            trailingIcon = {
                                IconButton(onClick = { showConfirm = !showConfirm }) {
                                    Icon(if (showConfirm) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = TextSecondary)
                                }
                            },
                            keyboardType = KeyboardType.Password,
                            visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation()
                        )

                        if (errorLocal.isNotEmpty()) ErrorBanner(errorLocal)
                        if (state is RecoveryState.Error) ErrorBanner((state as RecoveryState.Error).message)

                        Spacer(Modifier.height(22.dp))

                        PetPrimaryButton(
                            label = "🔐  Guardar contraseña",
                            isLoading = state is RecoveryState.Loading,
                            onClick = {
                                when {
                                    nuevaPassword != confirmPassword -> errorLocal = "Las contraseñas no coinciden"
                                    nuevaPassword.length < 6        -> errorLocal = "Mínimo 6 caracteres"
                                    else -> { errorLocal = ""; viewModel.cambiarPassword(nuevaPassword) }
                                }
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}