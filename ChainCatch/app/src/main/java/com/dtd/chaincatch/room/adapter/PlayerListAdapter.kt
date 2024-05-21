package com.dtd.chaincatch.room.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dtd.chaincatch.R
import com.dtd.chaincatch.databinding.ListItemUserBinding
import com.dtd.chaincatch.home.fragment.HomeFragment
import com.dtd.chaincatch.user.model.dto.UserDto

class PlayerListAdapter : ListAdapter<UserDto, PlayerListAdapter.ViewHolder>(diffUtil) {
  inner class ViewHolder(private val binding: ListItemUserBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(user: UserDto) {
      binding.tvNickname.text = user.nickname
      binding.progressBarExperience.progress = user.experience

      when (user.profileImg) {
        CAT_CHEESE -> binding.ivUser.setImageResource(R.drawable.cat_cheese_face)
        CAT_GREY -> binding.ivUser.setImageResource(R.drawable.cat_grey_face)
        CAT_FISH -> binding.ivUser.setImageResource(R.drawable.cat_fish_face)
        CAT_RAINBOW -> binding.ivUser.setImageResource(R.drawable.cat_rainbow_face)
      }

      binding.tvNickname.text = user.nickname
      binding.progressBarExperience.progress = user.experience
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val binding = ListItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return ViewHolder(binding)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position))
  }


  companion object {
    val diffUtil = object : DiffUtil.ItemCallback<UserDto>() {
      override fun areContentsTheSame(oldItem: UserDto, newItem: UserDto): Boolean {
        return oldItem == newItem
      }

      override fun areItemsTheSame(oldItem: UserDto, newItem: UserDto): Boolean {
        return oldItem.uid == newItem.uid
      }
    }

    private const val CAT_CHEESE = 0
    private const val CAT_GREY = 1
    private const val CAT_FISH = 2
    private const val CAT_RAINBOW = 3
  }
}