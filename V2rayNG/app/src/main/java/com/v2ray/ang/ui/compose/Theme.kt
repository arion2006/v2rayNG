package com.v2ray.ang.ui.compose

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.v2ray.ang.AppConfig
import com.v2ray.ang.handler.MmkvManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val LightColor = lightColorScheme(
    primary = Color(0xFF2F6FD0), // Accent Blue
    onPrimary = Color(0xFFFFFFFF), // White
    primaryContainer = Color(0xFFD6E4F8), // Pale Accent Blue
    onPrimaryContainer = Color(0xFF0B2B57), // Deep Blue
    secondary = Color(0xFF5F6873), // Secondary Text Gray
    onSecondary = Color(0xFFFFFFFF), // White
    secondaryContainer = Color(0xFFE7EBF0), // Light Blue Gray
    onSecondaryContainer = Color(0xFF191C20), // Near Black
    tertiary = Color(0xFF2E7D4F), // Green
    onTertiary = Color(0xFFFFFFFF), // White
    tertiaryContainer = Color(0xFFCDEEDB), // Light Green
    onTertiaryContainer = Color(0xFF0C2E1C), // Dark Green
    error = Color(0xFFBA1A1A), // Red
    errorContainer = Color(0xFFFFDAD6), // Light Red
    onError = Color(0xFFFFFFFF), // White
    onErrorContainer = Color(0xFF410002), // Dark Red
    background = Color(0xFFF4F6F9), // App Background
    onBackground = Color(0xFF191C20), // Primary Text
    surface = Color(0xFFFFFFFF), // Card White
    onSurface = Color(0xFF191C20), // Primary Text
    surfaceVariant = Color(0xFFE7EBF0), // Light Blue Gray
    onSurfaceVariant = Color(0xFF5F6873), // Secondary Text
    outline = Color(0xFF8A94A3), // Medium Gray
    outlineVariant = Color(0xFFD3D9E1), // Light Gray
    inverseSurface = Color(0xFF1D222B), // Dark Surface
    inverseOnSurface = Color(0xFFE8EAED), // Light Gray
    inversePrimary = Color(0xFF6FA8FF), // Accent Blue Light
    scrim = Color(0xFF000000), // Black
    surfaceTint = Color(0xFF2F6FD0), // Accent Blue
    surfaceContainerLowest = Color(0xFFFFFFFF), // Card White
    surfaceContainerLow = Color(0xFFFFFFFF), // Card White
    surfaceContainer = Color(0xFFFFFFFF), // Card White
    surfaceContainerHigh = Color(0xFFFDFEFF), // Card White
    surfaceContainerHighest = Color(0xFFFBFCFE), // Card White
)

private val DarkColor = darkColorScheme(
    primary = Color(0xFF6FA8FF), // Accent Blue
    onPrimary = Color(0xFF0A1E3F), // Deep Blue
    primaryContainer = Color(0xFF1D3A66), // Muted Accent Blue
    onPrimaryContainer = Color(0xFFCFE2FF), // Pale Blue
    secondary = Color(0xFF9AA3AF), // Secondary Text Gray
    onSecondary = Color(0xFF10141A), // Near Black
    secondaryContainer = Color(0xFF262C36), // Blue Gray Surface
    onSecondaryContainer = Color(0xFFE8EAED), // Primary Text
    tertiary = Color(0xFF7BD9A8), // Mint Green
    onTertiary = Color(0xFF0C2E1C), // Dark Green
    tertiaryContainer = Color(0xFF14532D), // Deep Green
    onTertiaryContainer = Color(0xFFCDEEDB), // Light Green
    error = Color(0xFFFFB4AB), // Light Red
    errorContainer = Color(0xFF93000A), // Dark Red
    onError = Color(0xFF690005), // Deep Red
    onErrorContainer = Color(0xFFFFDAD6), // Light Red
    background = Color(0xFF0B0D10), // App Background
    onBackground = Color(0xFFE8EAED), // Primary Text
    surface = Color(0xFF171B21), // Card Surface
    onSurface = Color(0xFFE8EAED), // Primary Text
    surfaceVariant = Color(0xFF1D222B), // Elevated Surface
    onSurfaceVariant = Color(0xFF9AA3AF), // Secondary Text
    outline = Color(0xFF3A414C), // Blue Gray Outline
    outlineVariant = Color(0xFF262C36), // Blue Gray Surface
    inverseSurface = Color(0xFFE8EAED), // Primary Text
    inverseOnSurface = Color(0xFF171B21), // Card Surface
    inversePrimary = Color(0xFF2F6FD0), // Accent Blue Dark
    scrim = Color(0xFF000000), // Black
    surfaceTint = Color(0xFF6FA8FF), // Accent Blue
    surfaceContainerLowest = Color(0xFF08090C), // Below Background
    surfaceContainerLow = Color(0xFF12151A), // Low Surface
    surfaceContainer = Color(0xFF171B21), // Card Surface
    surfaceContainerHigh = Color(0xFF1D222B), // Elevated Surface
    surfaceContainerHighest = Color(0xFF232833), // Highest Surface
)

