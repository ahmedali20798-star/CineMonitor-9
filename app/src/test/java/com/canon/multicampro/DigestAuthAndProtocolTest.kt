package com.canon.multicampro

import com.canon.multicampro.data.network.DigestAuthAuthenticator
import com.canon.multicampro.domain.model.TouchAfMapper
import com.canon.multicampro.ui.viewmodel.MultiCamViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DigestAuthAndProtocolTest {

    @Test
    fun `rfc2617 digest header includes query string in uri`() {
        val header = DigestAuthAuthenticator.buildAuthorizationHeader(
            username = "admin",
            password = "secret",
            realm = "CanonXC",
            nonce = "dcd98b7102dd2f0e8b11d0f600bfb0c093",
            method = "GET",
            digestUri = "/-wvhttp-01-/control.cgi?s=1001&f.rec=start",
            qop = "auth",
            nc = "00000001",
            cnonce = "0a4f113b",
            opaque = "5ccc069c403ebaf9f0171e9517f40e41"
        )

        assertTrue(header.contains("""uri="/-wvhttp-01-/control.cgi?s=1001&f.rec=start""""))
        assertTrue(header.contains("qop=auth"))
        assertTrue(header.contains("nc=00000001"))
    }

    @Test
    fun `touch af mapper clamps and scales normalized coordinates to sensor bounds`() {
        val point = TouchAfMapper.mapToLiveViewCoordinates(
            normalizedX = 0.5f,
            normalizedY = 0.25f,
            liveViewWidth = 1920,
            liveViewHeight = 1080
        )

        assertEquals(960, point.sensorX)
        assertEquals(270, point.sensorY)
    }

    @Test
    fun `viewmodel initializes 4 cameras and toggles master recording sync`() {
        val vm = MultiCamViewModel()
        assertEquals(4, vm.uiState.value.cameras.size)
        assertFalse(vm.uiState.value.isMasterRecording)

        vm.toggleMasterRecording()
        assertTrue(vm.uiState.value.isMasterRecording)
        assertTrue(vm.uiState.value.cameras.all { it.isRecording })
    }
}
