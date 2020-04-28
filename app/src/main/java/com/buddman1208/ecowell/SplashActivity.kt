package com.buddman1208.ecowell

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import org.jetbrains.anko.startActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        startMain()
    }

    private fun startMain() {
        Handler().postDelayed({
            startActivity<MainActivity>()
            finish()
        }, 1500)
    }
}
