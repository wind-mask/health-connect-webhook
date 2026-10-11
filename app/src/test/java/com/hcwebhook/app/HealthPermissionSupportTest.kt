@file:OptIn(androidx.health.connect.client.feature.ExperimentalFeatureAvailabilityApi::class)

package com.hcwebhook.app

import androidx.health.connect.client.HealthConnectFeatures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthPermissionSupportTest {

    private val steps = "android.permission.health.READ_STEPS"
    private val skinTemperature = "android.permission.health.READ_SKIN_TEMPERATURE"
    private val background = HealthConnectManager.BACKGROUND_PERMISSION_STR
    private val history = HealthConnectManager.HISTORY_PERMISSION_STR

    private fun unsupported(
        requested: Set<String>,
        available: Set<Int> = emptySet(),
    ) = HealthPermissionSupport.unsupportedPermissions(
        requestedPermissions = requested,
        isFeatureAvailable = { it in available },
        skinTemperaturePermission = skinTemperature,
    )

    @Test
    fun keepsEverythingWhenFeaturesAreAvailable() {
        val requested = setOf(steps, skinTemperature, background, history)
        val available = setOf(
            HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_IN_BACKGROUND,
            HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_HISTORY,
            HealthConnectFeatures.FEATURE_SKIN_TEMPERATURE,
        )

        assertTrue(unsupported(requested, available).isEmpty())
    }

    @Test
    fun dropsSkinTemperatureWhenFeatureIsUnavailable() {
        assertEquals(
            setOf(skinTemperature),
            unsupported(setOf(steps, skinTemperature)),
        )
    }

    @Test
    fun dropsBackgroundAndHistoryReadsButKeepsRecordPermissions() {
        assertEquals(
            setOf(background, history),
            unsupported(setOf(steps, background, history)),
        )
    }

    @Test
    fun onlyProbesFeaturesForRequestedPermissions() {
        val probed = mutableSetOf<Int>()
        HealthPermissionSupport.unsupportedPermissions(
            requestedPermissions = setOf(steps),
            isFeatureAvailable = { feature ->
                probed += feature
                false
            },
            skinTemperaturePermission = skinTemperature,
        )

        assertTrue("no feature should be probed for plain record permissions", probed.isEmpty())
    }

    @Test
    fun keepsRequestablePermissionsSoPlainRequestsStillGoThrough() {
        // Only the unsupported entries are reported, so callers keep every permission the
        // device can grant instead of losing the whole request.
        assertEquals(
            emptySet<String>(),
            unsupported(setOf(steps), setOf(HealthConnectFeatures.FEATURE_SKIN_TEMPERATURE)),
        )
    }
}
