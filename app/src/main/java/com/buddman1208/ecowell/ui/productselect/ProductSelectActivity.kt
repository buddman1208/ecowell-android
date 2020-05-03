package com.buddman1208.ecowell.ui.productselect

import android.os.Bundle
import com.buddman1208.ecowell.R
import com.buddman1208.ecowell.base.BaseActivity
import com.buddman1208.ecowell.databinding.ActivityProductSelectBinding

class ProductSelectActivity : BaseActivity<ActivityProductSelectBinding, ProductSelectViewModel>(
    R.layout.activity_product_select
) {
    override val viewModel: ProductSelectViewModel =
        ProductSelectViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.vm = viewModel
    }
}
