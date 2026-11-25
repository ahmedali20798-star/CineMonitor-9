package com.canon.multicampro.data.protocol

import com.canon.multicampro.domain.model.TouchAfPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class CcapiClient(
    private val httpClient: OkHttpClient
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun fetchDeviceModel(baseUrl: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("${baseUrl.trimEnd('/')}/ccapi/ver100/deviceinformation")
                .get()
                .build()
            httpClient.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "CCAPI HTTP ${response.code}" }
                val body = response.body?.string().orEmpty()
                val root = json.parseToJsonElement(body).jsonObject
                root["productname"]?.jsonPrimitive?.content ?: "Canon EOS CCAPI"
            }
        }
    }

    suspend fun triggerManualShutter(baseUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val endpoint = "${baseUrl.trimEnd('/')}/ccapi/ver100/shooting/control/shutterbutton/manual"
            postJson(endpoint, """{"action":"full_press","af":true}""")
            delay(SHUTTER_HOLD_MS)
            postJson(endpoint, """{"action":"release","af":false}""")
        }
    }

    suspend fun setRecording(baseUrl: String, start: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val action = if (start) "start" else "stop"
            val endpoint = "${baseUrl.trimEnd('/')}/ccapi/ver100/shooting/control/recbutton"
            postJson(endpoint, """{"action":"$action"}""")
        }
    }

    suspend fun sendTouchAf(baseUrl: String, point: TouchAfPoint): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val endpoint = "${baseUrl.trimEnd('/')}/ccapi/ver100/shooting/liveview/af"
            postJson(endpoint, """{"positionx":${point.sensorX},"positiony":${point.sensorY}}""")
        }
    }

    suspend fun setSettingValue(
        baseUrl: String,
        settingPath: String,
        value: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val endpoint = "${baseUrl.trimEnd('/')}/ccapi/ver100/shooting/settings/$settingPath"
            val payload = """{"value":"$value"}"""
            val request = Request.Builder()
                .url(endpoint)
                .put(payload.toRequestBody(jsonMediaType))
                .build()
            httpClient.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "CCAPI PUT $settingPath failed: ${response.code}" }
            }
        }
    }

    private fun postJson(url: String, payload: String) {
        val request = Request.Builder()
            .url(url)
            .post(payload.toRequestBody(jsonMediaType))
            .build()
        httpClient.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "CCAPI POST failed: ${response.code}" }
        }
    }

    companion object {
        private const val SHUTTER_HOLD_MS = 120L
    }
}
