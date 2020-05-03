package com.buddman1208.ecowell.base

import androidx.databinding.ObservableField

abstract class BaseDialogViewModel : BaseViewModel() {

    val dismissEvent: ObservableField<String> = ObservableField("")

}