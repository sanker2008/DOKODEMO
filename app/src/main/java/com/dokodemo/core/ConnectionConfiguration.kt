package com.dokodemo.core

import com.dokodemo.data.model.ServerProfile
import com.dokodemo.data.preferences.RoutingMode

/** A connection identity, excluding database identity and display/measurement metadata. */
class ConnectionConfiguration private constructor(
    private val profile: ServerProfile,
    private val routingMode: RoutingMode
) {
    override fun equals(other: Any?): Boolean =
        other is ConnectionConfiguration && profile == other.profile && routingMode == other.routingMode

    override fun hashCode(): Int = 31 * profile.hashCode() + routingMode.hashCode()

    companion object {
        fun from(profile: ServerProfile, routingMode: RoutingMode) = ConnectionConfiguration(
            profile.copy(
                id = 0,
                name = "",
                countryCode = "",
                countryName = "",
                latency = null,
                isSelected = false,
                lastConnected = null,
                subscriptionId = null,
                groupId = null,
                createdAt = 0,
                updatedAt = 0
            ),
            routingMode
        )
    }
}
