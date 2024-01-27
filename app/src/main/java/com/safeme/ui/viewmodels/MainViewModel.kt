package com.safeme.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.safeme.controllers.MainController

class MainViewModel(private val controller: MainController): ViewModel() {
    private var _isStarted: MutableLiveData<Boolean> = MutableLiveData(false)
    val isStarted: LiveData<Boolean>
        get() = _isStarted


    private fun startLocationUpdates() = controller.startLocationUpdates()
    private fun stopLocationUpdates() = controller.stopLocationUpdates()
    fun getPositions(): LiveData<MutableList<LatLng>> = controller.positions
    fun getLastPosition(): LiveData<LatLng> = controller.lastPosition
    fun getSteps(): LiveData<Int> = controller.steps
    fun deviceDropped(): LiveData<Boolean> = controller.accelerometerValue
    val isMoving: LiveData<Boolean> = controller.isMoving

    fun start() {
        _isStarted.value = _isStarted.value?.not() ?: true
        if (_isStarted.value == true) {
            startLocationUpdates()
        } else {
            stopLocationUpdates()
        }
    }
}