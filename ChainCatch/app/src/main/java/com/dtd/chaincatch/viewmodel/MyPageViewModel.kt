package com.dtd.chaincatch.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dtd.chaincatch.ApplicationClass
import com.dtd.chaincatch.model.dto.UserDto
import com.dtd.chaincatch.model.service.UserService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MyPageViewModel : ViewModel() {
  private var auth: FirebaseAuth = Firebase.auth

  private val userDB = Firebase.database.getReference("$USER_DB_KEY/${auth.currentUser!!.uid}")
  private val userService by lazy { ApplicationClass.wRetrofit.create(UserService::class.java) }

  private val _user = MutableLiveData<UserDto>(UserDto())
  val user: LiveData<UserDto> get() = _user

  init {
    viewModelScope.launch {
      _user.value = userDB.get().await().getValue(UserDto::class.java)

      userDB.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          _user.postValue(snapshot.getValue(UserDto::class.java))
        }

        override fun onCancelled(error: DatabaseError) {}
      })
    }
  }

  fun updateUser(nickname: String, profileImg: Int, nftAddress: String) {
    val userDto = UserDto(
      uid = user.value!!.uid,
      nickname = nickname,
      profileImg = profileImg,
      nftAddress = nftAddress
    )
    userDB.setValue(userDto)
  }

  fun deleteUser() {
    viewModelScope.launch {
      userService.deleteUser(UserDto(uid = user.value!!.uid))
    }
  }

  fun logout() {
    auth.signOut()
  }

  companion object {
    const val USER_DB_KEY = "user"
  }
}