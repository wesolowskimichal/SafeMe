package com.safeme.ui

import android.content.Context
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.safeme.R
import com.safeme.controllers.MainController
import com.safeme.db.ContactDatabase
import com.safeme.db.SettingsDataStore
import com.safeme.repository.ContactsRepository
import com.safeme.ui.viewmodels.ContactsViewModel
import com.safeme.ui.viewmodels.ContactsViewModelProvider
import com.safeme.ui.viewmodels.MainViewModel
import com.safeme.ui.viewmodels.MasterViewModel
import com.safeme.ui.viewmodels.MasterViewModelProvider
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var bnv: BottomNavigationView
    private lateinit var nhf: NavHostFragment
    lateinit var mainViewModel: MainViewModel
    lateinit var contactsViewModel: ContactsViewModel
    lateinit var masterViewModel: MasterViewModel
    lateinit var smsManager: SmsManager
    private lateinit var dataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        smsManager = SmsManager.getDefault()
        dataStore = SettingsDataStore(this)
        val mp = MediaPlayer.create(this, R.raw.alarm)
        val masterViewModelProvider = MasterViewModelProvider(dataStore, mp)
        masterViewModel = ViewModelProvider(this, masterViewModelProvider)[MasterViewModel::class.java]
        val controller = MainController(this)
        val viewModelProvider = com.safeme.ui.viewmodels.ViewModelProvider(controller)
        val contactsRepository = ContactsRepository(ContactDatabase(this))
        val contactsViewModelProvider = ContactsViewModelProvider(application, contactsRepository, dataStore)
        mainViewModel = ViewModelProvider(this, viewModelProvider)[MainViewModel::class.java]
        contactsViewModel = ViewModelProvider(this, contactsViewModelProvider)[ContactsViewModel::class.java]

        setContentView(R.layout.activity_main)

        val tvTimer = findViewById<TextView>(R.id.tv_timer)
        val btnStop = findViewById<Button>(R.id.btn_stopalert)


        btnStop.isVisible = false
        tvTimer.isVisible = false

        lifecycleScope.launch{
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                masterViewModel.timerState.collect {timer ->
                    val minutes = if(timer.minutes < 10) "0${timer.minutes}" else "${timer.minutes}"
                    val seconds = if(timer.seconds < 10) "0${timer.seconds}" else "${timer.seconds}"
                    tvTimer.text = "$minutes:$seconds"
                }
            }
        }


        mainViewModel.deviceDropped().observe(this, Observer { deviceDropped ->
            if(deviceDropped) {
                masterViewModel.startCountdown()
                btnStop.isVisible = true
                tvTimer.isVisible = true
                Log.d("Device", "DROPPED")
            }
        })

        masterViewModel.countdownStarted.observe(this, Observer{countdownStarted ->
            if(countdownStarted) {
                masterViewModel.stopAlertOnLocChng.observe(this, Observer {stopAlert ->
                    if(stopAlert) {
                        mainViewModel.isMoving.observe(this, Observer{ isMoving ->
                            if(isMoving) {
                                masterViewModel.stopCountdown()
                                btnStop.isVisible = false
                                tvTimer.isVisible = false
                            }
                        })
                    }
                })
            }
        })


        btnStop.setOnClickListener {
            masterViewModel.stopCountdown()
            btnStop.isVisible = false
            tvTimer.isVisible = false
        }

        masterViewModel.countdownFinished.observe(this, Observer {
            if(it) {
                contactsRepository.getSavedContacts().observe(this, Observer{savedContacts ->
                    var smsText = "Czujniki telefonu odczytaly upadek oraz brak reakcji\n"
                    val currLoc = "https://www.google.com/maps/place/${mainViewModel.getLastPosition().value?.latitude},${mainViewModel.getLastPosition().value?.longitude}"
//                    smsText += "Moja pozycja: ${mainViewModel.getLastPosition().value?.latitude},${mainViewModel.getLastPosition().value?.longitude}\n$currLoc\n"
                    smsText += "Moja pozycja: ${mainViewModel.getLastPosition().value?.latitude},${mainViewModel.getLastPosition().value?.longitude}"
                    val points = mainViewModel.getPositions().value as List<LatLng>
                    if(!points.isEmpty()) {
                        val waypoints = points.joinToString("|") { "${it.latitude},${it.longitude}"}
//                        val url = "https://www.google.com/maps/dir/?api=1&origin=${points.first().latitude},${points.first().longitude}&destination=${points.last().latitude},${points.last().longitude}&waypoints=$waypoints"
//                        smsText += "Moja trasa: $url"
                    }
                    for(contact in savedContacts) {
                        smsManager.sendTextMessage(contact.number, null, smsText, null, null)
                        Log.d("hfdskjfnsdkfsdkj SENDING", "${contact.toString()}: ${smsText}\n${smsText.length}")
                    }
                })
            }
        })


        bnv = findViewById(R.id.bottomNavigationView)
        nhf = supportFragmentManager.findFragmentById(R.id.contentNavHostFragment) as NavHostFragment
        bnv.setupWithNavController(nhf.findNavController())
    }
}