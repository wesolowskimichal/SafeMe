package com.safeme.ui.fragments

import android.content.Intent
import android.content.Intent.getIntent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavArgs
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.safeme.R
import com.safeme.adapters.AddContactAdapter
import com.safeme.models.Contact
import com.safeme.ui.MainActivity
import com.safeme.ui.viewmodels.ContactsViewModel
import kotlinx.coroutines.launch
import androidx.navigation.fragment.navArgs
import kotlinx.serialization.json.Json

class AddContactFragment: Fragment(R.layout.add_contact_fragment) {
    lateinit var viewModel: ContactsViewModel
    lateinit var contactsAdapter: AddContactAdapter
    val args: AddContactFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).contactsViewModel
        initRecyclerView()
        val serializedContacts = Json.decodeFromString<ArrayList<Contact>>(args.serlist)
        viewModel.getAllContacts(serializedContacts).observe(viewLifecycleOwner, Observer { contacts ->
            contactsAdapter.differ.submitList(contacts)
        })

        contactsAdapter.setOnItemClickListener {contact ->
            contact.fav = !contact.fav
            contactsAdapter.notifyItemChanged(contactsAdapter.differ.currentList.indexOf(contact))
            lifecycleScope.launch {
                val existingContact = viewModel.getContactByPhoneNumber(contact.number)
                if (existingContact != null) {
                    if (existingContact.fav != contact.fav) {
                        viewModel.updateFavByPhoneNumber(contact.number, contact.fav)
                    }
                } else {
                    viewModel.saveContact(contact)
                }
            }
        }

    }


    private fun initRecyclerView() {
        contactsAdapter = AddContactAdapter()
        val rv = view?.findViewById<RecyclerView>(R.id.rvAddContacts)
        rv?.apply {
            adapter = contactsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }
}