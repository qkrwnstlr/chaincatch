package com.dtd.chaincatch.home

import android.os.Bundle
import com.dtd.chaincatch.R
import com.dtd.chaincatch.config.BaseActivity
import com.dtd.chaincatch.databinding.ActivityHomeBinding
import com.dtd.chaincatch.drawing.fragment.DrawingFragment

class HomeActivity : BaseActivity<ActivityHomeBinding>(ActivityHomeBinding::inflate) {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, DrawingFragment())
      .commit()


  }
}