package org.opengrind.recaptcha

import android.app.Application
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.recaptcha.Recaptcha
import com.google.android.recaptcha.RecaptchaAction
import com.google.android.recaptcha.RecaptchaException

object RecaptchaMint {
    private const val SITE_KEY = "6LeGNmIqAAAAAIi9lNGTgQbFdzohq1cRfQCZW7eB"
    private const val TIMEOUT_MILLIS = 20_000L

    fun mint(application: Application, action: String): Task<String> = try {
        Recaptcha.fetchTaskClient(application, SITE_KEY).onSuccessTask { client ->
            client.executeTask(RecaptchaAction.custom(action), TIMEOUT_MILLIS)
        }
    } catch (e: RuntimeException) {
        Tasks.forException(e)
    }

    fun failureDetail(error: Throwable): String {
        val recaptchaError = generateSequence(error) { it.cause }
            .filterIsInstance<RecaptchaException>()
            .firstOrNull()
        return recaptchaError?.errorCode?.name ?: error.javaClass.simpleName
    }
}
