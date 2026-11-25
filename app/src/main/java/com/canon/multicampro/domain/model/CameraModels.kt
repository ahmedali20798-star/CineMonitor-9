package com.canon.multicampro.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class CameraProtocol(val displayName: String, val endpointHint: String) {
    CCAPI("CCAPI v1.4", "/ccapi/ver100"),
    XC_PROTOCOL("XC Protocol", "/-wvhttp-01-/"),
    USB_PTP("USB PTP", "USB-OTG 0x9153")
}

@Serializable
enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECORDING,
    ERROR
}

@Serializable
data class ExposureSettings(
    val shutterSpeed: String = "1/50",
    val aperture: String = "f/2.8",
    val iso: String = "800",
    val whiteBalanceKelvin: Int = 5600,
    val codecLabel: String = "XF-AVC 4K 25P"
)

@Serializable
data class TouchAfPoint(
    val normalizedX: Float,
    val normalizedY: Float,
    val sensorX: Int,
    val sensorY: Int
)

@Serializable
data class CameraSlot(
    val id: String,
    val slotCode: String,
    val modelName: String,
    val ipAddress: String,
    val protocol: CameraProtocol,
    val status: ConnectionStatus = ConnectionStatus.CONNECTED,
    val batteryPercent: Int = 88,
    val storageRemainingMinutes: Int = 114,
    val isMasterSyncMember: Boolean = true,
    val exposure: ExposureSettings = ExposureSettings(),
    val liveViewWidth: Int = 1920,
    val liveViewHeight: Int = 1080,
    val lastFocusPoint: TouchAfPoint? = null,
    val statusMessage: String = "Tally Ready"
) {
    val isRecording: Boolean
        get() = status == ConnectionStatus.RECORDING

    val isOnline: Boolean
        get() = status == ConnectionStatus.CONNECTED || status == ConnectionStatus.RECORDING
}

object TouchAfMapper {
    fun mapToLiveViewCoordinates(
        normalizedX: Float,
        normalizedY: Float,
        liveViewWidth: Int,
        liveViewHeight: Int
    ): TouchAfPoint {
        val clampedX = normalizedX.coerceIn(0f, 1f)
        val clampedY = normalizedY.coerceIn(0f, 1f)
        val safeW = liveViewWidth.coerceAtLeast(1)
        val safeH = liveViewHeight.coerceAtLeast(1)
        return TouchAfPoint(
            normalizedX = clampedX,
            normalizedY = clampedY,
            sensorX = (clampedX * safeW).toInt(),
            sensorY = (clampedY * safeH).toInt()
        )
    }
}
