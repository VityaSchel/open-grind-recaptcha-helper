package org.opengrind.recaptcha

import android.app.Application

class GrindrBoundApplication : Application() {
    override fun getPackageName(): String = MintContract.GRINDR_PACKAGE
}
