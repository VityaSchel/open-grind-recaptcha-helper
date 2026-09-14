package org.opengrind.recaptcha

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class MintAdmissionTest {

    private fun decide(
        callerTrusted: Boolean = true,
        action: String? = "report",
        grindrInstalled: Boolean = true,
    ) = MintAdmission.decide(
        isCallerTrusted = { callerTrusted },
        requestedAction = action,
        isGrindrInstalled = { grindrInstalled },
    )

    @Test
    fun admitsEveryGrindrWireAction() {
        val wireActions = listOf(
            "sign_up",
            "login",
            "forgot_password",
            "report",
            "decision_appeal",
            "device_key_registration",
        )
        wireActions.forEach { action ->
            assertEquals(MintAdmission.Admitted(action), decide(action = action))
        }
        assertEquals(wireActions.toSet(), MintContract.GRINDR_ACTIONS)
    }

    @Test
    fun refusesActionsOutsideTheAllowlist() {
        listOf(null, "", "REPORT", "signup", "report ", "custom_action").forEach { action ->
            assertEquals(
                MintAdmission.Refused(MintError.UNSUPPORTED_ACTION),
                decide(action = action),
            )
        }
    }

    @Test
    fun refusesAnUntrustedCallerBeforeInspectingAnythingElse() {
        val admission = MintAdmission.decide(
            isCallerTrusted = { false },
            requestedAction = "not-an-action",
            isGrindrInstalled = { fail("probed Grindr for an untrusted caller"); true },
        )
        assertEquals(MintAdmission.Refused(MintError.UNTRUSTED_CALLER), admission)
    }

    @Test
    fun refusesAnUnsupportedActionBeforeProbingGrindr() {
        val admission = MintAdmission.decide(
            isCallerTrusted = { true },
            requestedAction = "not-an-action",
            isGrindrInstalled = { fail("probed Grindr for an unsupported action"); true },
        )
        assertEquals(MintAdmission.Refused(MintError.UNSUPPORTED_ACTION), admission)
    }

    @Test
    fun refusesWhenGrindrIsMissing() {
        assertEquals(
            MintAdmission.Refused(MintError.GRINDR_MISSING),
            decide(grindrInstalled = false),
        )
    }

    @Test
    fun errorMarkersMatchTheContract() {
        assertEquals(
            listOf("untrusted-caller", "unsupported-action", "grindr-missing", "mint-failed"),
            MintError.entries.map { it.marker },
        )
    }
}
