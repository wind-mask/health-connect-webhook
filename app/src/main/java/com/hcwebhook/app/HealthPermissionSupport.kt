// Feature availability is an experimental Health Connect API; only the FEATURE_* constants
// are used here, which both the production code and unit tests may reference.
@file:OptIn(androidx.health.connect.client.feature.ExperimentalFeatureAvailabilityApi::class)

package com.hcwebhook.app

import androidx.health.connect.client.HealthConnectFeatures
/**
 * Pure helpers for deciding which Health Connect permissions a device can actually grant.
 *
 * Newer permissions (skin temperature, background and history reads) only exist in newer
 * Health Connect builds, and ask for them on a device whose build does not know them fails
 * the whole permission screen — leaving the app with no access at all. These rules drop such
 * permissions from a request instead, so the remaining ones can still be granted.
 *
 * Kept free of Android dependencies (feature availability arrives as a lambda) so it can be
 * unit tested.
 */
internal object HealthPermissionSupport {

    /**
     * Subset of [requestedPermissions] that cannot be granted on this device.
     *
     * [isFeatureAvailable] is only consulted for permissions that are actually requested, and
     * receives one of the `HealthConnectFeatures.FEATURE_*` constants.
     */
    fun unsupportedPermissions(
        requestedPermissions: Set<String>,
        isFeatureAvailable: (Int) -> Boolean,
        skinTemperaturePermission: String,
        backgroundPermission: String = HealthConnectManager.BACKGROUND_PERMISSION_STR,
        historyPermission: String = HealthConnectManager.HISTORY_PERMISSION_STR,
    ): Set<String> = buildSet {
        if (backgroundPermission in requestedPermissions &&
            !isFeatureAvailable(HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_IN_BACKGROUND)
        ) {
            add(backgroundPermission)
        }
        if (historyPermission in requestedPermissions &&
            !isFeatureAvailable(HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_HISTORY)
        ) {
            add(historyPermission)
        }
        if (skinTemperaturePermission in requestedPermissions &&
            !isFeatureAvailable(HealthConnectFeatures.FEATURE_SKIN_TEMPERATURE)
        ) {
            add(skinTemperaturePermission)
        }
    }

}
