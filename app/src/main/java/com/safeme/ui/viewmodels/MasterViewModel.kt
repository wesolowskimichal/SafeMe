package com.safeme.ui.viewmodels

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeme.db.SettingsDataStore
import com.safeme.models.Constants
import com.safeme.models.Timer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MasterViewModel(
    val dataStore: SettingsDataStore,
    val mediaPlayer: MediaPlayer
):ViewModel() {
    private var _isCountdownRunning = false
    private var _countdownStarted: MutableLiveData<Boolean> = MutableLiveData(false)
    val countdownStarted: LiveData<Boolean>
        get() = _countdownStarted
    private val _timerState = MutableStateFlow(Timer(0, 0))
    val timerState: StateFlow<Timer> = _timerState.asStateFlow()
    private var _countdownFinished: MutableLiveData<Boolean> = MutableLiveData(false)
    val countdownFinished: LiveData<Boolean>
        get() = _countdownFinished
    private var _stopAlertOnLocChng: MutableLiveData<Boolean> = MutableLiveData(false)
    val stopAlertOnLocChng: LiveData<Boolean>
        get() = _stopAlertOnLocChng


    init {
        viewModelScope.launch {
            updateTimerState()
        }
        viewModelScope.launch {
            setLocChng()
        }
    }


    fun startCountdown() {
        if (!_isCountdownRunning) {
            _isCountdownRunning = true
            _countdownStarted.postValue(true)
            mediaPlayer.start()
            viewModelScope.launch {
                while (_isCountdownRunning && (_timerState.value.minutes > 0 || _timerState.value.seconds > 0)) {
                    delay(1000)
                    _timerState.update { currentState ->
                        currentState.copy(
                            seconds = if(currentState.seconds == 0 && currentState.minutes > 0) 59 else  addTime(-1, currentState.seconds),
                            minutes = if (currentState.seconds == 0) addTime(-1, currentState.minutes) else currentState.minutes
                        )
                    }
                }
                if(_isCountdownRunning) {
                    _countdownFinished.postValue(true)
                }
                _isCountdownRunning = false
            }
        }
    }

    fun stopCountdown() {
        if(mediaPlayer.isPlaying){
            mediaPlayer.stop()
        }
        _isCountdownRunning = false
        _countdownFinished.postValue(false)
        viewModelScope.launch{
            updateTimerState()
            setLocChng()
        }
    }

    private suspend fun updateTimerState() {
        withContext(Dispatchers.IO) {
            dataStore.readTimer.collect { timer ->
                val minutes = timer.substring(0, 2).toInt()
                val seconds = timer.substring(3).toInt()
                _timerState.value = Timer(minutes, seconds)
            }
        }
    }

    private suspend fun setLocChng() {
        withContext(Dispatchers.IO) {
            dataStore.readStopOnLocChng.collect { stopOnLocChng ->
                _stopAlertOnLocChng.postValue(stopOnLocChng)
            }
        }
    }

    private fun addTime(addedTime: Int, currentTime: Int): Int {
        val sum = currentTime + addedTime
        return when {
            sum >= 60 -> 59
            sum < 0 -> 0
            else -> sum
        }
    }


}