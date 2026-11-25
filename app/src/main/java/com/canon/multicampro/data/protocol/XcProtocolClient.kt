package com.canon.multicampro.data.protocol

import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Canon XC Control Protocol (/-wvhttp-01-/) client for EOS R5 C Cinema Mode.
 * Manages session IDs from open.cgi and guarantees safe session release via NonCancellable.
 */
class XcProtocolClient(
    private val httpClient: OkHttpClient
) {
    private val activeSessions = ConcurrentHashMap<String, String>()

    suspend fun openSession(slotId: String, baseUrl: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val url = "${baseUrl.trimEnd('/')}/-wvhttp-01-/open.cgi"
            val request = Request.Builder().url(url).get().build()
            httpClient.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "XC open.cgi HTTP ${response.code}" }
                val body = response.body?.string().orEmpty()
                val sessionId = body.lines()
                    .firstOrNull { it.startsWith("s:=") }
                    ?.removePrefix("s:=")
                    ?.trim()
                    ?: throw IllegalStateException("Missing XC session ID in response")
                activeSessions[slotId] = sessionId
                sessionId
            }
        }
    }

    suspend fun setRecording(slotId: String, baseUrl: String, start: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val sessionId = activeSessions[slotId]
                    ?: openSession(slotId, baseUrl).getOrThrow()
                val cmd = if (start) "start" else "stop"
                val url = "${baseUrl.trimEnd('/')}/-wvhttp-01-/control.cgi?s=$sessionId&f.rec=$cmd"
                val request = Request.Builder().url(url).get().build()
                httpClient.newCall(request).execute().use { response ->
                    check(response.isSuccessful) { "XC control.cgi HTTP ${response.code}" }
                }
            }
        }

    suspend fun closeSessionSafely(slotId: String, baseUrl: String) {
        val sessionId = activeSessions.remove(slotId) ?: return
        withContext(NonCancellable + Dispatchers.IO) {
            runCatching {
                val url = "${baseUrl.trimEnd('/')}/-wvhttp-01-/close.cgi?s=$sessionId"
                val request = Request.Builder().url(url).get().build()
                httpClient.newCall(request).execute().close()
            }
        }
    }
}
