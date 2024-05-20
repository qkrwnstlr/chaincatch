package com.dtd.chaincatch.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dtd.chaincatch.home.model.dto.RoomDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

private const val TAG = "HomeViewModel_싸피"

class HomeViewModel : ViewModel() {
  private var auth: FirebaseAuth = Firebase.auth

  private var roomDB: DatabaseReference = Firebase.database.getReference(DB_KEY)

  private var _roomDTOList = MutableLiveData<List<RoomDTO>>(mutableListOf())
  val roomDTOList: LiveData<List<RoomDTO>> get() = _roomDTOList

  init {
    roomDB.addChildEventListener(object : ChildEventListener {
      override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
        val roomDTO = snapshot.getValue(RoomDTO::class.java) ?: return
        Log.d(TAG, "onChildAdded: $roomDTO")

        _roomDTOList.value = _roomDTOList.value?.toMutableList()?.apply { add(roomDTO) }
      }

      override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
        val roomDTO = snapshot.getValue<RoomDTO>() ?: return
        _roomDTOList.value = _roomDTOList.value?.toMutableList()?.apply { this[indexOf(roomDTO)] = roomDTO }
      }

      override fun onChildRemoved(snapshot: DataSnapshot) {
        val roomDTO = snapshot.getValue<RoomDTO>() ?: return
        _roomDTOList.value = _roomDTOList.value?.toMutableList()?.apply { removeAt(indexOf(roomDTO)) }
      }

      override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

      override fun onCancelled(error: DatabaseError) {}
    })
  }

  fun createRoom(roomDTO: RoomDTO) {

  }

  fun enterRoom(rid: String) {

  }

  companion object {
    const val DB_KEY = "room"
  }
}