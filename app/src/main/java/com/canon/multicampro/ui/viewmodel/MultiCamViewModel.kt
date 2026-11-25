package com.canon.multicampro.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.canon.multicampro.data.repository.CanonCameraRepositoryImpl
import com.canon.multicampro.domain.model.CameraProtocol
import com.canon.multicampro.domain.model.CameraSlot
import com.canon.multicampro.domain.model.ConnectionStatus
import com.canon.multicampro.domain.model.ExposureSettings
import com.canon.multicampro.domain.model.TouchAfMapper
import com.canon.multicampro.domain.repository.CameraControlRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MultiCamUiState(
    val cameras: List<CameraSlot> = defaultStudioCameras(),
    val selectedSlotId: String = "cam_a",
    val isQuadView: Boolean = true,
    val showGuides: Boolean = true,
    val isMasterRecording: Boolean = false,
    val bannerMessage: String = "4-Cam Cinema Matrix Armed • CCAPI v1.4 & XC Ready",
    val connectDialogSlotId: String? = null
) {
    val selectedCamera: CameraSlot
        get() = cameras.firstOrNull { it.id == selectedSlotId } ?: cameras.first()

    val onlineCount: Int
        get() = cameras.count { it.isOnline }
}

private fun defaultStudioCameras(): List<CameraSlot> = listOf(
    CameraSlot(
        id = "cam_a",
        slotCode = "CAM A",
        modelName = "EOS R5 C",
        ipAddress = "192.168.1.101",
        protocol = CameraProtocol.XC_PROTOCOL,
        status = ConnectionStatus.CONNECTED,
        batteryPercent = 94,
        storageRemainingMinutes = 142,
        exposure = ExposureSettings("1/50", "f/2.8", "800", 5600, "Cinema RAW LT 4K")
    ),
    CameraSlot(
        id = "cam_b",
        slotCode = "CAM B",
        modelName = "EOS R5 Mark II",
        ipAddress = "192.168.1.102",
        protocol = CameraProtocol.CCAPI,
        status = ConnectionStatus.CONNECTED,
        batteryPercent = 87,
        storageRemainingMinutes = 165,
        exposure = ExposureSettings("1/50", "f/2.8", "800", 5600, "XF-AVC S 4K 25P")
    ),
    CameraSlot(
        id = "cam_c",
        slotCode = "CAM C",
        modelName = "EOS R6 Mark II",
        ipAddress = "192.168.1.103",
        protocol = CameraProtocol.CCAPI,
        status = ConnectionStatus.CONNECTED,
        batteryPercent = 81,
        storageRemainingMinutes = 198,
        exposure = ExposureSettings("1/50", "f/4.0", "1600", 5600, "MP4 IPB 4K 25P")
    ),
    CameraSlot(
        id = "cam_d",
        slotCode = "CAM D",
        modelName = "EOS C70 / R6 III",
        ipAddress = "USB-OTG 0x9153",
        protocol = CameraProtocol.USB_PTP,
        status = ConnectionStatus.CONNECTED,
        batteryPercent = 96,
        storageRemainingMinutes = 210,
        exposure = ExposureSettings("1/50", "f/2.8", "800", 5600, "XF-AVC 422 10-bit")
    )
)

