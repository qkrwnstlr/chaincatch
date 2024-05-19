package com.dtd.chaincatch.home.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.dtd.chaincatch.R
import com.dtd.chaincatch.config.BaseFragment
import com.dtd.chaincatch.databinding.FragmentHomeBinding
import com.dtd.chaincatch.home.HomeAdapter
import com.dtd.chaincatch.home.viewmodel.HomeViewModel

class HomeFragment :
  BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::bind, R.layout.fragment_home) {

  companion object {
    fun newInstance() = HomeFragment()
  }

  private lateinit var viewModel: HomeViewModel

  private lateinit var adapter: HomeAdapter
  private val allData: List<RoomDTO> = getSampleData()
  private val pageSize = 4 // 한 페이지에 표시할 항목 수
  private var currentPage = 0

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    Handler(Looper.getMainLooper()).postDelayed({ initRecyclerView() }, 3000)
  }

  private fun initRecyclerView() {
    adapter = HomeAdapter(requireContext(), allData)
    binding.homeFragmentRecyclerView.layoutManager = LinearLayoutManager(context)
    binding.homeFragmentRecyclerView.adapter = adapter
    val animation =
      AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_anim_slide_in_left)
    binding.homeFragmentRecyclerView.layoutAnimation = animation
    binding.homeFragmentRecyclerView.scheduleLayoutAnimation()

    binding.btnPrev.visibility = View.VISIBLE
    binding.btnPrev.startAnimation(
      AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_with_no_offset)
    )

    binding.btnNext.visibility = View.VISIBLE
    binding.btnNext.startAnimation(
      AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_with_no_offset)
    )

    updateRecyclerView()
    initButtons()
  }

  private fun updateRecyclerView() {
    val start = currentPage * pageSize
    val end = Math.min(start + pageSize, allData.size)
    val sublist = allData.subList(start, end)
    adapter.submitList(sublist)
  }

  private fun initButtons() {
    binding.btnPrev.setOnClickListener {
      if (currentPage > 0) {
        currentPage--
        updateRecyclerView()
      } else {
        Toast.makeText(context, "첫 페이지입니다!", Toast.LENGTH_SHORT).show()
      }
    }

    binding.btnNext.setOnClickListener {
      if ((currentPage + 1) * pageSize < allData.size) {
        currentPage++
        updateRecyclerView()
      } else {
        Toast.makeText(context, "마지막 페이지입니다!", Toast.LENGTH_SHORT).show()
      }
    }
  }


  private fun getSampleData(): List<RoomDTO> {
    return listOf(
      RoomDTO(title = "초보만 들어와라", manager = "방장", currentUser = 2),
      RoomDTO(title = "방이름", manager = "방장2", currentUser = 2),
      RoomDTO(title = "방이름", manager = "방장3", currentUser = 4),
      RoomDTO(title = "방이름4", manager = "방장", currentUser = 2),
      RoomDTO(title = "방이름2", manager = "방장1", currentUser = 2),
      RoomDTO(title = "방이름3", manager = "방장d", currentUser = 2),
      RoomDTO(title = "방이름3", manager = "방장d", currentUser = 2),
      RoomDTO(title = "방이름", manager = "방장", currentUser = 2),
      RoomDTO(title = "방이름", manager = "방장s", currentUser = 2),
      RoomDTO(title = "방이름", manager = "방장", currentUser = 2),
      RoomDTO(title = "방이름", manager = "방장", currentUser = 2),
    )
  }

}