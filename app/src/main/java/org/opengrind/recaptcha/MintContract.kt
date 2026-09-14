package org.opengrind.recaptcha

object MintContract {
    const val EXTRA_ACTION = "org.opengrind.recaptcha.extra.ACTION"
    const val EXTRA_TOKEN = "org.opengrind.recaptcha.extra.TOKEN"
    const val EXTRA_ERROR = "org.opengrind.recaptcha.extra.ERROR"
    const val EXTRA_ERROR_DETAIL = "org.opengrind.recaptcha.extra.ERROR_DETAIL"

    const val TRUSTED_CALLER_PACKAGE = "org.opengrind"
    const val GRINDR_PACKAGE = "com.grindrapp.android"

    val GRINDR_ACTIONS = setOf(
        "sign_up",
        "login",
        "forgot_password",
        "report",
        "decision_appeal",
        "device_key_registration",
    )
}
