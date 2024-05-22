package com.dtd.chaincatch.widget

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.dtd.chaincatch.R
import com.dtd.chaincatch.util.ConstValues

;

class UserCardView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

  private var ivUser: ImageView
  private var tvNickname: TextView
  private var pbExperience: ProgressBar

  init {
    val view = LayoutInflater.from(context).inflate(R.layout.list_item_user, this, false)
    addView(view)
    setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
    ivUser = findViewById(R.id.iv_user)
    tvNickname = findViewById(R.id.tv_nickname)
    pbExperience = findViewById(R.id.progress_bar_experience)
    getAttrs(attrs, defStyleAttr)
  }

  private fun getAttrs(attrs: AttributeSet?, defStyleAttr: Int) {
    val typedArray = context.obtainStyledAttributes(attrs, R.styleable.UserCardView)
    setTypedArray(typedArray, defStyleAttr)
  }

  private fun setTypedArray(typedArray: TypedArray, defStyleAttr: Int) {
    setUserImage(
      typedArray.getResourceId(R.styleable.UserCardView_ivUser, defStyleAttr)
    )
    setUerNickname(typedArray.getText(R.styleable.UserCardView_tvNickname))
    setUserExperience(typedArray.getInt(R.styleable.UserCardView_pbExperience, defStyleAttr))
    setTextSize(
      typedArray.getDimension(R.styleable.UserCardView_android_textSize, defStyleAttr.toFloat())
    )
    typedArray.recycle()
  }

  fun setUserImage(resId: Int) {
    ivUser.setImageResource(resId)
  }

  fun setUerNickname(nickname: CharSequence) {
    tvNickname.text = nickname
  }

  private fun setTextSize(size: Float) {
    tvNickname.textSize = size
  }

  fun setUserExperience(experience: Int) {
    pbExperience.progress = experience
  }

  fun addUserExperience() {
    pbExperience.progress += ConstValues.EXPERIENCE_STEP
  }
}