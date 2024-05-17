package com.dtd.chaincatch.home

import android.os.Bundle
import com.dtd.chaincatch.R
import com.dtd.chaincatch.config.BaseActivity
import com.dtd.chaincatch.databinding.ActivityHomeBinding
import com.dtd.chaincatch.home.fragment.HomeFragment

class HomeActivity : BaseActivity<ActivityHomeBinding>(ActivityHomeBinding::inflate) {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    supportFragmentManager.beginTransaction().replace(R.id.home_fragment_container, HomeFragment())
      .commit()

  }

  override fun onDestroy() {
    super.onDestroy()
    _binding = null
  }
}