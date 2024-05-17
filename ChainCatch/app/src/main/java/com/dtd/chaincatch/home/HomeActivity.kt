package com.dtd.chaincatch.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dtd.chaincatch.R
import com.dtd.chaincatch.databinding.ActivityHomeBinding
import com.dtd.chaincatch.drawing.fragment.DrawingFragment

class HomeActivity : AppCompatActivity() {
  private var _binding: ActivityHomeBinding? = null
  private val binding get() = _binding!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    _binding = ActivityHomeBinding.inflate(layoutInflater)
    setContentView(binding.root)

    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, DrawingFragment())
      .commit()


  }
}