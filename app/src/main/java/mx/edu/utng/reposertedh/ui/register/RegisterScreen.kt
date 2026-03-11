package mx.edu.utng.reposertedh.ui.register

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
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var nombre    by remember { mutableStateOf("") }
    var apellido  by remember { mutableStateOf("") }
    var correo    by remember { mutableStateOf("") }
    var telefono  by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var showPass  by remember { mutableStateOf(false) }
    var visible   by remember { mutableStateOf(false) }
    val state     by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { visible = true }

    // Animación flotante del ícono
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val pawOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "float"
    )

    // Diálogo de éxito
    if (state is RegisterState.Success) {
        AlertDialog(
            onDismissRequest = {},
            shape = RoundedCornerShape(24.dp),
            containerColor = SurfaceWhite,
            icon = { Text("🎉", fontSize = 36.sp) },
            title = {
                Text(
                    "¡Cuenta creada!",
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    "Ya puedes buscar y reportar mascotas perdidas en tu comunidad.",
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = onRegisterSuccess,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 10.dp)
                            .background(
                                Brush.horizontalGradient(listOf(PetOrange, PetOrangeLight)),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 24.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🐾  Ir al inicio", color = SurfaceWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    // ── Fondo ─────────────────────────────────────────────────────────────────
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
                center = Offset(size.width * 0.85f, size.height * 0.06f)
            )
            drawCircle(
                color = PetYellow.copy(alpha = 0.10f),
                radius = 180.dp.toPx(),
                center = Offset(size.width * 0.1f, size.height * 0.92f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(52.dp))

            // ── Hero ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .offset(y = pawOffset.dp)
                    .size(88.dp)
                    .shadow(14.dp, CircleShape)
                    .background(
                        Brush.radialGradient(colors = listOf(PetOrangeLight, PetOrange)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🐾", fontSize = 40.sp)
            }

            Spacer(Modifier.height(18.dp))

            Text("PetFinder", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = PetBrown, letterSpacing = (-0.5).sp)
            Text(
                "Únete y ayuda a reunir familias con sus mascotas",
                fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Estadísticas rápidas
            Row(
                modifier = Modifier.padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("🐕 Perros", "🐈 Gatos", "🏡 Comunidad").forEach { label ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = PetYellow.copy(alpha = 0.30f)
                    ) {
                        Text(
                            label, fontSize = 11.sp, color = PetBrown, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Tarjeta ───────────────────────────────────────────────────
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
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                        Text("Crea tu cuenta", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            "Completa tus datos para empezar",
                            fontSize = 12.sp, color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp, bottom = 22.dp)
                        )

                        // Nombre + Apellido en fila
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                PetTextField(
                                    value = nombre,
                                    onValueChange = { nombre = it },
                                    label = "Nombre",
                                    leadingIcon = { Icon(Icons.Default.Person, null, tint = PetOrange) }
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        PetTextField(
                            value = correo,
                            onValueChange = { correo = it },
                            label = "Correo electrónico",
                            leadingIcon = { Icon(Icons.Default.Email, null, tint = PetOrange) },
                            keyboardType = KeyboardType.Email
                        )

                        Spacer(Modifier.height(14.dp))

                        PetTextField(
                            value = telefono,
                            onValueChange = { telefono = it },
                            label = "Teléfono (opcional)",
                            leadingIcon = { Icon(Icons.Default.Phone, null, tint = PetOrange) },
                            keyboardType = KeyboardType.Phone
                        )

                        Spacer(Modifier.height(14.dp))

                        PetTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Contraseña",
                            leadingIcon = { Icon(Icons.Default.Lock, null, tint = PetOrange) },
                            trailingIcon = {
                                IconButton(onClick = { showPass = !showPass }) {
                                    Icon(
                                        if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        null, tint = TextSecondary
                                    )
                                }
                            },
                            keyboardType = KeyboardType.Password,
                            visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation()
                        )

                        // Error
                        AnimatedVisibility(visible = state is RegisterState.Error) {
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
                                Text("⚠️", fontSize = 15.sp)
                                Text(
                                    (state as? RegisterState.Error)?.message ?: "",
                                    color = Color(0xFFB71C1C), fontSize = 13.sp, fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(Modifier.height(22.dp))

                        // Botón registrarse
                        Button(
                            onClick = {
                                viewModel.registrar(
                                    nombre,correo,telefono,password

                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            enabled = state !is RegisterState.Loading,
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
                                            colors = if (state !is RegisterState.Loading)
                                                listOf(PetOrange, PetOrangeLight)
                                            else
                                                listOf(PetOrange.copy(0.5f), PetOrangeLight.copy(0.5f))
                                        ),
                                        RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (state is RegisterState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.5.dp,
                                        color = SurfaceWhite
                                    )
                                } else {
                                    Text("🐾  Unirme a PetFinder", color = SurfaceWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        TextButton(onClick = onBackToLogin) {
                            Text("¿Ya tienes cuenta? Iniciar sesión", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Footer
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(PetGreen.copy(alpha = 0.15f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("✅", fontSize = 13.sp)
                Text(
                    "+12,400 mascotas reunidas con sus familias",
                    fontSize = 11.sp, color = Color(0xFF065F46), fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(36.dp))
        }
    }
}

// ── Campo reutilizable ─────────────────────────────────────────────────────────
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
        label = { Text(label, fontSize = 12.sp) },
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