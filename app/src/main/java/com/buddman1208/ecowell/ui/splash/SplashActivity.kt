package com.buddman1208.ecowell.ui.splash

import android.Manifest
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.ui.productselect.ProductSelectActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import org.jetbrains.anko.startActivity
import java.util.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        checkPermission()
    }

    private fun checkPermission() {
        TedPermission.with(this)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    startMain()
                }

                override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {
                    finish()
                }
            })
            .setRationaleMessage("앱 사용을 위해서 권한을 허용해 주세요")
            .setDeniedMessage("권한을 허용하셔야 앱 사용이 가능합니다.")
            .setPermissions(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .check()
    }

    private fun startMain() {
        Handler().postDelayed({
            startActivity<ProductSelectActivity>()
            finish()
        }, 1500)
    }
}
