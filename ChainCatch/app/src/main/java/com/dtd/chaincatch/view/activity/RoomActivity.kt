package com.dtd.chaincatch.view.activity

import android.app.AlertDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.dtd.chaincatch.R
import com.dtd.chaincatch.config.BaseActivity
import com.dtd.chaincatch.databinding.ActivityRoomBinding
import com.dtd.chaincatch.databinding.DialogBaseWithButtonsBinding
import com.dtd.chaincatch.util.dp
import com.dtd.chaincatch.view.fragment.RoomFragment
import com.dtd.chaincatch.viewmodel.RoomViewModel

class RoomActivity : BaseActivity<ActivityRoomBinding>(ActivityRoomBinding::inflate) {
  private val viewModel by viewModels<RoomViewModel>()

  private val onBackPressed = object : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
      val currentFragment = supportFragmentManager.findFragmentById(R.id.room_fragment_container)

      if (currentFragment is RoomFragment) {
        val binding = DialogBaseWithButtonsBinding.inflate(layoutInflater)
        binding.tvMessage.text = "게임을 나가시겠습니까?"

        val dialog = AlertDialog.Builder(this@RoomActivity, R.style.CustomAlertDialog)
          .setView(binding.root).show().apply { window?.setLayout(400.dp, 250.dp) }

        binding.btnNegative.setOnClickListener {
          dialog.dismiss()
        }

        binding.btnPositive.setOnClickListener {
          viewModel.exitRoom()
          dialog.dismiss()
        }
      } else finish()
    }
  }

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
      overrideActivityTransition(
        OVERRIDE_TRANSITION_CLOSE,
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
}