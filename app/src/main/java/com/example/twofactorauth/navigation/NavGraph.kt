package com.example.twofactorauth.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.twofactorauth.ui.add.AddAccountScreen
import com.example.twofactorauth.ui.add.AddAccountViewModel
import com.example.twofactorauth.ui.home.HomeScreen
import com.example.twofactorauth.ui.scan.QrScannerScreen
import com.example.twofactorauth.ui.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val ADD_ACCOUNT = "add_account"
    const val SETTINGS = "settings"
    const val SCAN_QR = "scan_qr"
}

// Navigation result key
const val SCAN_RESULT_KEY = "scan_result"

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onAddAccount = { navController.navigate(Routes.ADD_ACCOUNT) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.ADD_ACCOUNT) { backStackEntry ->
            val viewModel: AddAccountViewModel = hiltViewModel()

            // Check for scan result from previous back stack entry
            val savedStateHandle = backStackEntry.savedStateHandle
            val scanResult by savedStateHandle.getStateFlow<String?>(SCAN_RESULT_KEY, null)
                .collectAsState()

            // Load scan result when received
            LaunchedEffect(scanResult) {
                scanResult?.let { uri ->
                    viewModel.loadFromUri(uri)
                    savedStateHandle.remove<String>(SCAN_RESULT_KEY)
                }
            }

            AddAccountScreen(
                onNavigateBack = { navController.popBackStack() },
                onScanQr = { navController.navigate(Routes.SCAN_QR) }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SCAN_QR) {
            QrScannerScreen(
                onScanResult = { uri ->
                    // Set result on the previous back stack entry
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(SCAN_RESULT_KEY, uri)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
