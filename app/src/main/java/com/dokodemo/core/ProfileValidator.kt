package com.dokodemo.core

import com.dokodemo.data.model.Protocol
import com.dokodemo.data.model.ServerProfile

object ProfileValidator {
    enum class Error { ADDRESS, PORT, CREDENTIAL, REALITY, PROTOCOL }
    fun validate(profile: ServerProfile): Error? = when {
        profile.address.isBlank() || profile.address.any { it.isWhitespace() } || profile.address.any { it in "/?#@" } -> Error.ADDRESS
        profile.port !in 1..65535 -> Error.PORT
        profile.protocol == Protocol.WIREGUARD -> Error.PROTOCOL
        profile.protocol in listOf(Protocol.VMESS, Protocol.VLESS) &&
            !profile.uuid.matches(Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) -> Error.CREDENTIAL
        profile.protocol in listOf(Protocol.TROJAN, Protocol.SHADOWSOCKS) && profile.password.isEmpty() -> Error.CREDENTIAL
        profile.useReality && (profile.realityPublicKey.isBlank() || profile.serverName.isBlank() ||
            !profile.realityShortId.matches(Regex("(?:[0-9a-fA-F]{2}){0,8}"))) -> Error.REALITY
        else -> null
    }
}
