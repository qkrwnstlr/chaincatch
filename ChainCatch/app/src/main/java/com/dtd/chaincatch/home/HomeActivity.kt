package com.dtd.chaincatch.home

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import com.dtd.chaincatch.R
import com.dtd.chaincatch.config.BaseActivity
import com.dtd.chaincatch.databinding.ActivityHomeBinding
import com.dtd.chaincatch.home.fragment.HomeFragment

class HomeActivity : BaseActivity<ActivityHomeBinding>(ActivityHomeBinding::inflate) {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      overrideActivityTransition(
        OVERRIDE_TRANSITION_OPEN,
        R.anim.fade_in,
        R.anim.fade_out,
        Color.BLACK
      )
    }

    supportFragmentManager
      .beginTransaction()
//      .setReorderingAllowed(true)
//      .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
      .replace(R.id.home_fragment_container, HomeFragment())
      .commit()
  }

}