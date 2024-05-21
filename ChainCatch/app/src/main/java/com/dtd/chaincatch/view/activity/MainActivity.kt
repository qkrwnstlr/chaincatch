package com.dtd.chaincatch.view.activity

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dtd.chaincatch.ApplicationClass
import com.dtd.chaincatch.R
import com.dtd.chaincatch.config.BaseActivity
import com.dtd.chaincatch.databinding.ActivityMainBinding
import com.dtd.chaincatch.model.dto.UserDto
import com.dtd.chaincatch.model.service.UserService
import com.dtd.chaincatch.util.SignUpCardView
import com.dtd.chaincatch.viewmodel.HomeViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
  private lateinit var auth: FirebaseAuth
  private lateinit var googleSignInClient: GoogleSignInClient

  private val userService by lazy { ApplicationClass.wRetrofit.create(UserService::class.java) }

  private var firebaseAuthResult =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      when (result.resultCode) {
        RESULT_OK -> {
          val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
          try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
          } catch (e: ApiException) {
            showCustomToast("로그인에 실패하였습니다.")
          }
        }
      }
    }

  private lateinit var dialogTitle: TextView
  private lateinit var dialogTitleShadow: TextView
  private lateinit var catCheese: SignUpCardView
  private lateinit var layoutCats: View
  private lateinit var layoutNickName: View
  private lateinit var layoutNextButton: View
  private lateinit var catGrey: SignUpCardView
  private lateinit var catFish: SignUpCardView
  private lateinit var catRainbow: SignUpCardView
  private lateinit var btnCancel: View
  private lateinit var btnSubmit: View
  private lateinit var etNickname: EditText
  private var choicedCatId = -1

  private fun initFirebaseAuth() {
    auth = FirebaseAuth.getInstance()

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestIdToken(getString(R.string.default_web_client_id))
      .requestEmail()
      .build()
    googleSignInClient = GoogleSignIn.getClient(this, gso)
  }

  private fun firebaseAuthWithGoogle(idToken: String) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
      if (task.isSuccessful) checkUserInfo()
      else showCustomToast("로그인에 실패하였습니다.")
    }
  }

  private fun signIn() {
    val signInIntent = googleSignInClient.signInIntent
    firebaseAuthResult.launch(signInIntent)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initFirebaseAuth()
    initView()

    auth.signOut()
    googleSignInClient.signOut()
  }

  private fun checkUserInfo() {
    val userDB: DatabaseReference = Firebase.database.getReference(
      "${HomeViewModel.USER_DB_KEY}/${auth.currentUser!!.uid}"
    )

    lifecycleScope.launch {
      val userSnapshot = userDB.get().await()

      if (!userSnapshot.exists()) {
        showSignUpDialog()
      } else {
        userDB.child("isOnline").setValue(true)
      }
    }

    userDB.child("isOnline").addValueEventListener(object : ValueEventListener {
      override fun onDataChange(snapshot: DataSnapshot) {
        val isOnline = snapshot.getValue(Boolean::class.java) ?: false
        if (isOnline) startNextActivity()
      }

      override fun onCancelled(error: DatabaseError) {}
    })
  }

  private fun showSignUpDialog() {
    val layoutInflater = LayoutInflater.from(this@MainActivity)
    val view = layoutInflater.inflate(R.layout.dialog_sign_up, null)
    val alertDialog = AlertDialog.Builder(this@MainActivity, R.style.CustomAlertDialog)
      .setView(view)
      .create()

    dialogTitle = view.findViewById(R.id.tv_title)
    dialogTitleShadow = view.findViewById(R.id.tv_title_shadow)
    layoutCats = view.findViewById(R.id.layout_cats)
    layoutNextButton = view.findViewById(R.id.sign_up_layout_next_btns)
    layoutNickName = view.findViewById(R.id.sign_up_layout_nickname)
    catCheese = view.findViewById(R.id.cat_cheese)
    catGrey = view.findViewById(R.id.cat_grey)
    catFish = view.findViewById(R.id.cat_fish)
    catRainbow = view.findViewById(R.id.cat_rainbow)
    btnCancel = view.findViewById(R.id.sign_up_dialog_btn_cancel)
    btnSubmit = view.findViewById(R.id.sign_up_dialog_btn_submit)
    etNickname = view.findViewById(R.id.et_nickname)

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
    catCard: SignUpCardView,
    resId: Int,
    name: String,
    bgColor: String
  ) {
    catCard.apply {
      setCatCardBackground(this, bgColor)
      setCatImageResource(resId)
      setCatName(name)
    }
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
      CAT_CHEESE -> catCheese
      CAT_GREY -> catGrey
      CAT_FISH -> catFish
      CAT_RAINBOW -> catRainbow
      else -> null
    }
  }

  private fun getCatCardBgColorUnClicked(catId: Int): String {
    return when (catId) {
      CAT_CHEESE -> "#68bbe2"
      CAT_GREY -> "#adbca1"
      CAT_FISH -> "#c7b39f"
      CAT_RAINBOW -> "#bca1a9"
      else -> ""
    }
  }

  private fun toggleCat(clickedCatId: Int, clickedBgColor: String) {
    val prevCatCard = getCatCardViewByID(choicedCatId)

    // 이전에 선택된 애
    if (prevCatCard != null) setCatCardBackground(
      prevCatCard,
      getCatCardBgColorUnClicked(choicedCatId)
    )
    // 지금 선택된 애
    choicedCatId = clickedCatId
    setCatCardBackground(getCatCardViewByID(choicedCatId)!!, clickedBgColor)
  }

  private fun initClickListeners(alertDialog: AlertDialog) {
    // 우상단 취소 버튼 눌렀을 때
    btnCancel.setOnClickListener { alertDialog.dismiss() }

    // 냥이 선택할 때
    catCheese.setOnClickListener { toggleCat(CAT_CHEESE, "#4f8aa6") }
    catGrey.setOnClickListener { toggleCat(CAT_GREY, "#8d9b82") }
    catFish.setOnClickListener { toggleCat(CAT_FISH, "#a09080") }
    catRainbow.setOnClickListener { toggleCat(CAT_RAINBOW, "#9e828b") }

    // 냥이 선택 화면에서 우하단 다음 버튼 눌렀을 때
    layoutNextButton.setOnClickListener {
      dialogTitle.text = "닉네임을 정해주세요!"
      dialogTitleShadow.text = "닉네임을 정해주세요!"
      layoutCats.visibility = View.GONE
      layoutNextButton.visibility = View.GONE
      layoutNickName.visibility = View.VISIBLE
    }

    // 닉네임 입력 화면에서 확인 버튼 눌렀을 때
    btnSubmit.setOnClickListener {
      lifecycleScope.launch {
        userService.createUser(
          UserDto(
            uid = auth.currentUser!!.uid,
            profileImg = choicedCatId,
            nickname = etNickname.text.toString()
          )
        )
      }
    }
  }

  private fun startNextActivity() {
    val intent = Intent(this, HomeActivity::class.java)
    startActivity(intent)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      overrideActivityTransition(
        OVERRIDE_TRANSITION_OPEN,
        R.anim.fade_in,
        R.anim.fade_out,
        Color.BLACK
      )
      overrideActivityTransition(
        OVERRIDE_TRANSITION_CLOSE,
        R.anim.fade_out,
        R.anim.fade_in,
        Color.BLACK
      )
    } else {
      overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
  }

  private fun initView() {
    // Set Title
    Glide.with(this)
      .load(R.raw.title_animated)
      .into(binding.ivTitle)

    // Set Start Button
    Glide.with(this)
      .load(R.raw.btn_start_animated_slow)
      .into(binding.btnStart)

    binding.btnStart.setOnClickListener {
      if (auth.currentUser == null) signIn()
      else checkUserInfo()
    }
  }

  companion object {
    private const val CAT_CHEESE = 0
    private const val CAT_GREY = 1
    private const val CAT_FISH = 2
    private const val CAT_RAINBOW = 3
  }
}