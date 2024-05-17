package com.dtd.chaincatch

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dtd.chaincatch.databinding.ActivityMainBinding
import com.dtd.chaincatch.home.HomeActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
  private lateinit var auth: FirebaseAuth
  private lateinit var googleSignInClient: GoogleSignInClient

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

class MainActivity : AppCompatActivity() {
  private var _binding: ActivityMainBinding? = null
  private val binding get() = _binding!!

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initFirebaseAuth()
    initView()

    binding.btnStart.setOnClickListener {
//      startActivity(Intent(this, HomeActivity::class.java))
      startNextActivity()
    }
  }

  private fun startNextActivity() {
    val intent = Intent(this, HomeActivity::class.java)
    val options = ActivityOptions.makeCustomAnimation(
      this,
      R.anim.slide_in_bottom, R.anim.slide_out_top
    ).toBundle()
    startActivity(intent, options)
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
      else navigateToHomeActivity()
    }
  }
}