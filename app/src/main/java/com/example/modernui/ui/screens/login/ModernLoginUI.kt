package com.example.modernui.ui.screens.login

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FintechLoginScreenM3(
    viewModel: UserViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val uiState by viewModel.state.collectAsState()

    var number by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var acceptTerms by rememberSaveable { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            onLoginSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackgroundParticlesM3(color = colorScheme.primary.copy(alpha = 0.08f))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedLogoM3()

            Text(
                text = "SoftMint",
                style = MaterialTheme.typography.displaySmall,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Secure Fintech Solutions",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = colorScheme.surfaceContainerHigh
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = number,
                        onValueChange = { if (it.length <= 10) number = it },
                        label = { Text("User ID") },
                        leadingIcon = { Icon(Icons.Default.PermIdentity, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                     Icon(if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = acceptTerms, onCheckedChange = { acceptTerms = it })
                        Text(
                            text = "I accept the Terms & Conditions",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (uiState is UiState.Error) {
                        Text(
                            text = (uiState as UiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.performLogin( number, password)
                        },
                        enabled = number.isNotEmpty() && password.length >= 6 && acceptTerms && uiState !is UiState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (uiState is UiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Text("Sign In", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            TextButton(
                onClick = { onRegisterClick() },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("New user? Create an account", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun AnimatedLogoM3() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    Icon(
        imageVector = Icons.Default.AccountBalance,
        contentDescription = "App logo",
        modifier = Modifier
            .size(72.dp)
            .scale(scale),
        tint = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun AnimatedBackgroundParticlesM3(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles_bg")
    val count = 4

    val animationStates = (0 until count).map { i ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(8000 + (i * 1500), easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "anim_$i"
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        animationStates.forEachIndexed { i, animState ->
            val progress = animState.value
            val side = if (i % 2 == 0) 0.1f else 0.9f
            val x = (size.width * side) + (sin(progress * 3.14f) * 50f)
            val y = (size.height * 0.2f) + ((size.height * 0.7f) * (i.toFloat() / count)) + (progress * 50f)

            drawCircle(
                color = color,
                radius = 80f + (i * 5f),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        // ── LOGIN ─────────────────────────────────────────────────────────────
        composable("login") {
            // ViewModel is OWNED here — lives as long as login is in the backstack
            val userViewModel: UserViewModel = hiltViewModel()

            FintechLoginScreenM3(
                viewModel = userViewModel,
                onLoginSuccess = {
                    navController.navigate("Userdetail") {
                        // Keep login entry alive so Userdetail can borrow its ViewModel
                        popUpTo("login") { inclusive = false }
                    }
                },
                onRegisterClick = { navController.navigate("register") }
            )
        }

        // ── REGISTER ──────────────────────────────────────────────────────────
        composable("register") {
            FintechRegisterScreenM3(
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // ── USER DETAIL ───────────────────────────────────────────────────────
        composable("Userdetail") {
            // Grab the SAME ViewModel instance that login screen owns
            val loginBackStackEntry = remember(it) {
                navController.getBackStackEntry("login")
            }
            val userViewModel: UserViewModel = hiltViewModel(loginBackStackEntry)
            val uiState by userViewModel.state.collectAsState()

            when (val state = uiState) {
                is UiState.Success -> {
                    UserDetailScreenM3(
                        viewModel = userViewModel,
                        onBackClick = { navController.popBackStack() },
                        onContinueToDashboard = {
                            navController.navigate("dashboard") {
                                // Safe to clear login now — data is already in the dashboard VM
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }
                is UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Error: ${state.message}",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { navController.popBackStack() }) {
                                Text("Back to Login")
                            }
                        }
                    }
                }
                else -> {
                    // Should never reach here — login always sets state before navigating
                    LaunchedEffect(Unit) {
                        navController.navigate("login") { popUpTo(0) }
                    }
                }
            }
        }

        // ── DASHBOARD (shell with all 4 tabs inside) ──────────────────────────
        //
        // ✅ FintechAppShell replaces the old FintechDashboardM3.
        //    Tab switching (Home / Wallet / Report / History) happens internally
        //    via the shared selectedTab state inside FintechAppShellContent.
        //    No extra nav routes needed for the tabs — the back stack stays clean.
        //
        //    The ViewModel is freshly owned here. Login's backstack entry was
        //    cleared by popUpTo("login") { inclusive = true } above, so Hilt
        //    creates a new scoped instance for the dashboard lifecycle.
        //
        composable("dashboard") {
            FintechAppShell()
        }
    }
}
