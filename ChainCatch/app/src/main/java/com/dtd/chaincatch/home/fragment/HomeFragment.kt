package com.dtd.chaincatch.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dtd.chaincatch.databinding.FragmentHomeBinding
import com.dtd.chaincatch.home.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

  private var _binding: FragmentHomeBinding? = null
  private val binding get() = _binding!!

  companion object {
    fun newInstance() = HomeFragment()
  }

  private lateinit var viewModel: HomeViewModel

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentHomeBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    
  }

  override fun onDestroy() {
    super.onDestroy()
    _binding = null
  }

}