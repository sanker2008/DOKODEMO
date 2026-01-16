package com.dokodemo.navigation

/**
 * Navigation routes for DokoDemo
 */
sealed class Route(val path: String) {
    object Splash : Route("splash")
    
    // Main App
    object Home : Route("home")
    object ServerList : Route("server_list")
    object ConfigEditor : Route("config_editor/new")
    object ConfigEditorEdit : Route("config_editor/edit/{serverId}") {
        fun createRoute(serverId: Long) = "config_editor/edit/$serverId"
    }
    
    // Tools
    object QrScanner : Route("qr_scanner")
    object Settings : Route("settings")
    object SplitTunneling : Route("split_tunneling")
    // object SystemLogs : Route("logs") // Not implemented yet
    object Subscriptions : Route("subscriptions")
    object Logs : Route("logs")
}
