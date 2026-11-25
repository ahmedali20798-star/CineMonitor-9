package com.canon.multicampro.data.repository

import com.canon.multicampro.data.network.DigestAuthAuthenticator
import com.canon.multicampro.data.protocol.CcapiClient
import com.canon.multicampro.data.protocol.XcProtocolClient
import com.canon.multicampro.domain.model.CameraProtocol
import com.canon.multicampro.domain.model.CameraSlot
import com.canon.multicampro.domain.model.TouchAfPoint
import com.canon.multicampro.domain.repository.CameraControlRepository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

class CanonCameraRepositoryImpl : CameraControlRepository {

    private val slotClients = ConcurrentHashMap<String, OkHttpClient>()
    private val slotBaseUrls = ConcurrentHashMap<String, String>()

    override suspend fun connectCamera(
        slotId: String,
        ipAddress: String,
        protocol: CameraProtocol,
        username: String,
        password: String
    ): Result<String> {
        val normalizedUrl = normalizeBaseUrl(ipAddress)
        val client = buildClient(username, password)
        slotClients[slotId] = client
        slotBaseUrls[slotId] = normalizedUrl

        return when (protocol) {
            CameraProtocol.CCAPI -> CcapiClient(client).fetchDeviceModel(normalizedUrl)
            CameraProtocol.XC_PROTOCOL -> XcProtocolClient(client).openSession(slotId, normalizedUrl)
                .map { "EOS R5 C (XC Session $it)" }
            CameraProtocol.USB_PTP -> Result.success("Canon USB PTP Direct")
        }
    }

    override suspend fun triggerShutter(slot: CameraSlot): Result<Unit> {
        val (client, baseUrl) = getClientAndUrl(slot)
        return when (slot.protocol) {
            CameraProtocol.CCAPI -> CcapiClient(client).triggerManualShutter(baseUrl)
            CameraProtocol.XC_PROTOCOL -> XcProtocolClient(client).setRecording(slot.id, baseUrl, !slot.isRecording)
            CameraProtocol.USB_PTP -> Result.success(Unit)
        }
    }

    override suspend fun setRecording(slot: CameraSlot, record: Boolean): Result<Unit> {
        val (client, baseUrl) = getClientAndUrl(slot)
        return when (slot.protocol) {
            CameraProtocol.CCAPI -> CcapiClient(client).setRecording(baseUrl, record)
            CameraProtocol.XC_PROTOCOL -> XcProtocolClient(client).setRecording(slot.id, baseUrl, record)
            CameraProtocol.USB_PTP -> Result.success(Unit)
        }
    }

    override suspend fun sendTouchAf(slot: CameraSlot, point: TouchAfPoint): Result<Unit> {
        val (client, baseUrl) = getClientAndUrl(slot)
        return when (slot.protocol) {
            CameraProtocol.CCAPI -> CcapiClient(client).sendTouchAf(baseUrl, point)
            CameraProtocol.XC_PROTOCOL, CameraProtocol.USB_PTP -> Result.success(Unit)
        }
    }

    override suspend fun updateExposureParameter(
        slot: CameraSlot,
        parameter: String,
        value: String
    ): Result<Unit> {
        val (client, baseUrl) = getClientAndUrl(slot)
        return when (slot.protocol) {
            CameraProtocol.CCAPI -> CcapiClient(client).setSettingValue(baseUrl, parameter, value)
            CameraProtocol.XC_PROTOCOL, CameraProtocol.USB_PTP -> Result.success(Unit)
        }
    }

    override suspend fun releaseSessionSafely(slotId: String) {
        val client = slotClients.remove(slotId) ?: return
        val baseUrl = slotBaseUrls.remove(slotId) ?: return
        XcProtocolClient(client).closeSessionSafely(slotId, baseUrl)
    }

    private fun getClientAndUrl(slot: CameraSlot): Pair<OkHttpClient, String> {
        val client = slotClients.getOrPut(slot.id) { buildClient("admin", "") }
        val baseUrl = slotBaseUrls.getOrPut(slot.id) { normalizeBaseUrl(slot.ipAddress) }
        return client to baseUrl
    }

    private fun buildClient(username: String, password: String): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .writeTimeout(4, TimeUnit.SECONDS)
            .authenticator(DigestAuthAuthenticator(username, password))
            .build()
    }

    private fun normalizeBaseUrl(raw: String): String {
        val trimmed = raw.trim().trimEnd('/')
        return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "http://$trimmed:8080"
        }
    }
}
