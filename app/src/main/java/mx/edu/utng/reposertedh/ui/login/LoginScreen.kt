package mx.edu.utng.reposertedh.ui.login

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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

// ── Paleta de colores ──────────────────────────────────────────────────────────
private val PetOrange      = Color(0xFFFF6B35)
private val PetOrangeLight = Color(0xFFFF8C5A)
private val PetYellow      = Color(0xFFFFD166)
private val PetCream       = Color(0xFFFFF8F0)
private val PetBrown       = Color(0xFF6B3F1F)
private val PetBrownLight  = Color(0xFF9B6B47)
private val PetGreen       = Color(0xFF06D6A0)
private val SurfaceWhite   = Color(0xFFFFFFFF)
private val TextPrimary    = Color(0xFF2D1B0E)
private val TextSecondary  = Color(0xFF8B6347)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToRecovery: () -> Unit
) {
    var correo    by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var showPass  by remember { mutableStateOf(false) }
    val state     by viewModel.state.collectAsState()

    // Animación de entrada de la tarjeta
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    LaunchedEffect(state) {
        if (state is LoginState.Success) onLoginSuccess()
    }

    // Animación flotante de la pata
    val infiniteTransition = rememberInfiniteTransition(label = "paw_float")
    val pawOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "paw_float"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFEDD5), Color(0xFFFFF3E0), PetCream)
                )
            )
    ) {
        // Círculos decorativos de fondo
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = PetOrange.copy(alpha = 0.08f),
                radius = 280.dp.toPx(),
                center = Offset(size.width * 0.85f, size.height * 0.1f)
            )
            drawCircle(
                color = PetYellow.copy(alpha = 0.12f),
                radius = 200.dp.toPx(),
                center = Offset(size.width * 0.1f, size.height * 0.85f)
            )
            drawCircle(
                color = PetGreen.copy(alpha = 0.07f),
                radius = 140.dp.toPx(),
                center = Offset(size.width * 0.2f, size.height * 0.2f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            // ── Hero: ícono de pata animado ────────────────────────────────
            Box(
                modifier = Modifier
                    .offset(y = pawOffset.dp)
                    .size(100.dp)
                    .shadow(16.dp, CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(PetOrangeLight, PetOrange)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🐾", fontSize = 48.sp)
            }

            Spacer(Modifier.height(20.dp))

            // ── Título ─────────────────────────────────────────────────────
            Text(
                text = "PetFinder",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PetBrown,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Reencuentra a tu mejor amigo 🐶🐱",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Chips de mascotas decorativos
            Row(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("🐕 Perros", "🐈 Gatos", "🐰 Otros").forEach { label ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = PetYellow.copy(alpha = 0.35f),
                        modifier = Modifier.clip(RoundedCornerShape(50))
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            color = PetBrown,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Tarjeta principal ──────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(600, easing = EaseOutBack)
                ) + fadeIn(tween(600))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¡Bienvenido de vuelta!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Inicia sesión para continuar la búsqueda",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp, bottom = 24.dp)
                        )

                        // Campo correo
                        PetTextField(
                            value = correo,
                            onValueChange = { correo = it },
                            label = "Correo electrónico",
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    tint = PetOrange
                                )
                            },
                            keyboardType = KeyboardType.Email
                        )

                        Spacer(Modifier.height(14.dp))

                        // Campo contraseña
                        PetTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Contraseña",
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = PetOrange
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showPass = !showPass }) {
                                    Icon(
                                        if (showPass) Icons.Default.VisibilityOff
                                        else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = TextSecondary
                                    )
                                }
                            },
                            keyboardType = KeyboardType.Password,
                            visualTransformation = if (showPass) VisualTransformation.None
                            else PasswordVisualTransformation()
                        )

                        // Error message
                        AnimatedVisibility(visible = state is LoginState.Error) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFFEBEE))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("⚠️", fontSize = 16.sp)
                                Text(
                                    text = (state as? LoginState.Error)?.message ?: "",
                                    color = Color(0xFFB71C1C),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(Modifier.height(22.dp))

                        // Botón principal
                        Button(
                            onClick = { viewModel.login(correo, password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = state !is LoginState.Loading,
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
                                        if (state !is LoginState.Loading)
                                            Brush.horizontalGradient(
                                                colors = listOf(PetOrange, PetOrangeLight)
                                            )
                                        else
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    PetOrange.copy(alpha = 0.5f),
                                                    PetOrangeLight.copy(alpha = 0.5f)
                                                )
                                            ),
                                        RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (state is LoginState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.5.dp,
                                        color = SurfaceWhite
                                    )
                                } else {
                                    Text(
                                        text = "🔍  Buscar mascotas",
                                        color = SurfaceWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Botón registrarse
                        OutlinedButton(
                            onClick = onNavigateToRegister,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, PetOrange),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PetOrange
                            )
                        ) {
                            Text(
                                text = "🐾  Crear cuenta gratis",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        TextButton(onClick = onNavigateToRecovery) {
                            Text(
                                text = "¿Olvidaste tu contraseña?",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Footer de mascotas encontradas
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(PetGreen.copy(alpha = 0.15f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("✅", fontSize = 14.sp)
                Text(
                    text = "+12,400 mascotas reunidas con sus familias",
                    fontSize = 11.sp,
                    color = Color(0xFF065F46),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Componente reutilizable de campo de texto ──────────────────────────────────
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