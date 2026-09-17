package org.opengrind.recaptcha

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle

class MintTokenActivity : Activity() {

    private var resultDelivered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val admission = MintAdmission.decide(
            isCallerTrusted = ::isCallerTrusted,
            requestedAction = intent.getStringExtra(MintContract.EXTRA_ACTION),
            isGrindrInstalled = ::isGrindrInstalled,
        )
        when (admission) {
            is MintAdmission.Refused -> refuse(admission.error)
            is MintAdmission.Admitted -> RecaptchaMint.mint(application, admission.action)
                .addOnSuccessListener { token -> deliver(RESULT_OK, Intent().putExtra(MintContract.EXTRA_TOKEN, token)) }
                .addOnFailureListener { error -> refuse(MintError.MINT_FAILED, RecaptchaMint.failureDetail(error)) }
        }
    }

    private fun refuse(error: MintError, detail: String? = null) {
        val data = Intent().putExtra(MintContract.EXTRA_ERROR, error.marker)
        detail?.let { data.putExtra(MintContract.EXTRA_ERROR_DETAIL, it) }
        deliver(RESULT_CANCELED, data)
    }

    private fun deliver(resultCode: Int, data: Intent) {
        if (resultDelivered || isFinishing || isDestroyed) return
        resultDelivered = true
        setResult(resultCode, data)
        finish()
    }

    private fun isCallerTrusted(): Boolean =
        OpenGrindTrust.trusts(callingPackage) { name, sha256 ->
            packageManager.hasSigningCertificate(
                name,
                sha256,
                PackageManager.CERT_INPUT_SHA256,
            )
        }

    private fun isGrindrInstalled(): Boolean = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                MintContract.GRINDR_PACKAGE,
                PackageManager.PackageInfoFlags.of(0),
            )
        } else {
            packageManager.getPackageInfo(MintContract.GRINDR_PACKAGE, 0)
        }
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}
