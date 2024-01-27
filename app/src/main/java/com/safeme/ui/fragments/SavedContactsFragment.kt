package com.safeme.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.safeme.R
import com.safeme.adapters.ContactAdapter
import com.safeme.models.Contact
import com.safeme.ui.MainActivity
import com.safeme.ui.viewmodels.ContactsViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.ArrayList
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

class SavedContactsFragment: Fragment(R.layout.saved_contacts_fragment) {
    lateinit var viewModel: ContactsViewModel
    lateinit var contactsAdapter: ContactAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).contactsViewModel
        contactsAdapter = ContactAdapter()
        initRecyclerView()

        viewModel.getSavedContacts().observe(viewLifecycleOwner, Observer { contacts ->
            contactsAdapter.differ.submitList(contacts)
        })

        val minutesDecimal = view.findViewById<TextView>(R.id.minute_decimal)
        val minutesDigit = view.findViewById<TextView>(R.id.minute_digit)
        val secondsDecimal = view.findViewById<TextView>(R.id.second_decimal)
        val secondsDigit = view.findViewById<TextView>(R.id.second_digit)
        val checkBox = view.findViewById<CheckBox>(R.id.cbStopOnLocChng)
        val btnSaveSettings = view.findViewById<Button>(R.id.btnSaveSettings)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dataStore.readStopOnLocChng.collect{isChecked ->
                checkBox.isChecked = isChecked
            }
        }

        viewModel.minutes.observe(viewLifecycleOwner, Observer { minutes ->
            minutesDecimal.text = (minutes/10).toString()
            minutesDigit.text = (minutes%10).toString()
        })

        viewModel.seconds.observe(viewLifecycleOwner, Observer { seconds ->
            secondsDecimal.text = (seconds/10).toString()
            secondsDigit.text = (seconds%10).toString()
        })


        val _add10Minutes = view.findViewById<Button>(R.id.add_10_minutes)
        val _add1Minute = view.findViewById<Button>(R.id.add_1_minute)
        val _add10Seconds = view.findViewById<Button>(R.id.add_10_seconds)
        val _add1Second = view.findViewById<Button>(R.id.add_1_second)
        val _sub10Minutes = view.findViewById<Button>(R.id.sub_10_minutes)
        val _sub1Minute = view.findViewById<Button>(R.id.sub_1_minute)
        val _sub10Seconds = view.findViewById<Button>(R.id.sub_10_seconds)
        val _sub1Second = view.findViewById<Button>(R.id.sub_1_second)

        // change it to viewModel
        _add10Minutes.setOnClickListener {
            viewModel.addMinutes(10)
        }
        _add10Seconds.setOnClickListener {
            viewModel.addSeconds(10)
        }
        _add1Minute.setOnClickListener {
            viewModel.addMinutes(1)
        }
        _add1Second.setOnClickListener {
            viewModel.addSeconds(1)
        }
        _sub10Minutes.setOnClickListener {
            viewModel.addMinutes(-10)
        }
        _sub1Minute.setOnClickListener {
            viewModel.addMinutes(-1)
        }
        _sub10Seconds.setOnClickListener {
            viewModel.addSeconds(-10)
        }
        _sub1Second.setOnClickListener {
            viewModel.addSeconds(-1)
        }

        btnSaveSettings.setOnClickListener{
            viewLifecycleOwner.lifecycleScope.launch {
                val timer = "${minutesDecimal.text}${minutesDigit.text}:${secondsDecimal.text}${secondsDigit.text}"
                viewModel.dataStore.saveSettings(timer, checkBox.isChecked)
            }
        }

        val fab = view.findViewById<FloatingActionButton>(R.id.fab_addContact)
        fab.setOnClickListener {
            val serList = Json.encodeToString(contactsAdapter.differ.currentList)
            val bundle = Bundle().apply {
                putString("serlist", serList)
            }
            findNavController().navigate(
                R.id.action_savedContactsFragment_to_addContactFragment,
                bundle
            )
        }
    }

    private fun initRecyclerView() {
        val rvSavedContacts = view?.findViewById<RecyclerView>(R.id.rvSavedContacts)
        rvSavedContacts?.apply {
            adapter = contactsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }
}