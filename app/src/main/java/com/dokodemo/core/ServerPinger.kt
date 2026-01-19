package com.dokodemo.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ServerPinger handles real TCP latency measurement.
 */
@Singleton
class ServerPinger @Inject constructor() {

    /**
     * Pings a server using TCP connection.
     * Returns the latency in milliseconds, or null if unreachable.
     */
    suspend fun ping(host: String, port: Int, timeout: Int = 3000): Long? {
        return withContext(Dispatchers.IO) {
            try {
                val socket = Socket()
                val start = System.currentTimeMillis()
                socket.connect(InetSocketAddress(host, port), timeout)
                val end = System.currentTimeMillis()
                socket.close()
                end - start
            } catch (e: Exception) {
                null
            }
        }
    }
}
