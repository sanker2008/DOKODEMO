package go.tun2socks

import com.v2ray.ang.service.TProxyService
import java.io.File
import android.util.Log

object Tun2socks {
    private var proxyService: TProxyService? = null
    
    @JvmStatic
    fun start(tunFd: Int, socksAddr: String, dnsAddr: String, mtu: Int): Boolean {
        try {
            val parts = socksAddr.split(":")
            val addr = parts.getOrNull(0) ?: "127.0.0.1"
            val port = parts.getOrNull(1)?.toIntOrNull() ?: 10808
            
            val config = """
                tunnel:
                  mtu: $mtu
                socks5:
                  address: $addr
                  port: $port
                  udp: 'udp'
            """.trimIndent()
            
            val cacheDir = "/data/data/com.dokodemo/cache"
            val file = File(cacheDir, "hev.yml")
            file.parentFile?.mkdirs()
            file.writeText(config)
            
            if (proxyService == null) {
                proxyService = TProxyService()
            }
            proxyService?.TProxyStartService(file.absolutePath, tunFd)
            return true
        } catch (e: Throwable) {
            Log.e("Tun2socks", "Failed to start tunnel", e)
            return false
        }
    }
    
    @JvmStatic
    fun stop() {
        try { proxyService?.TProxyStopService() } catch (_: Throwable) {}
    }
    
    @JvmStatic
    fun isRunning(): Boolean = proxyService != null
    
    @JvmStatic
    fun getUploadBytes(): Long {
        return try { proxyService?.TProxyGetStats()?.get(1) ?: 0L } catch (_: Throwable) { 0L }
    }
    
    @JvmStatic
    fun getDownloadBytes(): Long {
        return try { proxyService?.TProxyGetStats()?.get(3) ?: 0L } catch (_: Throwable) { 0L }
    }
}
