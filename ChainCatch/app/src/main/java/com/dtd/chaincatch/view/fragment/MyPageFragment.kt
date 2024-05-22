package com.dtd.chaincatch.view.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.dtd.chaincatch.R
import com.dtd.chaincatch.config.BaseFragment
import com.dtd.chaincatch.databinding.FragmentMyPageBinding
import com.dtd.chaincatch.viewmodel.MyPageViewModel

class MyPageFragment :
  BaseFragment<FragmentMyPageBinding>(FragmentMyPageBinding::bind, R.layout.fragment_my_page) {

  private val viewModel: MyPageViewModel by activityViewModels()

  private fun parseProfileImage(profileImage: Int): Int {
    return when (profileImage) {
      CAT_CHEESE -> R.drawable.cat_cheese_face
      CAT_GREY -> R.drawable.cat_grey_face
      CAT_FISH -> R.drawable.cat_fish_face
      CAT_RAINBOW -> R.drawable.cat_rainbow_face
      else -> R.drawable.cat_cheese_face
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.user.observe(viewLifecycleOwner) {
      if (it == null) return@observe
      binding.userInfo.setUerNickname(it.nickname)
      binding.userInfo.setUserExperience(it.experience)
      binding.userInfo.setUserImage(parseProfileImage(it.profileImg))
    }
  }

  companion object {
    private const val CAT_CHEESE = 0
    private const val CAT_GREY = 1
    private const val CAT_FISH = 2
    private const val CAT_RAINBOW = 3
  }
}