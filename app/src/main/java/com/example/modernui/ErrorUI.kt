package com.example.modernui


import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────
// 1. API Error Model
// ─────────────────────────────────────────────

sealed class ApiError {
    data class NetworkError(
        val message: String = "Please check your internet connection and try again."
    ) : ApiError()

    data class TimeoutError(
        val message: String = "The request took too long. Please try again."
    ) : ApiError()

    data class ServerError(
        val code: Int,
        val message: String = "Our servers are having issues. Please try again later."
    ) : ApiError()

    data class UnauthorizedError(
        val message: String = "Your session has expired. Please log in again."
    ) : ApiError()

    data class NotFoundError(
        val message: String = "The requested resource could not be found."
    ) : ApiError()

    data class ValidationError(
        val message: String = "Some fields are invalid. Please review and try again."
    ) : ApiError()

    data class TooManyRequestsError(
        val message: String = "You've made too many requests. Please wait a moment."
    ) : ApiError()

    data class UnknownError(
        val message: String = "An unexpected error occurred. Please try again."
    ) : ApiError()
}

// ─────────────────────────────────────────────
// 2. API Error Mapper (maps HTTP codes → ApiError)
// ────────────────────────────────────────────

object ApiErrorMapper {
    fun fromHttpCode(code: Int, serverMessage: String? = null): ApiError {
        return when (code) {
            401, 403 -> ApiError.UnauthorizedError(
                message = serverMessage ?: "Your session has expired. Please log in again."
            )
            404 -> ApiError.NotFoundError(
                message = serverMessage ?: "The requested resource could not be found."
            )
            408, 504 -> ApiError.TimeoutError(
                message = serverMessage ?: "The request timed out. Please try again."
            )
            422 -> ApiError.ValidationError(
                message = serverMessage ?: "Some fields are invalid. Please review and try again."
            )
            429 -> ApiError.TooManyRequestsError(
                message = serverMessage ?: "Too many requests. Please slow down."
            )
            in 500..599 -> ApiError.ServerError(
                code = code,
                message = serverMessage ?: "Server error ($code). Please try again later."
            )
            else -> ApiError.UnknownError(
                message = serverMessage ?: "Unexpected error (code: $code)."
            )
        }
    }

    fun fromException(e: Exception): ApiError {
        return when {
            e is java.net.UnknownHostException ||
                    e is java.net.ConnectException -> ApiError.NetworkError()

            e is java.net.SocketTimeoutException -> ApiError.TimeoutError()

            else -> ApiError.UnknownError(message = e.localizedMessage ?: "Unknown error.")
        }
    }
}

// ─────────────────────────────────────────────
// 3. Error UI Config (icon, title, colors per type)
// ─────────────────────────────────────────────

data class ErrorUiConfig(
    val icon: ImageVector,
    val title: String,
    val iconTint: Color,
    val iconBgColor: Color,
    val showRetry: Boolean = true,
    val retryLabel: String = "Try Again",
    val showBack: Boolean = true,
    val showSecondaryAction: Boolean = false,
    val secondaryLabel: String = "",
)

@Composable
fun ApiError.toUiConfig(): ErrorUiConfig {
    return when (this) {
        is ApiError.NetworkError -> ErrorUiConfig(
            icon = Icons.Outlined.WifiOff,
            title = "No Internet Connection",
            iconTint = MaterialTheme.colorScheme.error,
            iconBgColor = MaterialTheme.colorScheme.errorContainer,
        )
        is ApiError.TimeoutError -> ErrorUiConfig(
            icon = Icons.Outlined.HourglassEmpty,
            title = "Request Timed Out",
            iconTint = MaterialTheme.colorScheme.tertiary,
            iconBgColor = MaterialTheme.colorScheme.tertiaryContainer,
        )
        is ApiError.ServerError -> ErrorUiConfig(
            icon = Icons.Outlined.Dns,
            title = "Server Error (${this.code})",
            iconTint = MaterialTheme.colorScheme.error,
            iconBgColor = MaterialTheme.colorScheme.errorContainer,
        )
        is ApiError.UnauthorizedError -> ErrorUiConfig(
           icon = Icons.Outlined.Lock,
            title = "Session Expired",
            iconTint = MaterialTheme.colorScheme.secondary,
            iconBgColor = MaterialTheme.colorScheme.secondaryContainer,
            showRetry = false,
            showSecondaryAction = true,
            secondaryLabel = "Log In Again",
        )
        is ApiError.NotFoundError -> ErrorUiConfig(
            icon = Icons.Outlined.SearchOff,
            title = "Not Found",
            iconTint = MaterialTheme.colorScheme.primary,
            iconBgColor = MaterialTheme.colorScheme.primaryContainer,
            showRetry = false,
        )
        is ApiError.ValidationError -> ErrorUiConfig(
            icon = Icons.Outlined.WarningAmber,
            title = "Validation Error",
            iconTint = Color(0xFFF59E0B),
            iconBgColor = Color(0xFFFEF3C7),
            retryLabel = "Fix & Retry",
        )
        is ApiError.TooManyRequestsError -> ErrorUiConfig(
            icon = Icons.Outlined.Timer,
            title = "Slow Down",
            iconTint = Color(0xFFF97316),
            iconBgColor = Color(0xFFFEF0E7),
        )
        is ApiError.UnknownError -> ErrorUiConfig(
            icon = Icons.Outlined.ErrorOutline,
            title = "Something Went Wrong",
            iconTint = MaterialTheme.colorScheme.error,
            iconBgColor = MaterialTheme.colorScheme.errorContainer,
        )
    }
}

