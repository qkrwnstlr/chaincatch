package com.dtd.chaincatch

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dtd.chaincatch.config.BaseActivity
import com.dtd.chaincatch.databinding.ActivityMainBinding
import com.dtd.chaincatch.home.HomeActivity
import com.dtd.chaincatch.home.model.service.UserService
import com.dtd.chaincatch.home.viewmodel.HomeViewModel
import com.dtd.chaincatch.user.model.dto.UserDto
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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

private const val TAG = "MainActivity_싸피"

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
        // TODO : 회원가입 다이얼로그 띄우기
        showSignUpdialog()

        showCustomToast("다이얼로그 띄우기")
        lifecycleScope.launch {
          userService.createUser(UserDto(uid = auth.currentUser!!.uid, nickname = "nickname"))
        }
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

  private fun showSignUpdialog() {
//    val layoutInflater = LayoutInflater.from(this@MainActivity)
//    val view = layoutInflater.inflate(R.layout.alert_dialog, null)
//
//    val alertDialog = AlertDialog.Builder(this@MainActivity)
//      .setView(view)
//      .create()
//
//    val textTitle = view.findViewById<TextView>(R.id.text_title)
//    val textSubtitle = view.findViewById<TextView>(R.id.text_subtitle)
//    val buttonConfirm = view.findViewById<TextView>(R.id.button_confirm)
//    val buttonClose = view.findViewById<View>(R.id.button_close)
//
//    textTitle.text = "로그인 해볼까요?"
//    textSubtitle.text = "로그인 후 사용할 수 있는 기능입니다."
//    buttonConfirm.text = "로그인 하기"
//    buttonClose.setOnClickListener {
//      alertDialog.dismiss()
//    }
//
//    alertDialog.show()
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
    Glide
      .with(this)
      .load(R.raw.title_animated)
      .into(binding.ivTitle)

    // Set Start Button
    Glide
      .with(this)
      .load(R.raw.btn_start_animated_slow)
      .into(binding.btnStart)

    binding.btnStart.setOnClickListener {
      if (auth.currentUser == null) signIn()
      else checkUserInfo()
    }
  }
}