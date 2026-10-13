package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.outlined.Biotech
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BiometricLockOverlay
import com.example.ui.screens.CheckupsScreen
import com.example.ui.screens.LabTestsScreen
import com.example.ui.screens.MedicationsScreen
import com.example.ui.screens.PcpReportAndSyncScreen
import com.example.ui.screens.VitalsOverviewScreen
import com.example.ui.theme.CareSyncTheme
import com.example.ui.viewmodel.HealthTab
import com.example.ui.viewmodel.HealthViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: HealthViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            CareSyncTheme(darkTheme = isDarkMode) {
                CareSyncApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareSyncApp(viewModel: HealthViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isAppLocked by viewModel.isAppLocked.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val pinError by viewModel.pinError.collectAsState()
    val isElderlyMode by viewModel.isElderlyMode.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val tabTitle = when (currentTab) {
        HealthTab.VITALS -> "CareSync • Vitals"
        HealthTab.CHECKUPS -> "CareSync • Checkups"
        HealthTab.MEDICATIONS -> "CareSync • Medications"
        HealthTab.LAB_TESTS -> "CareSync • Lab Tests"
        HealthTab.REPORTS_SYNC -> "CareSync • PCP Report"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tabTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = if (isElderlyMode) 20.sp else 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    actions = {
                        // Elderly Mode Quick Toggle
                        IconButton(
                            onClick = { viewModel.toggleElderlyMode() },
                            modifier = Modifier.testTag("toggle_elderly_mode_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessibilityNew,
                                contentDescription = "Toggle Elderly High-Legibility Mode",
                                tint = if (isElderlyMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Dark Mode Quick Toggle
                        IconButton(
                            onClick = { viewModel.toggleDarkMode() },
                            modifier = Modifier.testTag("toggle_dark_mode_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Dark Mode",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Privacy Lock Button
                        if (isBiometricEnabled) {
                            IconButton(
                                onClick = { viewModel.lockApp() },
                                modifier = Modifier.testTag("app_lock_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock Medical App",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                )
            },
            bottomBar = {
                CareSyncBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.setTab(it) },
                    isElderlyMode = isElderlyMode
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Crossfade(
                targetState = currentTab,
                label = "TabCrossfade",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { tab ->
                when (tab) {
                    HealthTab.VITALS -> VitalsOverviewScreen(viewModel = viewModel)
                    HealthTab.CHECKUPS -> CheckupsScreen(viewModel = viewModel)
                    HealthTab.MEDICATIONS -> MedicationsScreen(viewModel = viewModel)
                    HealthTab.LAB_TESTS -> LabTestsScreen(viewModel = viewModel)
                    HealthTab.REPORTS_SYNC -> PcpReportAndSyncScreen(viewModel = viewModel)
                }
            }
        }

        // Biometric & PIN Privacy Lock Overlay
        BiometricLockOverlay(
            isLocked = isAppLocked,
            onUnlockWithPin = { pin -> viewModel.unlockApp(pin) },
            onUnlockWithBiometric = { viewModel.unlockViaBiometric() },
            errorMessage = pinError,
            isElderlyMode = isElderlyMode
        )
    }
}

data class NavTabItem(
    val tab: HealthTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun CareSyncBottomBar(
    currentTab: HealthTab,
    onTabSelected: (HealthTab) -> Unit,
    isElderlyMode: Boolean
) {
    val items = listOf(
        NavTabItem(HealthTab.VITALS, "Vitals", Icons.Filled.MonitorHeart, Icons.Outlined.MonitorHeart),
        NavTabItem(HealthTab.CHECKUPS, "Checkups", Icons.Filled.LocalHospital, Icons.Outlined.LocalHospital),
        NavTabItem(HealthTab.MEDICATIONS, "Meds", Icons.Filled.Medication, Icons.Outlined.Medication),
        NavTabItem(HealthTab.LAB_TESTS, "Labs", Icons.Filled.Biotech, Icons.Outlined.Biotech),
        NavTabItem(HealthTab.REPORTS_SYNC, "PCP & Sync", Icons.Filled.PictureAsPdf, Icons.Outlined.PictureAsPdf)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("care_sync_bottom_nav")
    ) {
        items.forEach { item ->
            val isSelected = currentTab == item.tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(if (isElderlyMode) 28.dp else 24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = if (isElderlyMode) 13.sp else 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.testTag("nav_tab_${item.tab.name.lowercase()}")
            )
        }
    }
}
