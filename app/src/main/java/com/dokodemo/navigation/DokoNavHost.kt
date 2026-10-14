package com.dokodemo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dokodemo.R
import com.dokodemo.core.ImportSource
import com.dokodemo.core.ImportSourceClassifier
import com.dokodemo.ui.screens.configeditor.ConfigEditorScreen
import com.dokodemo.ui.screens.home.HomeScreen
import com.dokodemo.ui.screens.qrscanner.QrScannerScreen
import com.dokodemo.ui.screens.serverlist.ServerListScreen

@Composable
fun DokoNavHost(
    navController: NavHostController = rememberNavController()
) {
    var showUnsupportedImport by remember { mutableStateOf(false) }
    fun importDestination(text: String): String? = when (val source = ImportSourceClassifier.classify(text)) {
        is ImportSource.Node -> "${Route.ConfigEditor.path}?uri=${android.net.Uri.encode(source.uri)}"
        is ImportSource.Subscription -> Route.Subscriptions.createRoute(source.url)
        ImportSource.Unsupported -> null
    }
    if (showUnsupportedImport) {
        AlertDialog(
            onDismissRequest = { showUnsupportedImport = false },
            title = { Text(stringResource(R.string.import_unsupported_title)) },
            text = { Text(stringResource(R.string.import_unsupported_message)) },
            confirmButton = {
                TextButton(onClick = { showUnsupportedImport = false }) { Text(stringResource(R.string.confirm)) }
            }
        )
    }
    NavHost(
        navController = navController,
        startDestination = Route.Home.path
    ) {
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
                onNavigateToSubscriptions = { navController.navigate(Route.Subscriptions.path) },
                onNavigateToAddProfile = {
                    navController.navigate(Route.QrScanner.path)
                },
                onNavigateToConfigEditor = { serverId ->
                    if (serverId != null) {
                        navController.navigate(Route.ConfigEditorEdit.createRoute(serverId))
                    } else {
                        navController.navigate(Route.ConfigEditor.path)
                    }
                },
                onNavigateToConfigEditorWithUri = { uri ->
                    val destination = importDestination(uri)
                    if (destination != null) navController.navigate(destination)
                    else showUnsupportedImport = true
                }
            )
        }
        
        // ConfigEditor (New / Import)
        composable(
            route = "${Route.ConfigEditor.path}?uri={uri}",
            arguments = listOf(
                navArgument("uri") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val uri = backStackEntry.arguments?.getString("uri")
            val scannedUri = navController.previousBackStackEntry?.savedStateHandle?.get<String>("scannedUri")
            val finalUri = uri ?: scannedUri
            
            // Clean up to prevent re-triggering
            navController.previousBackStackEntry?.savedStateHandle?.remove<String>("scannedUri")

            ConfigEditorScreen(
                serverId = null,
                uri = finalUri,
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
                    val destination = importDestination(code)
                    if (destination != null) {
                        navController.navigate(destination) {
                            popUpTo(Route.QrScanner.path) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                        showUnsupportedImport = true
                    }
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
                },
                onNavigateToCustomRules = {
                    navController.navigate(Route.CustomRoutingRules.path)
                },
                onNavigateToSubscriptions = {
                    navController.navigate(Route.Subscriptions.path)
                },
                onNavigateToLogs = {
                    navController.navigate(Route.Logs.path)
                },
                onNavigateToTrafficHistory = {
                    navController.navigate(Route.TrafficHistory.path)
                }
            )
        }
        
        // Logs
        composable(Route.Logs.path) {
            com.dokodemo.ui.screens.logs.LogsScreen(
                onNavigateBack = {
                    navController.popBackStack()
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

        composable(Route.CustomRoutingRules.path) {
            com.dokodemo.ui.screens.routing.CustomRoutingRulesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Subscriptions
        composable(
            Route.Subscriptions.pattern,
            arguments = listOf(navArgument("url") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            com.dokodemo.ui.screens.subscription.SubscriptionScreen(
                initialUrl = backStackEntry.arguments?.getString("url"),
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateHome = {
                    navController.popBackStack(Route.Home.path, inclusive = false)
                }
            )
        }
        
        // Traffic History
        composable(Route.TrafficHistory.path) {
            com.dokodemo.ui.screens.traffichistory.TrafficHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
