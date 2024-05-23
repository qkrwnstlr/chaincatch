package com.dtd.chaincatch.view.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dtd.chaincatch.R
import com.dtd.chaincatch.config.BaseFragment
import com.dtd.chaincatch.databinding.FragmentHomeBinding
import com.dtd.chaincatch.view.activity.RoomActivity
import com.dtd.chaincatch.view.adapter.HomeAdapter
import com.dtd.chaincatch.viewmodel.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class HomeFragment :
  BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::bind, R.layout.fragment_home) {

  companion object {
    fun newInstance() = HomeFragment()

    const val VISIBILITY_OFFSET = 1000L
    const val BUTTON_CLICKED_DELAY = 100L
    const val COUNT_PER_PAGE = 4 // 한 페이지에 표시할 항목 수
    const val ITEM_HEIGHT_DP = 50 // 한 아이템의 Height (dp)
    const val DIVIDER_HEIGHT_PX = 15 // 구분선 Height (px)

    private const val CAT_CHEESE = 0
    private const val CAT_GREY = 1
    private const val CAT_FISH = 2
    private const val CAT_RAINBOW = 3
  }

  private val viewModel: HomeViewModel by activityViewModels()

  private lateinit var adapter: HomeAdapter
  private val pageSize = COUNT_PER_PAGE
  private var currentPage = 0

  private lateinit var etRoomName: EditText
  private lateinit var btnNewRoomCancel: View
  private lateinit var btnNewRoomSubmit: View
  private lateinit var btnSettingBGM: ImageView
  private lateinit var btnSettingEffectSound: ImageView
  private lateinit var btnSettingHelp: ImageView
  private lateinit var btnSettingMypage: ImageView
  private lateinit var btnSettingCancel: View
  private var isBgbOn = true
  private var isEffectSoundOn = true

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    initAllButtons()
    initRecyclerView()
    initUserInfo()
  }

  private fun initAllButtons() {
    isBgbOn = true
    isEffectSoundOn = true
    setResourceWithGlide(R.raw.bg_animated2, binding.ivBackground)
    setResourceWithGlide(R.raw.btn_prev_animated, binding.btnPrev)
    setResourceWithGlide(R.raw.btn_next_animated, binding.btnNext)
    setResourceWithGlide(R.raw.btn_new_room_animated3, binding.ivBtnNewRoom)
    setResourceWithGlide(R.raw.btn_random_start_animated, binding.ivBtnRandomStart)
    setResourceWithGlide(R.raw.btn_setting_animated6, binding.ivBtnSetting)

    binding.ivBtnNewRoom.setOnClickListener {
      showNewRoomDialog()
    }

    binding.ivBtnRandomStart.setOnClickListener {
      if (!viewModel.enterRandomRoom()) showCustomToast("입장 가능한 방이 없습니다.")
    }

    binding.ivBtnSetting.setOnClickListener {
      showSettingDialog()
    }
  }

  private fun showNewRoomDialog() {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.dialog_new_room, null)
    val alertDialog = AlertDialog.Builder(context, R.style.CustomAlertDialog)
      .setView(view)
      .create()

    etRoomName = view.findViewById(R.id.et_room_name)
    btnNewRoomSubmit = view.findViewById(R.id.new_room_dialog_btn_submit)
    btnNewRoomCancel = view.findViewById(R.id.new_room_dialog_btn_cancel)

    initNewRoomDialogClickListeners(alertDialog)
    alertDialog.show()
  }

  private fun initNewRoomDialogClickListeners(alertDialog: AlertDialog) {
    btnNewRoomCancel.setOnClickListener { alertDialog.dismiss() }

    btnNewRoomSubmit.setOnClickListener {
      val title = etRoomName.text.toString()
      if (title.isBlank()) {
        showCustomToast("빈 문자열은 입력할 수 없습니다.")
      } else {
        viewModel.createRoom(title)
        alertDialog.dismiss()
      }
    }
  }

  private fun showSettingDialog() {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.dialog_setting, null)
    val alertDialog = AlertDialog.Builder(context, R.style.CustomAlertDialog)
      .setView(view)
      .create()

    btnSettingBGM = view.findViewById(R.id.btn_bgm)
    btnSettingEffectSound = view.findViewById(R.id.btn_effect_sound)
    btnSettingHelp = view.findViewById(R.id.btn_help)
    btnSettingMypage = view.findViewById(R.id.btn_mypage)
    btnSettingCancel = view.findViewById(R.id.setting_dialog_btn_cancel)

    initSettingDialogClickListeners(alertDialog)
    alertDialog.show()
  }

  private fun initSettingDialogClickListeners(alertDialog: AlertDialog) {
    btnSettingCancel.setOnClickListener {
      alertDialog.dismiss()
    }

    btnSettingBGM.setOnClickListener {
      toggleBGM()
    }
    btnSettingEffectSound.setOnClickListener { toggleEffectSound() }
    btnSettingHelp.setOnClickListener {
      Toast.makeText(context, "help", Toast.LENGTH_SHORT).show()
    }

    btnSettingMypage.setOnClickListener {
      alertDialog.dismiss()

      parentFragmentManager.beginTransaction()
        .setReorderingAllowed(true)
        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
        .replace(R.id.home_fragment_container, MyPageFragment())
        .addToBackStack(null)
        .commit()

    }
  }

  private fun toggleBGM() {
    if (isBgbOn) {
      btnSettingBGM.setImageResource(R.drawable.ic_bgm_off)
      // TODO : bgm stop
    } else {
      btnSettingBGM.setImageResource(R.drawable.ic_bgm_on)
      // TODO : bgm start
    }
    isBgbOn = !isBgbOn
  }

  private fun toggleEffectSound() {
    if (isEffectSoundOn) {
      btnSettingEffectSound.setImageResource(R.drawable.ic_effect_sound_off)
      // TODO : effect sound stop
    } else {
      btnSettingEffectSound.setImageResource(R.drawable.ic_effect_sound_on)
      // TODO : effect sound start
    }
    isEffectSoundOn = !isEffectSoundOn
  }

  private fun setResourceWithGlide(rawInt: Int, imageView: ImageView) {
    Glide.with(requireContext())
      .load(rawInt)
      .into(imageView)
  }

  private fun initUserInfo() {
    viewModel.userInfo.observe(viewLifecycleOwner) {
      binding.ivUser.setOnClickListener {
        parentFragmentManager.beginTransaction()
          .setReorderingAllowed(true)
          .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
          .replace(R.id.home_fragment_container, MyPageFragment())
          .addToBackStack(null)
          .commit()
      }

      if (it == null) return@observe
      when (it.profileImg) {
        CAT_CHEESE -> binding.ivUser.setImageResource(R.drawable.cat_cheese_face)
        CAT_GREY -> binding.ivUser.setImageResource(R.drawable.cat_grey_face)
        CAT_FISH -> binding.ivUser.setImageResource(R.drawable.cat_fish_face)
        CAT_RAINBOW -> binding.ivUser.setImageResource(R.drawable.cat_rainbow_face)
      }

      binding.tvNickname.text = it.nickname
      binding.progressBarExperience.progress = it.experience

      val intent = Intent(requireContext(), RoomActivity::class.java)
      if (it.currentRid != null) startActivity(intent)
    }
  }

  private fun initRecyclerView() {
    adapter = HomeAdapter(requireContext(), viewModel.roomDtoList.value!!).apply {
      setOnItemClickListener {
        if (it.currentUser < it.maxUser) viewModel.enterRoom(it.rid)
        else AlertDialog.Builder(requireContext()).setMessage("사용자가 가득차서 들어갈 수 없습니다.").show()
      }
    }
    binding.homeFragmentRecyclerView.layoutManager = object : LinearLayoutManager(context) {
      override fun canScrollVertically() = false
    }
    binding.homeFragmentRecyclerView.adapter = adapter

    setRecyclerViewHeight()

    val animation =
      AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_anim_slide_in_left)
    binding.homeFragmentRecyclerView.layoutAnimation = animation
    viewLifecycleOwner.lifecycleScope.launch {
      delay(300)
      binding.homeFragmentRecyclerView.scheduleLayoutAnimation()
    }

    updateRecyclerView()
    setVisibilityForAllButtons()
    initPrevAndNextButtons()

    viewModel.roomDtoList.observe(viewLifecycleOwner) {
      updateRecyclerView()
    }
  }

  private fun setRecyclerViewHeight() {
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
    val end = Math.min(start + pageSize, viewModel.roomDtoList.value!!.size)
    val sublist = viewModel.roomDtoList.value!!.subList(start, end)
    adapter.submitList(sublist)
  }

  private fun setVisibilityForAllButtons() {
    viewLifecycleOwner.lifecycleScope.launch {
      delay(VISIBILITY_OFFSET)
      with(binding) {
        btnPrev.visibility = View.VISIBLE
        btnNext.visibility = View.VISIBLE
        ivBtnNewRoom.visibility = View.VISIBLE
      }
      delay(VISIBILITY_OFFSET - 700)
      binding.ivBtnRandomStart.visibility = View.VISIBLE
      delay(VISIBILITY_OFFSET - 700)
      binding.ivBtnSetting.visibility = View.VISIBLE
    }
  }

  private fun initPrevAndNextButtons() {
    // when prev button clicked
    binding.btnPrev.setOnClickListener {
      // twinkle
      setResourceWithGlide(R.drawable.btn_prev_clicked, binding.btnPrev)
      Handler(Looper.getMainLooper()).postDelayed({
        setResourceWithGlide(R.raw.btn_prev_animated, binding.btnPrev)
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
      setResourceWithGlide(R.drawable.btn_next_clicked, binding.btnNext)
      Handler(Looper.getMainLooper()).postDelayed({
        setResourceWithGlide(R.raw.btn_next_animated, binding.btnNext)
      }, BUTTON_CLICKED_DELAY)

      // last page handling
      if ((currentPage + 1) * pageSize < viewModel.roomDtoList.value!!.size) {
        currentPage++
        updateRecyclerView()
      } else {
        Toast.makeText(context, "마지막 페이지입니다!", Toast.LENGTH_SHORT).show()
      }
    }
  }
}
