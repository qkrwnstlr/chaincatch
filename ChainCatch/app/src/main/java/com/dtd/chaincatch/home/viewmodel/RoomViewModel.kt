package com.dtd.chaincatch.home.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dtd.chaincatch.home.model.dto.Room
import com.google.android.play.integrity.internal.f
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class RoomViewModel {
  private var auth: FirebaseAuth = Firebase.auth

  private var roomDB: DatabaseReference = Firebase.database.getReference("message")
  private var roomRef: DatabaseReference? = null
    set(value) {
      field = value
      if(field == null) {
        _currentRoom.value = null
      } else {
        field?.get()?.addOnSuccessListener {
          _currentRoom.value = it.getValue(Room::class.java)
        }
      }
    }

  private var _roomList = MutableLiveData<List<Room>>(mutableListOf())
  val roomList: LiveData<List<Room>> get() = _roomList

  private var _currentRoom = MutableLiveData<Room?>()
  val currentRoom: LiveData<Room?> get() = _currentRoom

  init {
    roomDB.addChildEventListener(object : ChildEventListener {
      override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
        val room = snapshot.getValue<Room>() ?: return
        _roomList.value = _roomList.value?.toMutableList()?.apply { add(room) }
      }

      override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
        val room = snapshot.getValue<Room>() ?: return
        _roomList.value = _roomList.value?.toMutableList()?.apply { this[indexOf(room)] = room }
      }

      override fun onChildRemoved(snapshot: DataSnapshot) {
        val room = snapshot.getValue<Room>() ?: return
        _roomList.value = _roomList.value?.toMutableList()?.apply { removeAt(indexOf(room)) }
      }

      override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

      override fun onCancelled(error: DatabaseError) {}
    })
  }

  fun createRoom(room: Room) {
    roomRef = roomDB.push().apply {
      roomDB.setValue("title", room.title)
      roomDB.setValue("manager", room.manager)
      roomDB.setValue("maxUser", room.maxUser)
      roomDB.setValue("currentUser", room.currentUser)
    }
  }

  fun enterRoom(rid: String) {
    roomRef = roomDB.child(rid)
  }

  fun exitRoom() {
    roomRef = null
  }

  companion object {
    const val DB_KEY = "room"
  }
}