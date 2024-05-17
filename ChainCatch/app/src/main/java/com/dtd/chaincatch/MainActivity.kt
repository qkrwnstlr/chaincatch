package com.dtd.chaincatch

import android.content.Intent
import android.os.Bundle
import android.view.View
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
    setContentView(binding.root)

    initView()
    binding.btnStart.setOnClickListener {
      startActivity(Intent(this, HomeActivity::class.java))
    }
  }

  private fun initView() {
    // Hide Navigation Bar
    window.decorView.apply {
      systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
    }

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
}