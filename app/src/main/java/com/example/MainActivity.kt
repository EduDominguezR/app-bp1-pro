package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NoiseAware
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.BaseusPulseViewModel
import com.example.ui.components.AirPodsConnectionPopup
import com.example.ui.screens.AncScreen
import com.example.ui.screens.ConnectionScreen
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BaseusPulseTheme
import com.example.ui.theme.PulseBlue
import com.example.ui.theme.PulseCyan

enum class PulseScreen(val title: String, val icon: ImageVector) {
    HOME("Inicio", Icons.Default.Home),
    ANC("ANC", Icons.Default.NoiseAware),
    EQUALIZER("EQ", Icons.Default.GraphicEq),
    CONNECTION("Conexión", Icons.Default.Bluetooth),
    SETTINGS("Ajustes", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: BaseusPulseViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        viewModel.setAppForeground(true)
    }

    override fun onPause() {
        super.onPause()
        viewModel.setAppForeground(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (intent?.getBooleanExtra("SHOW_POPUP", false) == true) {
            viewModel.triggerInAppPopup()
        }

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()

            BaseusPulseTheme(themeMode = themeMode) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("SHOW_POPUP", false)) {
            viewModel.triggerInAppPopup()
        }
    }
}

@Composable
fun MainAppContent(viewModel: BaseusPulseViewModel) {
    var currentScreen by remember { mutableStateOf(PulseScreen.HOME) }
    val showPopup by viewModel.showInAppPopup.collectAsState()
    val popupDeviceName by viewModel.popupDeviceName.collectAsState()
    val batteryInfo by viewModel.batteryInfo.collectAsState()

    // Request Bluetooth and Notification runtime permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Permissions handled gracefully
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    // Android back handling: Return to Home screen if on secondary screen
    BackHandler(enabled = currentScreen != PulseScreen.HOME || showPopup) {
        if (showPopup) {
            viewModel.dismissPopup()
        } else {
            currentScreen = PulseScreen.HOME
        }
    }

    val themeMode by viewModel.themeMode.collectAsState()
    val isGlass = themeMode == com.example.data.AppThemeMode.LIQUID_GLASS

    com.example.ui.components.glass.GlassBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = if (isGlass) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.background,
                bottomBar = {
                    val navBarColor = if (isGlass) {
                        Color(0xE60D1324)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }

                    val navShape = if (isGlass) RoundedCornerShape(26.dp) else RoundedCornerShape(0.dp)
                    val navBorder = if (isGlass) {
                        BorderStroke(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.32f),
                                    Color.White.copy(alpha = 0.08f)
                                )
                            )
                        )
                    } else null

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(
                                horizontal = if (isGlass) 16.dp else 0.dp,
                                vertical = if (isGlass) 10.dp else 0.dp
                            )
                    ) {
                        Surface(
                            shape = navShape,
                            color = navBarColor,
                            border = navBorder,
                            shadowElevation = if (isGlass) 12.dp else 3.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(navShape)
                        ) {
                            NavigationBar(
                                containerColor = Color.Transparent,
                                tonalElevation = 0.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("main_navigation_bar")
                            ) {
                                PulseScreen.entries.forEach { screen ->
                                    val isSelected = currentScreen == screen
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentScreen = screen },
                                        icon = {
                                            Icon(
                                                imageVector = screen.icon,
                                                contentDescription = screen.title
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = screen.title,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = PulseCyan,
                                            selectedTextColor = PulseCyan,
                                            unselectedIconColor = Color.White.copy(alpha = 0.55f),
                                            unselectedTextColor = Color.White.copy(alpha = 0.55f),
                                            indicatorColor = if (isGlass) PulseBlue.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                when (currentScreen) {
                    PulseScreen.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToEq = { currentScreen = PulseScreen.EQUALIZER },
                        onNavigateToAnc = { currentScreen = PulseScreen.ANC },
                        onNavigateToConnection = { currentScreen = PulseScreen.CONNECTION }
                    )
                    PulseScreen.ANC -> AncScreen(viewModel = viewModel)
                    PulseScreen.EQUALIZER -> EqualizerScreen(viewModel = viewModel)
                    PulseScreen.CONNECTION -> ConnectionScreen(viewModel = viewModel)
                    PulseScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }

            // AirPods-style Connection Pop-up Overlay
            AirPodsConnectionPopup(
                visible = showPopup,
                deviceName = popupDeviceName,
                batteryInfo = batteryInfo,
                onDismiss = { viewModel.dismissPopup() },
                onNavigateToEq = { currentScreen = PulseScreen.EQUALIZER },
                onNavigateToAnc = { currentScreen = PulseScreen.ANC }
            )
        }
    }
}
