package com.dtd.chaincatch.home.fragment

import android.os.Bundle
import android.view.View
import com.dtd.chaincatch.R
import com.dtd.chaincatch.config.BaseFragment
import com.dtd.chaincatch.databinding.FragmentHomeBinding
import com.dtd.chaincatch.home.viewmodel.HomeViewModel

class HomeFragment :
  BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::bind, R.layout.fragment_home) {

  companion object {
    fun newInstance() = HomeFragment()
  }

  private lateinit var viewModel: HomeViewModel

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

  }


}