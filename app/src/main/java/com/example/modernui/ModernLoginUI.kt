package com.example.modernui

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.modernui.ui.theme.FintechRegisterScreenM3
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FintechLoginScreenM3(

) {

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var acceptTerms by rememberSaveable { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
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
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        // FIX: Named parameters use karein
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
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
                        // FIX: Named parameters use karein
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

                    Button(
                        onClick = {
                            isLoading = true
                            scope.launch {
                                delay(1500)
                                isLoading = false
                                if (email == "admin@softmint.com" && password == "123456") {
                                 //   onLoginSuccess()
                                    Toast.makeText(context, "Rukk Jaa Bhai ", Toast.LENGTH_SHORT).show()

                                } else {
                                    Toast.makeText(context, "Invalid ID or Password", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = email.isNotEmpty() && password.length >= 6 && acceptTerms && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Text("Sign In", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            // Moved "New User" link here - outside the card for better UX
            TextButton(
               onClick = {  },
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
    val count = 12

    val animationStates = (0 until count).map { i ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(8000 + (i * 1500), easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        animationStates.forEachIndexed { i, animState ->
            val progress = animState.value
            val x = (size.width * (i % 4) / 4f) + (progress * 100f)
            val y = (size.height * (i % 3) / 3f) + (progress * 80f)

            drawCircle(
                color = color,
                radius = 120f + (i * 15f),
                center = Offset(x, y)
            )
        }
    }
}
//
//@Composable
//fun AppNavigation() {
//    val navController = rememberNavController()
//
//    NavHost(navController = navController, startDestination = "login") {
//        composable("login") {
//            FintechLoginScreenM3(
//                onLoginSuccess = {
//                    navController.navigate("dashboard") {
//                        popUpTo("login") { inclusive = true }
//                    }
//                },
//                onRegisterClick = { navController.navigate("register") }
//            )
//        }
//        composable("register") {
//            // Ensure this function is defined in your project
//            FintechRegisterScreenM3(onBackToLogin = { navController.popBackStack() })
//        }
//        composable("dashboard") {
//            FintechDashboardM3()
//        }
//    }
//}