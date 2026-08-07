package com.dokodemo.utils

import java.net.Inet4Address
import java.net.NetworkInterface

object NetworkUtils {
    /**
     * Get the local IPv4 address of the device.
     * Often used for finding the Wi-Fi or local network IP.
     */
    fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                // Skip loopback and inactive interfaces, and common virtual ones like tun/tap
                if (networkInterface.isLoopback || !networkInterface.isUp || networkInterface.name.startsWith("tun")) {
                    continue
                }

                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
