package com.dokodemo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dokodemo.ui.screens.configeditor.ConfigEditorScreen
import com.dokodemo.ui.screens.home.HomeScreen
import com.dokodemo.ui.screens.qrscanner.QrScannerScreen
import com.dokodemo.ui.screens.serverlist.ServerListScreen
import com.dokodemo.ui.screens.splash.SplashScreen

@Composable
fun DokoNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Route.Splash.path
    ) {
        // Splash Screen
        composable(Route.Splash.path) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Route.Home.path) {
                        popUpTo(Route.Splash.path) { inclusive = true }
                    }
                }
            )
        }
        
        // Home Dashboard
        composable(Route.Home.path) {
            HomeScreen(
                onNavigateToServerList = {
                    navController.navigate(Route.ServerList.path)
                },
                onNavigateToSettings = {
                    navController.navigate(Route.Settings.path)
                }
            )
        }
        
        // Server List
        composable(Route.ServerList.path) {
            ServerListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAddProfile = {
                    navController.navigate(Route.QrScanner.path)
                },
                onNavigateToConfigEditor = { serverId ->
                    if (serverId != null) {
                        navController.navigate(Route.ConfigEditorEdit.createRoute(serverId))
                    } else {
                        navController.navigate(Route.ConfigEditor.path)
                    }
                }
            )
        }
        
        // Config Editor (New)
        composable(Route.ConfigEditor.path) {
            ConfigEditorScreen(
                serverId = null,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Config Editor (Edit existing)
        composable(
            route = Route.ConfigEditorEdit.path,
            arguments = listOf(
                navArgument("serverId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val serverId = backStackEntry.arguments?.getLong("serverId")
            ConfigEditorScreen(
                serverId = serverId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // QR Scanner
        composable(Route.QrScanner.path) {
            QrScannerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onQrCodeScanned = { code ->
                    // TODO: Parse and save profile
                    navController.popBackStack()
                }
            )
        }
        
        // Settings
        composable(Route.Settings.path) {
            com.dokodemo.ui.screens.settings.SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSplitTunneling = {
                    navController.navigate(Route.SplitTunneling.path)
                }
            )
        }
        
        // Split Tunneling
        composable(Route.SplitTunneling.path) {
            com.dokodemo.ui.screens.splittunneling.SplitTunnelingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
