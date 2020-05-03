package com.buddman1208.ecowell.ui.commondialog

import androidx.databinding.ObservableField
import com.buddman1208.ecowell.base.BaseDialogViewModel
import com.buddman1208.ecowell.utils.clearAndSet

class CommonDialogViewModel : BaseDialogViewModel() {

    val dialogTitle: ObservableField<String> = ObservableField("")
    val dialogContent: ObservableField<String> = ObservableField("")
    var positiveCallback: (() -> Unit)? = null
    var negativeCallback: (() -> Unit)? = null

    fun onPositiveClick() {
        positiveCallback?.invoke()
        dismissEvent.clearAndSet("dismiss")
    }

    fun onNegativeClick() {
        negativeCallback?.invoke()
        dismissEvent.clearAndSet("dismiss")
    }
}