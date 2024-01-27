package com.safeme.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.safeme.R
import com.safeme.models.Contact

class ContactAdapter: RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {
    inner class ContactViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.contact_preview,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = differ.currentList[position]
        val name = holder.itemView.findViewById<TextView>(R.id.tvName)
        val number = holder.itemView.findViewById<TextView>(R.id.tvNumber)
        holder.itemView.apply {
            name.text = contact.name
            number.text = contact.number.toString()
            setOnClickListener{
                onItemClickListener?.let {
                    it(contact)
                }
            }
        }
    }

    private var onItemClickListener : ((Contact) -> Unit)? = null

    fun setOnItemClickListener(listener: (Contact) -> Unit) {
        onItemClickListener = listener
    }
}