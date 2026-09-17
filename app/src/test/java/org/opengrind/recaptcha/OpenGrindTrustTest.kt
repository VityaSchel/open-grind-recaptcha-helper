package org.opengrind.recaptcha

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenGrindTrustTest {
    private val release = "2805FDD8F0BADB9424D3244C5E5B3473CEF5B8798EC1117382E89EDA45C3658C"
    private val play = "90F4BB85C1F2FA2A7810A1076A60D20E791720C0EE88FDB88F9E4BA32CA8702E"

    private fun signedBy(vararg certs: String) =
        OpenGrindTrust.SigningCertificates { _, sha256 ->
            certs.any { it.hexToByteArray().contentEquals(sha256) }
        }

    @Test
    fun `trusts the release signer`() {
        assertTrue(OpenGrindTrust.trusts("org.opengrind", signedBy(release)))
    }

    @Test
    fun `trusts the play signer so a store install can mint`() {
        assertTrue(OpenGrindTrust.trusts("org.opengrind", signedBy(play)))
    }

    @Test
    fun `refuses another signer on the right package`() {
        val impostor = "0".repeat(64)
        assertFalse(OpenGrindTrust.trusts("org.opengrind", signedBy(impostor)))
    }

    @Test
    fun `refuses another package even when it holds a trusted certificate`() {
        assertFalse(OpenGrindTrust.trusts("com.grindrapp.android", signedBy(release)))
    }

    @Test
    fun `refuses a caller android could not name`() {
        assertFalse(OpenGrindTrust.trusts(null, signedBy(release)))
    }

    @Test
    fun `asks only about the trusted package, never the caller's own name`() {
        val asked = mutableListOf<String>()
        OpenGrindTrust.trusts("org.opengrind") { name, _ ->
            asked += name
            false
        }
        assertTrue(asked.all { it == OpenGrindTrust.PACKAGE })
    }
}
