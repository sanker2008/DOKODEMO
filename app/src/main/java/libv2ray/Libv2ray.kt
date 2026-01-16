package libv2ray

/**
 * LibXray JNI Interface
 * 
 * This interface defines the API for the LibXray library (AndroidLibXrayLite).
 * The actual implementation is provided by the native library (libv2ray.aar).
 * 
 * To use this:
 * 1. Download libv2ray.aar from https://github.com/nicegram/nicegram-xray-core/releases
 * 2. Place it in app/libs/libv2ray.aar
 * 3. Add `implementation(files("libs/libv2ray.aar"))` to build.gradle.kts
 */
object Libv2ray {
    
    init {
        try {
            System.loadLibrary("v2ray")
        } catch (e: UnsatisfiedLinkError) {
            // Library not found - will use fallback
        }
    }
    
    /**
     * Initialize the V2Ray environment
     * @param filesDir Application files directory for assets
     */
    @JvmStatic
    external fun initV2Env(filesDir: String)
    
    /**
     * Start V2Ray with a JSON configuration
     * @param configContent V2Ray JSON configuration
     * @return true if started successfully
     */
    @JvmStatic
    external fun startV2Ray(configContent: String): Boolean
    
    /**
     * Stop V2Ray core
     * @return true if stopped successfully
     */
    @JvmStatic
    external fun stopV2Ray(): Boolean
    
    /**
     * Check if V2Ray is running
     * @return true if running
     */
    @JvmStatic
    external fun isRunning(): Boolean
    
    /**
     * Get V2Ray version
     * @return version string
     */
    @JvmStatic
    external fun version(): String
    
    /**
     * Get current traffic statistics
     * @return upload,download bytes as comma-separated string
     */
    @JvmStatic
    external fun queryStats(tag: String, direct: String): Long
    
    /**
     * Test configuration validity
     * @param configContent V2Ray JSON configuration
     * @return empty string if valid, error message otherwise
     */
    @JvmStatic
    external fun testConfig(configContent: String): String
}
