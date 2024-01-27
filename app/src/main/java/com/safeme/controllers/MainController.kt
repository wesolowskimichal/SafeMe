package com.safeme.controllers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import kotlin.math.log
import kotlin.math.sqrt

class MainController(val context: Context): SensorEventListener {
    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var accelerometer: Sensor? = null

    private var _positions: MutableLiveData<MutableList<LatLng>> = MutableLiveData(mutableListOf())
    val positions: LiveData<MutableList<LatLng>>
        get() = _positions

    private var _lastPosition: MutableLiveData<LatLng> = MutableLiveData()
    val lastPosition: LiveData<LatLng>
        get() = _lastPosition

    private var _steps: MutableLiveData<Int> = MutableLiveData(-1)
    val steps: LiveData<Int>
        get() = _steps
    private var initSteps = -1

    private val _threshold = 9.8 * 8.0
    private var _accelerometerValue: MutableLiveData<Boolean> = MutableLiveData(false)
    val accelerometerValue: LiveData<Boolean>
        get() = _accelerometerValue

    private var _isMoving: MutableLiveData<Boolean> = MutableLiveData(true)
    val isMoving: LiveData<Boolean>
        get() = _isMoving
    private var _lastPositionVar: LatLng? = null

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val newReading = LatLng(location.latitude, location.longitude)
            addPos(newReading)
        }
    }

    init {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if(stepSensor == null) {
                Toast.makeText(context, "No steps sensor detected on this device", Toast.LENGTH_SHORT).show()
            }
            if(accelerometer == null) {
                Toast.makeText(context, "No accelerometer detected on this device", Toast.LENGTH_SHORT).show()
            }
            val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastKnownLocation != null) {
                val initialReading = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
                _lastPosition.postValue(initialReading)
            } else {
                _lastPosition.postValue(LatLng(0.0, 0.0))
            }
        }
    }

    private fun addPos(pos: LatLng) {
        _positions.value?.add(pos)
        _positions.value = _positions.value
        _lastPosition.postValue(pos)
        Log.d("pos", pos.toString())
        Log.d("oldpos", _lastPositionVar.toString())
        if(_lastPositionVar != null) {
            if(_lastPositionVar == pos) {
                _isMoving.postValue(false)
            } else {
                _isMoving.postValue(true)
            }
        }
        _lastPositionVar = pos
    }

    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if(stepSensor == null) {
            Toast.makeText(context, "No step sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }

        if(accelerometer == null) {
            Toast.makeText(context, "No accelerometer detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,   // 1000 milliseconds = 1 second
                0.0f,   // 0 meters, all location changes
                locationListener
        )
    }

    fun stopLocationUpdates() {
        locationManager.removeUpdates(locationListener)
        sensorManager?.unregisterListener(this, stepSensor)
        sensorManager?.unregisterListener(this, accelerometer)
        initSteps = -1
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when(event?.sensor) {
            stepSensor -> {
                if(initSteps == -1) {
                    initSteps = event!!.values[0].toInt()
                }
                _steps.postValue(event!!.values[0].toInt() - initSteps)
            }
            accelerometer -> {
                val x = event!!.values[0]
                val y = event.values[1]
                val z = event.values[2]

                // Calculate the magnitude of acceleration
                val acceleration = sqrt(x * x + y * y + z * z)

                // Check if the acceleration exceeds the threshold
                if (acceleration > _threshold) {
                    _accelerometerValue.postValue(true)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        println("onAccuracyChanged: Sensor: $sensor; accuracy: $accuracy")
    }
}