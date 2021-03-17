package com.buddman1208.ecowell.ui.test

import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.buddman1208.ecowell.R
import kotlinx.android.synthetic.main.activity_test.*


class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        val timer = object : CountDownTimer(60 * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                tvTime.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {

            }
        }
        timer.start()
    }
}