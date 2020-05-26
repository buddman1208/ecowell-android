package com.buddman1208.ecowell.ui.commondialog

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.buddman1208.ecowell.ui.base.BaseDialogViewModel
import com.buddman1208.ecowell.utils.clearAndSet

class CommonDialogViewModel : BaseDialogViewModel() {

    val isOnlyConfirmable: ObservableBoolean = ObservableBoolean(false)
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