// ─────────────────────────────────────────────
// 4. Main CommonErrorScreen
// ─────────────────────────────────────────────

@Composable
fun CommonErrorScreen(
    apiError: ApiError,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onSecondaryAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val config = apiError.toUiConfig()
    val errorMessage = when (apiError) {
        is ApiError.NetworkError -> apiError.message
        is ApiError.TimeoutError -> apiError.message
        is ApiError.ServerError -> apiError.message
        is ApiError.UnauthorizedError -> apiError.message
        is ApiError.NotFoundError -> apiError.message
        is ApiError.ValidationError -> apiError.message
        is ApiError.TooManyRequestsError -> apiError.message
        is ApiError.UnknownError -> apiError.message
    }

    // Pulse animation on icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )

    // Entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(
            tween(400, easing = EaseOutCubic)
        ) { it / 4 }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Icon with background circle ──
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(config.iconBgColor.copy(alpha = 0.3f))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(config.iconBgColor.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = config.icon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = config.iconTint
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Title ──
            Text(
                text = config.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(10.dp))

            // ── Message ──
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp,
                modifier = Modifier.widthIn(max = 320.dp)
            )

            Spacer(Modifier.height(36.dp))

            // ── Primary Button (Retry) ──
            if (config.showRetry) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = config.retryLabel,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            // ── Secondary Action (e.g. Log In Again) ──
            if (config.showSecondaryAction && onSecondaryAction != null) {
                Button(
                    onClick = onSecondaryAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = config.secondaryLabel,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            // ── Go Back (Text button) ──
            if (config.showBack) {
                TextButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Go Back",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 5. Convenience overload: pass HTTP code directly
// ─────────────────────────────────────────────

@Composable
fun CommonErrorScreen(
    httpCode: Int,
    serverMessage: String? = null,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onSecondaryAction: (() -> Unit)? = null,
) {
    CommonErrorScreen(
        apiError = ApiErrorMapper.fromHttpCode(httpCode, serverMessage),
        onRetry = onRetry,
        onBack = onBack,
        onSecondaryAction = onSecondaryAction,
    )
}

// ─────────────────────────────────────────────
// 6. Usage examples
// ─────────────────────────────────────────────

/*
// ── From HTTP response code ──
CommonErrorScreen(
    httpCode = 401,
    serverMessage = "Token expired",
    onRetry = { viewModel.retry() },
    onBack = { navController.popBackStack() },
    onSecondaryAction = { navController.navigate("login") }
)

// ── From sealed class directly ──
CommonErrorScreen(
    apiError = ApiError.NetworkError(),
    onRetry = { viewModel.retry() },
    onBack = { navController.popBackStack() }
)

// ── From exception in ViewModel ──
val error = ApiErrorMapper.fromException(exception)
CommonErrorScreen(
    apiError = error,
    onRetry = { viewModel.retry() },
    onBack = { navController.popBackStack() }
)

// ── In a LazyColumn item (inline, not full screen) ──
CommonErrorScreen(
    apiError = ApiError.ServerError(500),
    onRetry = { viewModel.retry() },
    onBack = { navController.popBackStack() },
    modifier = Modifier.fillMaxWidth().height(400.dp)
)
*/