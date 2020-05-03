package com.buddman1208.ecowell.ui.main

import android.os.Bundle
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.databinding.ActivityMainBinding
import com.buddman1208.ecowell.ui.base.BaseActivity

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>(
    R.layout.activity_main
) {
    override val viewModel: MainViewModel =
        MainViewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.vm = viewModel
    }
}
