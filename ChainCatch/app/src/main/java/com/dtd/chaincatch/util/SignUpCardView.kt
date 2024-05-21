package com.dtd.chaincatch.util

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.dtd.chaincatch.R

class SignUpCardView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

  private var ivCat: ImageView
  private var tvCat: TextView

  init {
    val view = LayoutInflater.from(context).inflate(R.layout.card_item_sign_up_dialog, this, false)
    addView(view)
    setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
    ivCat = findViewById(R.id.iv_cat)
    tvCat = findViewById(R.id.tv_cat)
    getAttrs(attrs)
  }

  private fun getAttrs(attrs: AttributeSet?) {
    val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SignUpCardView)
    setTypedArray(typedArray)
  }

  private fun setTypedArray(typedArray: TypedArray) {
    setCatImageResource(
      typedArray.getResourceId(
        R.styleable.SignUpCardView_ivCat,
        R.raw.cat_white_with_fish_animated
      )
    )
    setCatName(typedArray.getText(R.styleable.SignUpCardView_tvCat))
    typedArray.recycle()
  }

  fun setCatImageResource(resId: Int) {
    Glide.with(context).load(resId).into(ivCat)
  }

  fun setCatName(text: CharSequence) {
    tvCat.text = text
  }
}