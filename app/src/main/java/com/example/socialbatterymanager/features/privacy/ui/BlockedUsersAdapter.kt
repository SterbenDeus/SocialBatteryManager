package com.example.socialbatterymanager.features.privacy.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.socialbatterymanager.R
import com.example.socialbatterymanager.data.model.BlockedUser

class BlockedUsersAdapter(
    private val blockedUsers: List<BlockedUser>,
    private val onRemoveClick: (BlockedUser) -> Unit
) : RecyclerView.Adapter<BlockedUsersAdapter.BlockedUserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedUserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blocked_user, parent, false)
        return BlockedUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlockedUserViewHolder, position: Int) {
        holder.bind(blockedUsers[position])
    }

    override fun getItemCount(): Int = blockedUsers.size

    inner class BlockedUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)
        private val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
        private val removeButton: ImageView = itemView.findViewById(R.id.removeButton)

        fun bind(blockedUser: BlockedUser) {
            nameTextView.text = blockedUser.blockedUserName
            emailTextView.text = blockedUser.blockedUserEmail ?: "@${blockedUser.blockedUserId}"
            
            // Set profile image - in real app, load from URI or use default
            profileImageView.setImageResource(R.drawable.ic_people)
            
            removeButton.setOnClickListener {
                onRemoveClick(blockedUser)
            }
        }
    }
}