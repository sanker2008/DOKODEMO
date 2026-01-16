package go.tun2socks

/**
 * Tun2socks JNI Interface
 * 
 * This interface bridges Android VpnService TUN interface with V2Ray SOCKS proxy.
 * The implementation is provided by the native library built from go-tun2socks.
 */
object Tun2socks {
    
    /**
     * Start tun2socks with the given parameters
     * 
     * @param tunFd File descriptor from VpnService.Builder.establish()
     * @param socksAddr SOCKS5 proxy address (e.g., "127.0.0.1:10808")
     * @param dnsAddr DNS server address (e.g., "8.8.8.8")
     * @param mtu MTU value (typically 1500)
     * @return true if started successfully
     */
    @JvmStatic
    external fun start(tunFd: Int, socksAddr: String, dnsAddr: String, mtu: Int): Boolean
    
    /**
     * Stop tun2socks
     */
    @JvmStatic
    external fun stop()
    
    /**
     * Check if tun2socks is running
     */
    @JvmStatic
    external fun isRunning(): Boolean
    
    /**
     * Get traffic statistics
     * @return upload bytes
     */
    @JvmStatic
    external fun getUploadBytes(): Long
    
    /**
     * Get traffic statistics
     * @return download bytes
     */
    @JvmStatic
    external fun getDownloadBytes(): Long
    
    init {
        try {
            System.loadLibrary("tun2socks")
        } catch (e: UnsatisfiedLinkError) {
            // Library not found
        }
    }
}
