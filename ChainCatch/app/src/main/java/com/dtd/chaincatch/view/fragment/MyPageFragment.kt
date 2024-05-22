package com.dtd.chaincatch.view.fragment

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.dtd.chaincatch.R
import com.dtd.chaincatch.config.BaseFragment
import com.dtd.chaincatch.databinding.FragmentMyPageBinding
import com.dtd.chaincatch.model.dto.UserDto
import com.dtd.chaincatch.viewmodel.MyPageViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class MyPageFragment :
  BaseFragment<FragmentMyPageBinding>(FragmentMyPageBinding::bind, R.layout.fragment_my_page) {

  private val viewModel: MyPageViewModel by activityViewModels()

  private fun initButtons() {
    binding.btnEdit.setOnClickListener {
      // TODO : 다이얼로그 추가
      viewModel.updateUser("춘시기", 1, "")
    }

    binding.btnDelete.setOnClickListener {
      // TODO : 다이얼로그 추가
      viewModel.deleteUser()
    }

    binding.btnLogout.setOnClickListener {
      // TODO : 다이얼로그 추가
      viewModel.logout()
      val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
      GoogleSignIn.getClient(requireActivity(), gso).signOut()
      requireActivity().finish()
    }
  }

  private fun parseProfileImage(profileImage: Int): Int {
    return when (profileImage) {
      CAT_CHEESE -> R.drawable.cat_cheese_face
      CAT_GREY -> R.drawable.cat_grey_face
      CAT_FISH -> R.drawable.cat_fish_face
      CAT_RAINBOW -> R.drawable.cat_rainbow_face
      else -> R.drawable.cat_cheese_face
    }
  }

  private fun setResourceWithGlide(rawInt: Int, imageView: ImageView) {
    Glide.with(requireContext())
      .load(rawInt)
      .into(imageView)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.user.observe(viewLifecycleOwner) {
      if (it == null) return@observe
      binding.userInfo.setUerNickname(it.nickname)
      binding.userInfo.setUserExperience(it.experience)
      binding.userInfo.setUserImage(parseProfileImage(it.profileImg))
    }

    setResourceWithGlide(R.raw.bg_animated2, binding.ivBackground)
    initButtons()
  }

  companion object {
    private const val CAT_CHEESE = 0
    private const val CAT_GREY = 1
    private const val CAT_FISH = 2
    private const val CAT_RAINBOW = 3
  }
}