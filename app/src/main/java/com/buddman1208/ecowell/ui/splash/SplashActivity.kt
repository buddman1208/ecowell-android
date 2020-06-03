package com.buddman1208.ecowell.ui.splash

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.ui.productselect.ProductSelectActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import org.jetbrains.anko.startActivity
import java.util.*


class SplashActivity : AppCompatActivity() {

    private var isLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        checkPermission()
    }

    override fun onResume() {
        super.onResume()
        if(isLoaded) checkLocation()
    }

    private fun checkPermission() {
        TedPermission.with(this)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    checkLocation()
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

    private fun checkLocation() {
        Log.e("asdf", "checkLocation")

        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val result: Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(this@SplashActivity)
                .checkLocationSettings(builder.build())

        result.addOnCompleteListener { task ->
            isLoaded = true
            try {
                Log.e("asdf", "completion success")
                val response = task.getResult(ApiException::class.java)
                startMain()
            } catch (exception: ApiException) {
                Log.e("asdf", "completion failure")
                MaterialDialog(this@SplashActivity).show {
                    title(text = "위치를 허용해주셔야 앱 사용이 가능합니다.")
                    positiveButton(res = R.string.ok) {
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(intent)
                    }
                    negativeButton(res = R.string.cancel) {
                        finish()
                    }
                    cancelable(false)
                }

            }
        }
    }

    private fun startMain() {
        Handler().postDelayed({
            startActivity<ProductSelectActivity>()
            finish()
        }, 1500)
    }
}
