package com.v2ray.ang.service

import android.util.Log

class TProxyService {
    companion object {
        init {
            try {
                System.loadLibrary("hev-socks5-tunnel")
            } catch (e: Throwable) {
                Log.e("TProxyService", "Failed to load hev-socks5-tunnel", e)
            }
        }
    }
    
    external fun TProxyStartService(configPath: String, fd: Int)
    external fun TProxyStopService()
    external fun TProxyGetStats(): LongArray?
}
