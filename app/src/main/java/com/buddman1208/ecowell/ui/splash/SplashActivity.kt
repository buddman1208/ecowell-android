package com.buddman1208.ecowell.ui.splash

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.ui.productselect.ProductSelectActivity
import org.jetbrains.anko.startActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        startMain()
    }

    private fun startMain() {
        Handler().postDelayed({
            startActivity<ProductSelectActivity>()
            finish()
        }, 1500)
    }
}