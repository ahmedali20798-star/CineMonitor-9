package com.canon.multicampro.domain.repository

import com.canon.multicampro.domain.model.CameraProtocol
import com.canon.multicampro.domain.model.CameraSlot
import com.canon.multicampro.domain.model.TouchAfPoint

interface CameraControlRepository {
    suspend fun connectCamera(
        slotId: String,
        ipAddress: String,
        protocol: CameraProtocol,
        username: String = "admin",
        password: String = ""
    ): Result<String>

    suspend fun triggerShutter(slot: CameraSlot): Result<Unit>

    suspend fun setRecording(slot: CameraSlot, record: Boolean): Result<Unit>

    suspend fun sendTouchAf(slot: CameraSlot, point: TouchAfPoint): Result<Unit>

    suspend fun updateExposureParameter(
        slot: CameraSlot,
        parameter: String,
        value: String
    ): Result<Unit>

    suspend fun releaseSessionSafely(slotId: String)
}
