package com.dtd.chaincatch.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dtd.chaincatch.ApplicationClass
import com.dtd.chaincatch.home.model.dto.RoomActionDto
import com.dtd.chaincatch.home.model.dto.RoomDto
import com.dtd.chaincatch.home.model.service.UserService
import com.dtd.chaincatch.user.model.dto.UserDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "HomeViewModel_싸피"

class HomeViewModel : ViewModel() {
  private var auth: FirebaseAuth = Firebase.auth

  private val userDB: DatabaseReference =
    Firebase.database.getReference("$USER_DB_KEY/${auth.currentUser!!.uid}")
  private val roomDB: DatabaseReference = Firebase.database.getReference(ROOM_DB_KEY)

  private val userService by lazy { ApplicationClass.wRetrofit.create(UserService::class.java) }

  private val _roomDtoList = MutableLiveData<List<RoomDto>>(mutableListOf())
  val roomDtoList: LiveData<List<RoomDto>> get() = _roomDtoList

  private val _userInfo = MutableLiveData<UserDto?>()
  val userInfo: LiveData<UserDto?> get() = _userInfo

  init {
    userDB.addValueEventListener(object : ValueEventListener {
      override fun onDataChange(snapshot: DataSnapshot) {
        val userDto = snapshot.getValue(UserDto::class.java)
        _userInfo.postValue(userDto)
      }

      override fun onCancelled(error: DatabaseError) {}
    })

    Firebase.database.getReference(".info/connected")
      .addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          if (snapshot.value != false) return
          userDB.child("isOnline").onDisconnect().setValue(false)
        }

        override fun onCancelled(error: DatabaseError) {}
      })

    viewModelScope.launch {
      _roomDtoList.postValue(
        roomDB.get().await()
          .getValue(object : GenericTypeIndicator<HashMap<String, RoomDto>>() {})?.values?.toList()
          ?: listOf()
      )

      roomDB.addChildEventListener(object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
          val roomDTO = snapshot.getValue(RoomDto::class.java) ?: return
          if (_roomDtoList.value?.contains(roomDTO) == true) return
          _roomDtoList.postValue(_roomDtoList.value?.toMutableList()?.apply { add(roomDTO) })
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
          val roomDTO = snapshot.getValue<RoomDto>() ?: return
          _roomDtoList.postValue(_roomDtoList.value?.toMutableList()?.apply {
            this[indexOf(roomDTO)] = roomDTO
          })
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
          val roomDTO = snapshot.getValue<RoomDto>() ?: return
          _roomDtoList.postValue(_roomDtoList.value?.toMutableList()?.apply {
            removeAt(indexOf(roomDTO))
          })
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onCancelled(error: DatabaseError) {}
      })

    }
  }

  fun createRoom(roomDTO: RoomDto) {
    viewModelScope.launch {
      userService.createRoom(roomDTO)
    }
  }

  fun enterRoom(rid: String) {
    viewModelScope.launch {
      userService.enterRoom(RoomActionDto(rid = rid, uid = userInfo.value!!.uid))
    }
  }

  companion object {
    const val ROOM_DB_KEY = "room"
    const val USER_DB_KEY = "user"
  }
}