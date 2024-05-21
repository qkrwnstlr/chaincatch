package com.dtd.chaincatch.room

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.dtd.chaincatch.R
import com.dtd.chaincatch.config.BaseActivity
import com.dtd.chaincatch.databinding.ActivityRoomBinding
import com.dtd.chaincatch.room.fragment.RoomFragment
import com.dtd.chaincatch.room.viewmodel.RoomViewModel


class RoomActivity : BaseActivity<ActivityRoomBinding>(ActivityRoomBinding::inflate) {
  private val onBackPressed = object : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
      val currentFragment = supportFragmentManager.findFragmentById(R.id.room_fragment_container)

      if (currentFragment is RoomFragment) {
        viewModel.exitRoom()
      } else {
        finish()
      }
    }
  }

  private val viewModel by viewModels<RoomViewModel>()
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    this.onBackPressedDispatcher.addCallback(this, onBackPressed)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      overrideActivityTransition(
        OVERRIDE_TRANSITION_OPEN,
        com.google.android.material.R.anim.abc_fade_in,
        com.google.android.material.R.anim.abc_fade_out,
        Color.BLACK
      )
    }

    supportFragmentManager
      .beginTransaction()
      .setReorderingAllowed(true)
      .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
      .replace(R.id.room_fragment_container, RoomFragment())
      .commit()
  }

//  override fun onBackPressed() {
//    val currentFragment = supportFragmentManager.findFragmentById(R.id.room_fragment_container)
//
//    // 특정 프래그먼트를 확인합니다.
//    if (currentFragment is RoomFragment) {
//      viewModel.exitRoom()
//    } else {
//      super.onBackPressed() // 기본 동작을 수행합니다.
//    }
//  }
}