package com.buddman1208.ecowell

import android.app.Application
import android.content.Context
import com.buddman1208.ecowell.utils.BLEController
import com.buddman1208.ecowell.utils.CredentialManager
import com.buddman1208.ecowell.utils.LocaleWrapper

class AppController : Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        LocaleWrapper.setLocale(
            CredentialManager.instance.language
        )
        initFont()
        initBleClient()
    }

    private fun initFont() {
//        Typekit.getInstance()
//            .addNormal(Typekit.createFromAsset(this, "NanumSquareRegular.ttf"))
//            .addBold(Typekit.createFromAsset(this, "NanumSquareExtraBold.ttf"))

    }

    private fun initBleClient() {
        BLEController.initialize(this@AppController)
    }

    companion object {
        lateinit var context: Context
    }
}