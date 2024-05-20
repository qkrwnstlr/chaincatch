package com.dtd.chaincatch.home.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dtd.chaincatch.R
import com.dtd.chaincatch.config.BaseFragment
import com.dtd.chaincatch.databinding.FragmentHomeBinding
import com.dtd.chaincatch.home.HomeAdapter
import com.dtd.chaincatch.home.viewmodel.HomeViewModel
import kotlin.math.roundToInt

private const val TAG = "HomeFragment_싸피"

class HomeFragment :
  BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::bind, R.layout.fragment_home) {

  companion object {
    fun newInstance() = HomeFragment()

    private const val RECYCLER_VIEW_DELAY = 1000L
    private const val BUTTON_CLICKED_DELAY = 100L
    private const val COUNT_PER_PAGE = 4 // 한 페이지에 표시할 항목 수
    private const val ITEM_HEIGHT_DP = 50 // 한 아이템의 Height (dp)
    private const val DIVIDER_HEIGHT_PX = 15 // 구분선 Height (px)
  }

  private val viewModel: HomeViewModel by activityViewModels()

  private lateinit var adapter: HomeAdapter
  private val allData: List<RoomDTO> = getSampleData()
  private val pageSize = COUNT_PER_PAGE
  private var currentPage = 0

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

//    Handler(Looper.getMainLooper()).postDelayed({ initRecyclerView() }, RECYCLER_VIEW_DELAY)
    initRecyclerView()

    viewModel.roomDTOList.observe(viewLifecycleOwner) {
      updateRecyclerView()
    }
  }

  private fun initRecyclerView() {
    adapter = HomeAdapter(requireContext(), viewModel.roomDTOList.value!!)
    binding.homeFragmentRecyclerView.layoutManager = LinearLayoutManager(context)
    binding.homeFragmentRecyclerView.adapter = adapter

    setRecyclerViewHeight()

    val animation =
      AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_anim_slide_in_left)
    binding.homeFragmentRecyclerView.layoutAnimation = animation
    binding.homeFragmentRecyclerView.scheduleLayoutAnimation()

    updateRecyclerView()
    initButtons()
  }

  fun setRecyclerViewHeight() {
    val itemHeightDp = ITEM_HEIGHT_DP
    val itemHeightPx = TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      itemHeightDp.toFloat(),
      resources.displayMetrics
    ).roundToInt()
    val dividerHeightPx = DIVIDER_HEIGHT_PX
    val recyclerViewHeight =
      calculateRecyclerViewHeight(
        itemHeightPx,
        pageSize,
        binding.homeFragmentRecyclerView,
        dividerHeightPx
      )

    val layoutParams = binding.homeFragmentRecyclerView.layoutParams
    layoutParams.height = recyclerViewHeight
//    Log.d(TAG, "setRecyclerViewHeight: $recyclerViewHeight")
    binding.homeFragmentRecyclerView.layoutParams = layoutParams
  }

  private fun calculateRecyclerViewHeight(
    itemHeightPx: Int,
    itemCount: Int,
    recyclerView: RecyclerView,
    dividerHeightPx: Int
  ): Int {
    val paddingTop = recyclerView.paddingTop
    val paddingBottom = recyclerView.paddingBottom
    val totalDividerHeight = dividerHeightPx * (itemCount - 1)
    return (itemHeightPx * itemCount) + paddingTop + paddingBottom + totalDividerHeight
  }

  private fun updateRecyclerView() {
    val start = currentPage * pageSize
    val end = Math.min(start + pageSize, viewModel.roomDTOList.value!!.size)
    val sublist = viewModel.roomDTOList.value!!.subList(start, end)
    adapter.submitList(sublist)
  }

  private fun initButtons() {
    // button initial animation : fade in
    binding.btnPrev.visibility = View.VISIBLE
    binding.btnPrev.startAnimation(
      AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_with_no_offset)
    )

    binding.btnNext.visibility = View.VISIBLE
    binding.btnNext.startAnimation(
      AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_with_no_offset)
    )

    // when prev button clicked
    binding.btnPrev.setOnClickListener {
      // twinkle
      binding.btnPrev.setBackgroundResource(R.drawable.btn_prev3_clicked2)
      Handler(Looper.getMainLooper()).postDelayed({
        binding.btnPrev.setBackgroundResource(R.drawable.btn_prev3)
      }, BUTTON_CLICKED_DELAY)

      // first page handling
      if (currentPage > 0) {
        currentPage--
        updateRecyclerView()
      } else {
        Toast.makeText(context, "첫 페이지입니다!", Toast.LENGTH_SHORT).show()
      }
    }

    // when next button clicked
    binding.btnNext.setOnClickListener {
      // twinkle
      binding.btnNext.setBackgroundResource(R.drawable.btn_next3_clicked2)
      Handler(Looper.getMainLooper()).postDelayed({
        binding.btnNext.setBackgroundResource(R.drawable.btn_next3)
      }, BUTTON_CLICKED_DELAY)

      // last page handling
      if ((currentPage + 1) * pageSize < viewModel.roomDTOList.value!!.size) {
        currentPage++
        updateRecyclerView()
      } else {
        Toast.makeText(context, "마지막 페이지입니다!", Toast.LENGTH_SHORT).show()
      }
    }
  }
}