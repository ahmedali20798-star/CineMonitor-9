package com.canon.multicampro.data.network

import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * RFC 2617 HTTP Digest Authentication tailored for Canon XC Control Protocol (/-wvhttp-01-/).
 * Includes full query string in digest-uri calculation as required by Canon Cinema EOS firmware.
 */
class DigestAuthAuthenticator(
    private val username: String,
    private val password: String
) : Authenticator {

    private val nonceCounter = AtomicInteger(0)

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= MAX_AUTH_RETRIES) return null

        val header = response.headers("WWW-Authenticate")
            .firstOrNull { it.startsWith("Digest", ignoreCase = true) }
            ?: return null

        val params = parseDigestChallenge(header)
        val realm = params["realm"] ?: return null
        val nonce = params["nonce"] ?: return null
        val qop = params["qop"]
        val opaque = params["opaque"]

        val request = response.request
        val encodedPath = request.url.encodedPath
        val encodedQuery = request.url.encodedQuery
        val digestUri = if (!encodedQuery.isNullOrEmpty()) {
            "$encodedPath?$encodedQuery"
        } else {
            encodedPath
        }

        val nc = String.format("%08x", nonceCounter.incrementAndGet())
        val cnonce = UUID.randomUUID().toString().replace("-", "").take(16)

        val authHeader = buildAuthorizationHeader(
            username = username,
            password = password,
            realm = realm,
            nonce = nonce,
            method = request.method,
            digestUri = digestUri,
            qop = qop,
            nc = nc,
            cnonce = cnonce,
            opaque = opaque
        )

        return request.newBuilder()
            .header("Authorization", authHeader)
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    companion object {
        private const val MAX_AUTH_RETRIES = 2

        fun md5Hex(input: String): String {
            val digest = MessageDigest.getInstance("MD5").digest(input.toByteArray(Charsets.UTF_8))
            return digest.joinToString("") { "%02x".format(it) }
        }

        fun buildAuthorizationHeader(
            username: String,
            password: String,
            realm: String,
            nonce: String,
            method: String,
            digestUri: String,
            qop: String?,
            nc: String,
            cnonce: String,
            opaque: String?
        ): String {
            val ha1 = md5Hex("$username:$realm:$password")
            val ha2 = md5Hex("$method:$digestUri")
            val selectedQop = qop?.split(",")?.map { it.trim() }?.firstOrNull { it == "auth" }

            val responseHash = if (selectedQop != null) {
                md5Hex("$ha1:$nonce:$nc:$cnonce:$selectedQop:$ha2")
            } else {
                md5Hex("$ha1:$nonce:$ha2")
            }

            return buildString {
                append("Digest username=\"$username\", ")
                append("realm=\"$realm\", ")
                append("nonce=\"$nonce\", ")
                append("uri=\"$digestUri\", ")
                if (selectedQop != null) {
                    append("qop=$selectedQop, ")
                    append("nc=$nc, ")
                    append("cnonce=\"$cnonce\", ")
                }
                if (!opaque.isNullOrEmpty()) {
                    append("opaque=\"$opaque\", ")
                }
                append("response=\"$responseHash\"")
            }
        }

        fun parseDigestChallenge(header: String): Map<String, String> {
            val content = header.removePrefix("Digest").trim()
            val regex = Regex("(\\w+)=(\"([^\"]*)\"|([^,\\s]*))")
            val result = mutableMapOf<String, String>()
            for (match in regex.findAll(content)) {
                val key = match.groupValues[1]
                val value = match.groupValues[3].ifEmpty { match.groupValues[4] }
                result[key] = value
            }
            return result
        }
    }
}
