package com.dtd.chaincatch

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dtd.chaincatch.databinding.ActivityMainBinding
import com.dtd.chaincatch.home.HomeActivity

class MainActivity : AppCompatActivity() {
  private var _binding: ActivityMainBinding? = null
  private val binding get() = _binding!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    _binding = ActivityMainBinding.inflate(layoutInflater)
    window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
    window.exitTransition = null
    setContentView(binding.root)

    initView()

    binding.btnStart.setOnClickListener {
//      startActivity(Intent(this, HomeActivity::class.java))
      startNextActivity()
    }
  }

  private fun startNextActivity() {
    val intent = Intent(this, HomeActivity::class.java)
    val options = ActivityOptions.makeCustomAnimation(
      this,
      R.anim.slide_in_bottom, R.anim.slide_out_top
    ).toBundle()
    startActivity(intent, options)
  }

  private fun initView() {

    // Set Title
    Glide
      .with(this)
      .load(R.raw.title_animated)
      .into(binding.ivTitle)

    // Set Start Button
    Glide
      .with(this)
      .load(R.raw.btn_start_animated_slow)
      .into(binding.btnStart)
  }

  override fun onDestroy() {
    super.onDestroy()
    _binding = null
  }
}