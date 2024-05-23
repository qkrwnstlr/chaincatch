package com.dtd.chaincatch.view.fragment

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.dtd.chaincatch.R
import com.dtd.chaincatch.config.BaseFragment
import com.dtd.chaincatch.databinding.FragmentMyPageBinding
import com.dtd.chaincatch.util.ConstValues
import com.dtd.chaincatch.util.SignUpCardView
import com.dtd.chaincatch.viewmodel.MyPageViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.card.MaterialCardView

class MyPageFragment :
  BaseFragment<FragmentMyPageBinding>(FragmentMyPageBinding::bind, R.layout.fragment_my_page) {

  private val viewModel: MyPageViewModel by activityViewModels()

  private lateinit var btnCancel: View
  private lateinit var btnSubmit: View
  private lateinit var catCheese: SignUpCardView
  private lateinit var catGrey: SignUpCardView
  private lateinit var catFish: SignUpCardView
  private lateinit var catRainbow: SignUpCardView
  private var choicedCatId = -1


  private fun initButtons() {
    binding.btnEditSubmit.setOnClickListener {
      viewModel.updateUser(
        binding.etNickname.text.toString(), viewModel.user.value!!.profileImg, ""
      )
    }

    binding.btnDelete.setOnClickListener { showResignDialog() }

    binding.btnLogout.setOnClickListener { showLogOutDialog() }

    binding.framelayoutUser.setOnClickListener() {
      binding.framelayoutUser.setBackgroundColor(Color.parseColor("#4F000000"))
      Handler(Looper.getMainLooper()).postDelayed({
        binding.framelayoutUser.setBackgroundColor(
          getColor(
            requireContext(), android.R.color.transparent
          )
        )
        showEditUserCatDialog()
      }, 50L)

    }
  }

  private fun showEditUserCatDialog() {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.dialog_edit_user_cat, null)
    val alertDialog = AlertDialog.Builder(context, R.style.CustomAlertDialog).setView(view).create()
    btnCancel = view.findViewById(R.id.edit_user_cat_dialog_btn_cancel)
    btnSubmit = view.findViewById(R.id.edit_user_cat_dialog_btn_submit)
    catCheese = view.findViewById(R.id.cat_cheese)
    catGrey = view.findViewById(R.id.cat_grey)
    catFish = view.findViewById(R.id.cat_fish)
    catRainbow = view.findViewById(R.id.cat_rainbow)

    initCats()
    initClickListeners(alertDialog)
    alertDialog.show()
  }

  private fun initCats() {
    setCatImageAndName(catCheese, R.raw.cat_cheese_animated, "치즈", "#68bbe2")
    setCatImageAndName(catGrey, R.raw.cat_grey_animated, "고등어", "#adbca1")
    setCatImageAndName(catFish, R.raw.cat_white_with_fish_animated, "생선", "#c7b39f")
    setCatImageAndName(catRainbow, R.raw.cat_rainbow_animated, "무지개", "#bca1a9")
  }

  private fun setCatImageAndName(
    catCard: SignUpCardView, resId: Int, name: String, bgColor: String
  ) {
    catCard.apply {
      setCatCardBackground(this, bgColor)
      setCatImageResource(resId)
      setCatName(name)
    }
  }

  private fun initClickListeners(alertDialog: AlertDialog) {
    btnCancel.setOnClickListener { alertDialog.dismiss() }
    btnSubmit.setOnClickListener {
      Toast.makeText(context, "${choicedCatId}번 고양이 선택", Toast.LENGTH_SHORT).show()
      viewModel.updateUser(viewModel.user.value!!.nickname, choicedCatId, "")

      setResourceWithGlide(parseProfileImage(choicedCatId), binding.ivUser)
      alertDialog.dismiss()
    }
    catCheese.setOnClickListener { toggleCat(ConstValues.CAT_CHEESE, "#4f8aa6") }
    catGrey.setOnClickListener { toggleCat(ConstValues.CAT_GREY, "#8d9b82") }
    catFish.setOnClickListener { toggleCat(ConstValues.CAT_FISH, "#a09080") }
    catRainbow.setOnClickListener { toggleCat(ConstValues.CAT_RAINBOW, "#9e828b") }
  }

  private fun toggleCat(clickedCatId: Int, clickedBgColor: String) {
    val prevCatCard = getCatCardViewByID(choicedCatId)
    if (prevCatCard != null) setCatCardBackground(
      prevCatCard, getCatCardBgColorUnClicked(choicedCatId)
    )
    choicedCatId = clickedCatId
    setCatCardBackground(getCatCardViewByID(choicedCatId)!!, clickedBgColor)
  }

  private fun setCatCardBackground(catCard: SignUpCardView, bgColor: String) {
    catCard.apply {
      findViewById<MaterialCardView>(R.id.cardview_cat).setCardBackgroundColor(
        Color.parseColor(
          bgColor
        )
      )
    }
  }

  private fun getCatCardViewByID(catId: Int): SignUpCardView? {
    return when (catId) {
      ConstValues.CAT_CHEESE -> catCheese
      ConstValues.CAT_GREY -> catGrey
      ConstValues.CAT_FISH -> catFish
      ConstValues.CAT_RAINBOW -> catRainbow
      else -> null
    }
  }

  private fun getCatCardBgColorUnClicked(catId: Int): String {
    return when (catId) {
      ConstValues.CAT_CHEESE -> "#68bbe2"
      ConstValues.CAT_GREY -> "#adbca1"
      ConstValues.CAT_FISH -> "#c7b39f"
      ConstValues.CAT_RAINBOW -> "#bca1a9"
      else -> ""
    }
  }

  private fun parseProfileImage(profileImage: Int): Int {
    return when (profileImage) {
      ConstValues.CAT_CHEESE -> R.raw.cat_cheese_animated
      ConstValues.CAT_GREY -> R.raw.cat_grey_animated
      ConstValues.CAT_FISH -> R.raw.cat_white_with_fish_animated
      ConstValues.CAT_RAINBOW -> R.raw.cat_rainbow_animated
      else -> R.drawable.cat_no_user_face
    }
  }

  private fun setResourceWithGlide(rawInt: Int, imageView: ImageView) {
    Glide.with(requireContext()).load(rawInt).into(imageView)
  }

  private fun showLogOutDialog() {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.dialog_logout, null)
    val alertDialog = AlertDialog.Builder(context, R.style.CustomAlertDialog).setView(view).create()

    view.findViewById<Button>(R.id.btn_logout_yes).setOnClickListener {
      viewModel.logout()
      val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
      GoogleSignIn.getClient(requireActivity(), gso).signOut()
      requireActivity().finish()
      alertDialog.dismiss()
    }
    view.findViewById<Button>(R.id.btn_logout_no).setOnClickListener {
      alertDialog.dismiss()
    }
    alertDialog.show()
  }

  private fun showResignDialog() {
    val layoutInflater = LayoutInflater.from(context)
    val view = layoutInflater.inflate(R.layout.dialog_resign, null)
    val alertDialog = AlertDialog.Builder(context, R.style.CustomAlertDialog).setView(view).create()

    view.findViewById<Button>(R.id.btn_resign_yes).setOnClickListener {
      viewModel.deleteUser()
      alertDialog.dismiss()
      requireActivity().finish()
    }

    view.findViewById<Button>(R.id.btn_resign_no).setOnClickListener {
      alertDialog.dismiss()
    }
    alertDialog.show()
  }


  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewModel.user.observe(viewLifecycleOwner) {
      if (it == null) return@observe
      binding.etNickname.setText(it.nickname)
      binding.progressBarExperience.progress = it.experience
      setResourceWithGlide(parseProfileImage(it.profileImg), binding.ivUser)
    }
    setResourceWithGlide(R.raw.bg_animated2, binding.ivBackground)

    initButtons()
  }


}