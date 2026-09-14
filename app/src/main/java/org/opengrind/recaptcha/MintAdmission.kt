package org.opengrind.recaptcha

enum class MintError(val marker: String) {
    UNTRUSTED_CALLER("untrusted-caller"),
    UNSUPPORTED_ACTION("unsupported-action"),
    GRINDR_MISSING("grindr-missing"),
    MINT_FAILED("mint-failed"),
}

sealed interface MintAdmission {
    data class Admitted(val action: String) : MintAdmission
    data class Refused(val error: MintError) : MintAdmission

    companion object {
        fun decide(
            isCallerTrusted: () -> Boolean,
            requestedAction: String?,
            isGrindrInstalled: () -> Boolean,
        ): MintAdmission = when {
            !isCallerTrusted() -> Refused(MintError.UNTRUSTED_CALLER)
            requestedAction == null || requestedAction !in MintContract.GRINDR_ACTIONS ->
                Refused(MintError.UNSUPPORTED_ACTION)
            !isGrindrInstalled() -> Refused(MintError.GRINDR_MISSING)
            else -> Admitted(requestedAction)
        }
    }
}