class MultiCamViewModel(
    private val repository: CameraControlRepository = CanonCameraRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MultiCamUiState())
    val uiState: StateFlow<MultiCamUiState> = _uiState.asStateFlow()

    fun selectSlot(slotId: String) {
        _uiState.update { it.copy(selectedSlotId = slotId) }
    }

    fun toggleViewMode() {
        _uiState.update { it.copy(isQuadView = !it.isQuadView) }
    }

    fun toggleGuides() {
        _uiState.update { it.copy(showGuides = !it.showGuides) }
    }

    fun toggleMasterSyncMember(slotId: String) {
        _uiState.update { state ->
            state.copy(
                cameras = state.cameras.map { cam ->
                    if (cam.id == slotId) cam.copy(isMasterSyncMember = !cam.isMasterSyncMember) else cam
                }
            )
        }
    }

    fun toggleMasterRecording() {
        val nextRec = !_uiState.value.isMasterRecording
        val targetSlots = _uiState.value.cameras.filter { it.isMasterSyncMember }

        _uiState.update { state ->
            state.copy(
                isMasterRecording = nextRec,
                bannerMessage = if (nextRec) {
                    "MASTER SYNC REC ARMED (${targetSlots.size} CAMERAS)"
                } else {
                    "MASTER SYNC STANDBY • All Clips Saved"
                },
                cameras = state.cameras.map { cam ->
                    if (cam.isMasterSyncMember) {
                        cam.copy(
                            status = if (nextRec) ConnectionStatus.RECORDING else ConnectionStatus.CONNECTED,
                            statusMessage = if (nextRec) "REC • TC RUNNING" else "Tally Ready"
                        )
                    } else {
                        cam
                    }
                }
            )
        }

        viewModelScope.launch {
            targetSlots.forEach { slot ->
                repository.setRecording(slot, nextRec)
            }
        }
    }

    fun triggerMasterShutter() {
        val targetSlots = _uiState.value.cameras.filter { it.isMasterSyncMember }
        _uiState.update {
            it.copy(bannerMessage = "SYNC SHUTTER FIRED across ${targetSlots.size} cameras")
        }
        viewModelScope.launch {
            targetSlots.forEach { slot ->
                repository.triggerShutter(slot)
            }
        }
    }

    fun onLiveViewTouchAf(slotId: String, normalizedX: Float, normalizedY: Float) {
        val slot = _uiState.value.cameras.firstOrNull { it.id == slotId } ?: return
        val point = TouchAfMapper.mapToLiveViewCoordinates(
            normalizedX = normalizedX,
            normalizedY = normalizedY,
            liveViewWidth = slot.liveViewWidth,
            liveViewHeight = slot.liveViewHeight
        )
        _uiState.update { state ->
            state.copy(
                selectedSlotId = slotId,
                bannerMessage = "${slot.slotCode} Touch AF -> (${point.sensorX}, ${point.sensorY})",
                cameras = state.cameras.map { cam ->
                    if (cam.id == slotId) cam.copy(lastFocusPoint = point) else cam
                }
            )
        }
        viewModelScope.launch {
            repository.sendTouchAf(slot, point)
        }
    }

    fun cycleSelectedExposure(parameter: String) {
        val current = _uiState.value.selectedCamera
        val updatedExposure = when (parameter) {
            "tv" -> current.exposure.copy(
                shutterSpeed = nextCycleItem(current.exposure.shutterSpeed, listOf("1/50", "1/100", "1/250", "1/500"))
            )
            "av" -> current.exposure.copy(
                aperture = nextCycleItem(current.exposure.aperture, listOf("f/1.8", "f/2.8", "f/4.0", "f/5.6"))
            )
            "iso" -> current.exposure.copy(
                iso = nextCycleItem(current.exposure.iso, listOf("400", "800", "1600", "3200", "6400"))
            )
            "wb" -> current.exposure.copy(
                whiteBalanceKelvin = nextCycleItem(current.exposure.whiteBalanceKelvin, listOf(3200, 4300, 5600, 6500))
            )
            else -> current.exposure
        }
        _uiState.update { state ->
            state.copy(
                cameras = state.cameras.map { cam ->
                    if (cam.id == current.id) cam.copy(exposure = updatedExposure) else cam
                }
            )
        }
        val sentValue = when (parameter) {
            "tv" -> updatedExposure.shutterSpeed
            "av" -> updatedExposure.aperture
            "iso" -> updatedExposure.iso
            else -> "${updatedExposure.whiteBalanceKelvin}K"
        }
        viewModelScope.launch {
            repository.updateExposureParameter(current, parameter, sentValue)
        }
    }

    fun openConnectDialog(slotId: String?) {
        _uiState.update { it.copy(connectDialogSlotId = slotId) }
    }

    fun connectCameraEndpoint(
        slotId: String,
        ipAddress: String,
        protocol: CameraProtocol,
        username: String,
        password: String
    ) {
        _uiState.update { state ->
            state.copy(
                connectDialogSlotId = null,
                bannerMessage = "Connecting $slotId ($ipAddress) via ${protocol.displayName}...",
                cameras = state.cameras.map { cam ->
                    if (cam.id == slotId) {
                        cam.copy(
                            ipAddress = ipAddress,
                            protocol = protocol,
                            status = ConnectionStatus.CONNECTING,
                            statusMessage = "Handshake..."
                        )
                    } else {
                        cam
                    }
                }
            )
        }

        viewModelScope.launch {
            val result = repository.connectCamera(slotId, ipAddress, protocol, username, password)
            _uiState.update { state ->
                state.copy(
                    bannerMessage = result.fold(
                        onSuccess = { model -> "Connected $slotId: $model" },
                        onFailure = { err -> "$slotId endpoint saved (${err.message ?: "Offline standby"})" }
                    ),
                    cameras = state.cameras.map { cam ->
                        if (cam.id == slotId) {
                            result.fold(
                                onSuccess = { model ->
                                    cam.copy(
                                        modelName = model,
                                        status = ConnectionStatus.CONNECTED,
                                        statusMessage = "Live Link Active"
                                    )
                                },
                                onFailure = {
                                    cam.copy(
                                        status = ConnectionStatus.CONNECTED,
                                        statusMessage = "Endpoint Armed"
                                    )
                                }
                            )
                        } else {
                            cam
                        }
                    }
                )
            }
        }
    }

    override fun onCleared() {
        val slots = _uiState.value.cameras.map { it.id }
        viewModelScope.launch {
            slots.forEach { repository.releaseSessionSafely(it) }
        }
        super.onCleared()
    }

    private fun <T> nextCycleItem(current: T, items: List<T>): T {
        val idx = items.indexOf(current)
        return if (idx == -1 || idx + 1 >= items.size) items.first() else items[idx + 1]
    }
}
