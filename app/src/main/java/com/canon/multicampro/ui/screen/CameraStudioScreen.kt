package com.canon.multicampro.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.canon.multicampro.domain.model.CameraProtocol
import com.canon.multicampro.domain.model.CameraSlot
import com.canon.multicampro.ui.components.StudioBrandBadge
import com.canon.multicampro.ui.theme.CanonCrimson
import com.canon.multicampro.ui.theme.StudioAmber
import com.canon.multicampro.ui.theme.StudioBorder
import com.canon.multicampro.ui.theme.StudioEmerald
import com.canon.multicampro.ui.theme.StudioIvory
import com.canon.multicampro.ui.theme.StudioObsidian
import com.canon.multicampro.ui.theme.StudioSlate
import com.canon.multicampro.ui.theme.StudioSurface
import com.canon.multicampro.ui.theme.StudioSurfaceElevated
import com.canon.multicampro.ui.theme.TelemetryCyan
import com.canon.multicampro.ui.viewmodel.MultiCamViewModel

@Composable
fun CameraStudioScreen(
    viewModel: MultiCamViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioObsidian)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StudioTopHeader(
            isAnyRecording = state.isMasterRecording,
            onlineCount = state.onlineCount,
            totalCount = state.cameras.size,
            bannerMessage = state.bannerMessage,
            isQuadView = state.isQuadView,
            showGuides = state.showGuides,
            onToggleViewMode = viewModel::toggleViewMode,
            onToggleGuides = viewModel::toggleGuides
        )

        Box(modifier = Modifier.weight(1f)) {
            if (state.isQuadView) {
                QuadMonitorGrid(
                    cameras = state.cameras,
                    selectedSlotId = state.selectedSlotId,
                    showGuides = state.showGuides,
                    onSelectSlot = viewModel::selectSlot,
                    onToggleSyncMember = viewModel::toggleMasterSyncMember,
                    onOpenConnectDialog = viewModel::openConnectDialog,
                    onTouchAf = viewModel::onLiveViewTouchAf
                )
            } else {
                CameraFeedCard(
                    slot = state.selectedCamera,
                    isSelected = true,
                    showGuides = state.showGuides,
                    onSelect = { },
                    onToggleSyncMember = { viewModel.toggleMasterSyncMember(state.selectedCamera.id) },
                    onOpenConnect = { viewModel.openConnectDialog(state.selectedCamera.id) },
                    onTouchAf = { nx, ny -> viewModel.onLiveViewTouchAf(state.selectedCamera.id, nx, ny) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        StudioBottomDeck(
            selectedCamera = state.selectedCamera,
            isMasterRecording = state.isMasterRecording,
            onCycleExposure = viewModel::cycleSelectedExposure,
            onTriggerMasterShutter = viewModel::triggerMasterShutter,
            onToggleMasterRecording = viewModel::toggleMasterRecording
        )
    }

    val dialogSlotId = state.connectDialogSlotId
    if (dialogSlotId != null) {
        val targetSlot = state.cameras.firstOrNull { it.id == dialogSlotId }
        if (targetSlot != null) {
            CameraEndpointDialog(
                slot = targetSlot,
                onDismiss = { viewModel.openConnectDialog(null) },
                onConfirm = { ip, protocol, user, pass ->
                    viewModel.connectCameraEndpoint(targetSlot.id, ip, protocol, user, pass)
                }
            )
        }
    }
}

@Composable
private fun StudioTopHeader(
    isAnyRecording: Boolean,
    onlineCount: Int,
    totalCount: Int,
    bannerMessage: String,
    isQuadView: Boolean,
    showGuides: Boolean,
    onToggleViewMode: () -> Unit,
    onToggleGuides: () -> Unit
) {
    Surface(
        color = StudioSurface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, StudioBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StudioBrandBadge(isAnyRecording = isAnyRecording)

            Row(
                modifier = Modifier
                    .background(StudioSurfaceElevated, RoundedCornerShape(8.dp))
                    .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isAnyRecording) CanonCrimson else StudioEmerald)
                )
                Text(
                    text = "$onlineCount/$totalCount LINKED",
                    style = MaterialTheme.typography.labelMedium,
                    color = StudioIvory
                )
                Text(
                    text = "|",
                    style = MaterialTheme.typography.labelMedium,
                    color = StudioBorder
                )
                Text(
                    text = bannerMessage,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isAnyRecording) CanonCrimson else TelemetryCyan
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(onClick = onToggleGuides) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = "Toggle Framing Guides",
                        tint = if (showGuides) TelemetryCyan else StudioSlate
                    )
                }
                OutlinedButton(
                    onClick = onToggleViewMode,
                    border = BorderStroke(1.dp, StudioBorder),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Switch View",
                        tint = TelemetryCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isQuadView) "SOLO VIEW" else "QUAD 2x2",
                        style = MaterialTheme.typography.labelMedium,
                        color = StudioIvory
                    )
                }
            }
        }
    }
}

