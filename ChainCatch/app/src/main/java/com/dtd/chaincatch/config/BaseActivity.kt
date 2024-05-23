package com.dtd.chaincatch.config

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.dtd.chaincatch.util.BgmService
import com.dtd.chaincatch.util.LoadingDialog
import com.dtd.chaincatch.util.SettingsManager


// 액티비티의 기본을 작성, 뷰 바인딩 활용
abstract class BaseActivity<B : ViewBinding>(private val inflate: (LayoutInflater) -> B) :
  AppCompatActivity() {
  protected lateinit var binding: B
    private set
  protected val mLoadingDialog: LoadingDialog by lazy {
    LoadingDialog(this)
  }
  private lateinit var settingsManager: SettingsManager

  override fun onResume() {
    super.onResume()
    setBGM()
    hideSystemUI()
  }

  // 뷰 바인딩 객체를 받아서 inflate해서 화면을 만들어줌.
  // 즉 매번 onCreate에서 setContentView를 하지 않아도 됨.
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)


    binding = inflate(layoutInflater)
    setContentView(binding.root)

    setBGM()
    hideSystemUI()
  }

  fun setBGM() {
    settingsManager = SettingsManager(this)
    Log.d("아아아", "onCreate: ${settingsManager.getSoundSettingVal()}")

    if (settingsManager.getSoundSettingVal()) startBGM()
    else stopBGM()
  }

  // 로딩 다이얼로그, 즉 로딩창을 띄워줌.
  // 네트워크가 시작될 때 사용자가 무작정 기다리게 하지 않기 위해 작성.
  fun showLoadingDialog() {
    if (!mLoadingDialog.isShowing) {
      mLoadingDialog.show()
    }
  }

  // 띄워 놓은 로딩 다이얼로그를 없앰.
  fun dismissLoadingDialog() {
    if (mLoadingDialog.isShowing) {
      mLoadingDialog.dismiss()
    }
  }

  // 토스트를 쉽게 띄울 수 있게 해줌.
  fun showCustomToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
  }

  protected open fun hideSystemUI() {
    supportActionBar?.hide()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      window.setDecorFitsSystemWindows(false)
      val controller = window.insetsController
      if (controller != null) {
        controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
      }
    } else {
      val decorView: View = window.decorView
      decorView.setSystemUiVisibility(
        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
      )
    }

  }

  fun stopBGM() {
    stopService(Intent(applicationContext, BgmService::class.java))
  }

  fun startBGM() {
    startService(Intent(applicationContext, BgmService::class.java))
  }

  override fun onDestroy() {
    stopService(Intent(applicationContext, BgmService::class.java))
    super.onDestroy()
  }

  override fun onUserLeaveHint() {
    stopService(Intent(applicationContext, BgmService::class.java))
    super.onUserLeaveHint()
  }
}