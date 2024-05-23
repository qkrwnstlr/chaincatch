package com.dtd.chaincatch.view.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.dtd.chaincatch.R
import com.dtd.chaincatch.databinding.ListItemRoomBinding
import com.dtd.chaincatch.model.dto.RoomDto

class HomeAdapter(private val context: Context, private val items: List<RoomDto>) :
  RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

  private var lastPosition = -1
  private var list: List<RoomDto> = items
  private var onItemClickListener: ((room: RoomDto) -> Unit)? = null

  fun setOnItemClickListener(onItemClickListener: (room: RoomDto) -> Unit) {
    this.onItemClickListener = onItemClickListener
  }

  inner class HomeViewHolder(private val binding: ListItemRoomBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(room: RoomDto) {
      with(binding) {
        tvTitle.text = room.title
        tvManager.text = room.manager
        if (room.state == "Playing") {
          tvStatus.text = "게임 중!"
          tvStatus.setTextColor(Color.parseColor("#AE0505"))
        } else {
          tvStatus.text = "대기 중"
          tvStatus.setTextColor(Color.parseColor("#0505AE"))
        }
        tvCurrentPlayer.text = room.currentUser.toString()
        if (room.currentUser < room.maxUser) {
          root.setOnClickListener { onItemClickListener?.invoke(room) }
        }
      }

//      val animator = ObjectAnimator.ofFloat(itemView, "translationX", -5f, 5f)
//      animator.duration = 500
//      animator.interpolator = AccelerateDecelerateInterpolator()
//      animator.repeatMode = ValueAnimator.REVERSE
//      animator.repeatCount = ValueAnimator.INFINITE
//      animator.start()
    }

  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
    val binding = ListItemRoomBinding.inflate(LayoutInflater.from(context), parent, false)
    return HomeViewHolder(binding)
  }

  override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
    holder.bind(list[position])
    setAnimation(holder.itemView, position)
  }

  override fun getItemCount() = list.size

  private fun setAnimation(viewToAnimate: View, position: Int) {
    if (position > lastPosition) {
      val animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_left)
      viewToAnimate.startAnimation(animation)
      lastPosition = position
    }
  }

  fun submitList(newDataList: List<RoomDto>) {
    list = newDataList
    notifyDataSetChanged()
  }


}