@Composable
private fun QuadMonitorGrid(
    cameras: List<CameraSlot>,
    selectedSlotId: String,
    showGuides: Boolean,
    onSelectSlot: (String) -> Unit,
    onToggleSyncMember: (String) -> Unit,
    onOpenConnectDialog: (String) -> Unit,
    onTouchAf: (String, Float, Float) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val rows = cameras.chunked(2)
        rows.forEach { rowCameras ->
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowCameras.forEach { slot ->
                    CameraFeedCard(
                        slot = slot,
                        isSelected = slot.id == selectedSlotId,
                        showGuides = showGuides,
                        onSelect = { onSelectSlot(slot.id) },
                        onToggleSyncMember = { onToggleSyncMember(slot.id) },
                        onOpenConnect = { onOpenConnectDialog(slot.id) },
                        onTouchAf = { nx, ny -> onTouchAf(slot.id, nx, ny) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraFeedCard(
    slot: CameraSlot,
    isSelected: Boolean,
    showGuides: Boolean,
    onSelect: () -> Unit,
    onToggleSyncMember: () -> Unit,
    onOpenConnect: () -> Unit,
    onTouchAf: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        slot.isRecording -> CanonCrimson
        isSelected -> TelemetryCyan
        else -> StudioBorder
    }

    Surface(
        color = StudioSurface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(if (slot.isRecording || isSelected) 2.dp else 1.dp, borderColor),
        modifier = modifier.clickable { onSelect() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0E131D), Color(0xFF080A0E))
                    )
                )
                .pointerInput(slot.id) {
                    detectTapGestures { offset ->
                        val nx = if (size.width > 0) offset.x / size.width.toFloat() else 0.5f
                        val ny = if (size.height > 0) offset.y / size.height.toFloat() else 0.5f
                        onTouchAf(nx, ny)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (showGuides) {
                    val guideColor = Color.White.copy(alpha = 0.08f)
                    drawLine(guideColor, Offset(size.width / 3f, 0f), Offset(size.width / 3f, size.height), 1f)
                    drawLine(guideColor, Offset(size.width * 2f / 3f, 0f), Offset(size.width * 2f / 3f, size.height), 1f)
                    drawLine(guideColor, Offset(0f, size.height / 3f), Offset(size.width, size.height / 3f), 1f)
                    drawLine(guideColor, Offset(0f, size.height * 2f / 3f), Offset(size.width, size.height * 2f / 3f), 1f)
                }

                val af = slot.lastFocusPoint
                if (af != null) {
                    val cx = af.normalizedX * size.width
                    val cy = af.normalizedY * size.height
                    val boxHalf = 22.dp.toPx()
                    drawRect(
                        color = TelemetryCyan,
                        topLeft = Offset(cx - boxHalf, cy - boxHalf),
                        size = androidx.compose.ui.geometry.Size(boxHalf * 2f, boxHalf * 2f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (slot.isRecording) CanonCrimson else StudioSurfaceElevated,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = slot.slotCode,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = StudioIvory
                        )
                    }
                    Text(
                        text = slot.modelName,
                        style = MaterialTheme.typography.titleMedium,
                        color = StudioIvory
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ProtocolBadge(protocol = slot.protocol)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (slot.isMasterSyncMember) TelemetryCyan.copy(alpha = 0.16f) else StudioSurfaceElevated)
                            .border(
                                1.dp,
                                if (slot.isMasterSyncMember) TelemetryCyan else StudioBorder,
                                RoundedCornerShape(4.dp)
                            )
                            .clickable { onToggleSyncMember() }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (slot.isMasterSyncMember) "SYNC IN" else "SOLO",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (slot.isMasterSyncMember) TelemetryCyan else StudioSlate
                        )
                    }
                    IconButton(
                        onClick = onOpenConnect,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Configure Camera IP",
                            tint = StudioSlate,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.SettingsInputAntenna,
                    contentDescription = null,
                    tint = if (slot.isRecording) CanonCrimson else TelemetryCyan.copy(alpha = 0.65f),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${slot.ipAddress} • ${slot.exposure.codecLabel}",
                    style = MaterialTheme.typography.labelMedium,
                    color = StudioSlate
                )
                Text(
                    text = "Tap anywhere on frame for Touch AF",
                    style = MaterialTheme.typography.labelSmall,
                    color = StudioSlate.copy(alpha = 0.7f)
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${slot.exposure.shutterSpeed}  |  ${slot.exposure.aperture}  |  ISO ${slot.exposure.iso}  |  ${slot.exposure.whiteBalanceKelvin}K",
                    style = MaterialTheme.typography.labelMedium,
                    color = StudioIvory
                )
                Text(
                    text = "BAT ${slot.batteryPercent}% • ${slot.storageRemainingMinutes}m",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (slot.isRecording) CanonCrimson else StudioEmerald
                )
            }
        }
    }
}

