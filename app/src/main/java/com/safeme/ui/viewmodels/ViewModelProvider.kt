package com.safeme.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.safeme.controllers.MainController

class ViewModelProvider(
    val controller: MainController
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(controller) as T
    }
}