package com.example.socialbatterymanager.features.people.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.model.Person

class PersonAdapter(
    private val onItemClick: (Person) -> Unit,
    private val onMoreClick: (Person) -> Unit
) : ListAdapter<Person, PersonAdapter.PersonViewHolder>(PersonDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_person, parent, false)
        return PersonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAvatar: ImageView = itemView.findViewById(R.id.ivAvatar)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        private val tvPhone: TextView = itemView.findViewById(R.id.tvPhone)
        private val ivMore: ImageView = itemView.findViewById(R.id.ivMore)

        fun bind(person: Person) {
            tvName.text = person.name
            tvEmail.text = person.email ?: ""
            tvPhone.text = person.phone ?: ""
            
            // Show/hide email and phone if empty
            tvEmail.visibility = if (person.email.isNullOrEmpty()) View.GONE else View.VISIBLE
            tvPhone.visibility = if (person.phone.isNullOrEmpty()) View.GONE else View.VISIBLE

            // Load avatar image if available
            if (!person.avatarPath.isNullOrEmpty()) {
                try {
                    val uri = android.net.Uri.parse(person.avatarPath)
                    ivAvatar.setImageURI(uri)
                } catch (e: Exception) {
                    // If avatar can't be loaded, use default icon
                    ivAvatar.setImageResource(R.drawable.ic_person)
                }
            } else {
                ivAvatar.setImageResource(R.drawable.ic_person)
            }

            itemView.setOnClickListener { onItemClick(person) }
            ivMore.setOnClickListener { onMoreClick(person) }
        }
    }

    class PersonDiffCallback : DiffUtil.ItemCallback<Person>() {
        override fun areItemsTheSame(oldItem: Person, newItem: Person): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Person, newItem: Person): Boolean {
            return oldItem == newItem
        }
    }
}