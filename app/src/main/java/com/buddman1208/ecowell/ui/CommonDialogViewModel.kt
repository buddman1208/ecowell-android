package com.buddman1208.ecowell.ui

import androidx.databinding.ObservableField
import com.buddman1208.ecowell.base.BaseViewModel

class CommonDialogViewModel : BaseViewModel() {

    val dialogTitle : ObservableField<String> = ObservableField("")
    val dialogContent : ObservableField<String> = ObservableField("")

}