@Composable
private fun ProtocolBadge(protocol: CameraProtocol) {
    val accent = when (protocol) {
        CameraProtocol.CCAPI -> TelemetryCyan
        CameraProtocol.XC_PROTOCOL -> StudioAmber
        CameraProtocol.USB_PTP -> StudioEmerald
    }
    Box(
        modifier = Modifier
            .background(accent.copy(alpha = 0.14f), RoundedCornerShape(4.dp))
            .border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = protocol.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = accent
        )
    }
}

@Composable
private fun StudioBottomDeck(
    selectedCamera: CameraSlot,
    isMasterRecording: Boolean,
    onCycleExposure: (String) -> Unit,
    onTriggerMasterShutter: () -> Unit,
    onToggleMasterRecording: () -> Unit
) {
    Surface(
        color = StudioSurface,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, StudioBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${selectedCamera.slotCode} CTRL:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TelemetryCyan
                )
                ExposureChip("TV ${selectedCamera.exposure.shutterSpeed}") { onCycleExposure("tv") }
                ExposureChip("AV ${selectedCamera.exposure.aperture}") { onCycleExposure("av") }
                ExposureChip("ISO ${selectedCamera.exposure.iso}") { onCycleExposure("iso") }
                ExposureChip("WB ${selectedCamera.exposure.whiteBalanceKelvin}K") { onCycleExposure("wb") }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onTriggerMasterShutter,
                    border = BorderStroke(1.dp, TelemetryCyan),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Master Shutter",
                        tint = TelemetryCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SYNC SHUTTER",
                        style = MaterialTheme.typography.labelMedium,
                        color = StudioIvory
                    )
                }

                Button(
                    onClick = onToggleMasterRecording,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMasterRecording) StudioSurfaceElevated else CanonCrimson,
                        contentColor = StudioIvory
                    ),
                    border = if (isMasterRecording) BorderStroke(1.dp, CanonCrimson) else null,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = if (isMasterRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                        contentDescription = "Master Record",
                        tint = if (isMasterRecording) CanonCrimson else StudioIvory,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMasterRecording) "STOP ALL REC" else "SYNC REC ALL",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExposureChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(StudioSurfaceElevated)
            .border(1.dp, StudioBorder, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = StudioIvory
        )
    }
}

@Composable
private fun CameraEndpointDialog(
    slot: CameraSlot,
    onDismiss: () -> Unit,
    onConfirm: (String, CameraProtocol, String, String) -> Unit
) {
    var ip by remember { mutableStateOf(slot.ipAddress) }
    var protocol by remember { mutableStateOf(slot.protocol) }
    var username by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioSurface,
        title = {
            Text(
                text = "Configure ${slot.slotCode} (${slot.modelName})",
                style = MaterialTheme.typography.titleMedium,
                color = StudioIvory
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = ip,
                    onValueChange = { ip = it },
                    label = { Text("Camera IP / Endpoint") },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CameraProtocol.entries.forEach { entry ->
                        FilterChip(
                            selected = protocol == entry,
                            onClick = { protocol = entry },
                            label = { Text(entry.displayName) }
                        )
                    }
                }
                if (protocol == CameraProtocol.XC_PROTOCOL) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("XC Digest Username") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("XC Digest Password") },
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(ip, protocol, username, password) },
                colors = ButtonDefaults.buttonColors(containerColor = TelemetryCyan, contentColor = StudioObsidian)
            ) {
                Text("Connect")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = StudioSlate)
            }
        }
    )
}