// Semantic Colors
val colorPing = Color(0xFF009966) // Green
val colorPingRed = Color(0xFFFF0099) // Pink Red
val colorConfigType = Color(0xFF6FA8FF) // Accent Blue
val colorFabActive = Color(0xFF6FA8FF) // Accent Blue
val colorFabInactiveLight = Color(0xFF8A94A3) // Blue Gray
val colorFabInactiveDark = Color(0xFF3A414C) // Blue Gray Outline
val dividerColorLight = Color(0xFFD3D9E1) // Light Gray
val dividerColorDark = Color(0xFF262C36) // Blue Gray Surface

// Toast Colors 85%
val toastNormalBgLight = Color(0xD9353A3E) // Dark Gray
val toastNormalBgDark = Color(0xD94A4F54) // Darker Gray
val toastSuccessBg = Color(0xD9388E3C) // Green
val toastErrorBg = Color(0xD9D50000) // Red
val toastInfoBg = Color(0xD93F51B5) // Indigo Blue
val toastIconCircleBg = Color(0x33FFFFFF) // Semi-transparent White
val toastTextColor = Color.White // White

object ThemeManager {
    private val _themeMode = MutableStateFlow(
        MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "0") ?: "0"
    )
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _dynamicColorEnabled = MutableStateFlow(
        MmkvManager.decodeSettingsBool(AppConfig.PREF_DYNAMIC_COLOR, true)
    )
    val dynamicColorEnabled: StateFlow<Boolean> = _dynamicColorEnabled.asStateFlow()

    fun setThemeMode(mode: String) {
        MmkvManager.encodeSettings(AppConfig.PREF_UI_MODE_NIGHT, mode)
        _themeMode.value = mode
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        MmkvManager.encodeSettings(AppConfig.PREF_DYNAMIC_COLOR, enabled)
        _dynamicColorEnabled.value = enabled
    }

    fun refresh() {
        _themeMode.value =
            MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "0") ?: "0"
        _dynamicColorEnabled.value =
            MmkvManager.decodeSettingsBool(AppConfig.PREF_DYNAMIC_COLOR, true)
    }
}

@Composable
fun resolveDarkTheme(): Boolean {
    val mode by ThemeManager.themeMode.collectAsState()
    return when (mode) {
        "1" -> false
        "2" -> true
        else -> isSystemInDarkTheme()
    }
}

val LocalDarkTheme = compositionLocalOf { false }

@Composable
fun AppTheme(
    darkTheme: Boolean = resolveDarkTheme(),
    content: @Composable () -> Unit
) {
    val dynamicColor by ThemeManager.dynamicColorEnabled.collectAsState()
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColor
        else -> LightColor
    }
    val snackbarController = rememberAppSnackbarController()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val window = activity.window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalAppSnackbar provides snackbarController
    ) {
        MaterialTheme(
            colorScheme = colorScheme
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AppSnackbarBridge(controller = snackbarController)
                content()
                AppSnackbarHost(hostState = snackbarController.hostState)
            }
        }
    }
}
