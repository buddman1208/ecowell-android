package com.buddman1208.ecowell

import android.app.Application
import com.tsengvn.typekit.Typekit

class AppController : Application() {

    override fun onCreate() {
        super.onCreate()
        initFont()
    }

    private fun initFont() {
        Typekit.getInstance()
            .addNormal(Typekit.createFromAsset(this, "NanumSquareRegular.ttf"))
            .addBold(Typekit.createFromAsset(this, "NanumSquareExtraBold.ttf"))

    }
}