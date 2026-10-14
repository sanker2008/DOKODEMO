package com.dokodemo.core

import java.net.URI
import java.util.Locale

sealed interface ImportSource {
    data class Node(val uri: String) : ImportSource
    data class Subscription(val url: String) : ImportSource
    data object Unsupported : ImportSource
}

/** Shared by explicit clipboard import and QR scan results; classification never accesses the network. */
object ImportSourceClassifier {
    fun classify(text: String): ImportSource {
        val value = text.trim().removePrefix("\uFEFF").trim()
        if (isSubscriptionUrl(value)) return ImportSource.Subscription(value)
        if (ShareLinkParser().parse(value) != null) return ImportSource.Node(value)
        return ImportSource.Unsupported
    }

    fun isSubscriptionUrl(value: String): Boolean = runCatching {
        val uri = URI(value.trim())
        uri.scheme?.lowercase(Locale.ROOT) in listOf("http", "https") &&
            !uri.host.isNullOrBlank() && (uri.port == -1 || uri.port in 1..65535)
    }.getOrDefault(false)

    fun suggestedSubscriptionName(url: String): String = runCatching {
        URI(url.trim()).host.orEmpty().removeSurrounding("[", "]")
    }.getOrDefault("")
}
