package com.dtd.chaincatch.room.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dtd.chaincatch.ApplicationClass
import com.dtd.chaincatch.home.model.dto.RoomActionDto
import com.dtd.chaincatch.home.model.dto.RoomDto
import com.dtd.chaincatch.home.model.service.UserService
import com.dtd.chaincatch.room.model.dto.ChattingDto
import com.dtd.chaincatch.room.model.dto.QuestionDto
import com.dtd.chaincatch.room.model.dto.RoundDto
import com.dtd.chaincatch.user.model.dto.UserDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RoomViewModel : ViewModel() {
  private var auth: FirebaseAuth = Firebase.auth

  private val userDB = Firebase.database.getReference(USER_DB_KEY)
  private lateinit var roomDB: DatabaseReference
  private lateinit var roomDetailDB: DatabaseReference
  private lateinit var roundDB: DatabaseReference
  private lateinit var questionDB: DatabaseReference
  private lateinit var chattingDB: DatabaseReference

  private val userService by lazy { ApplicationClass.wRetrofit.create(UserService::class.java) }

  private val _user = MutableLiveData<UserDto>()
  val user: LiveData<UserDto> get() = _user

  private val _playerList = MutableLiveData<List<UserDto>>()
  val playerList: LiveData<List<UserDto>> get() = _playerList;

  private val _room = MutableLiveData<RoomDto>()
  val room: LiveData<RoomDto> get() = _room

  private val _round = MutableLiveData<RoundDto?>()
  val round: LiveData<RoundDto?> get() = _round

  private val _question = MutableLiveData<QuestionDto?>()
  val question: LiveData<QuestionDto?> get() = _question

  private val _drawing = MutableLiveData<String?>()
  val drawing: LiveData<String?> get() = _drawing

  private val _chattingList = MutableLiveData<List<ChattingDto>>()
  val chatting: LiveData<List<ChattingDto>> get() = _chattingList

  init {
    viewModelScope.launch {
      val currentUserDB = userDB.child(auth.currentUser!!.uid)
      _user.value = currentUserDB.get().await().getValue(UserDto::class.java)

      val rid = _user.value!!.currentRid
      roomDB = Firebase.database.getReference("${ROOM_DB_KEY}/${rid}")
      roomDetailDB = Firebase.database.getReference("${ROOM_DETAIL_DB_KEY}/${rid}")
      roundDB = Firebase.database.getReference("${ROUND_DB_KEY}/${rid}")
      questionDB = Firebase.database.getReference("${QUESTION_DB_KEY}/${rid}")
      chattingDB = Firebase.database.getReference("${CHATTING_DB_KEY}/${rid}")

      currentUserDB.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          _user.postValue(snapshot.getValue(UserDto::class.java))
        }

        override fun onCancelled(error: DatabaseError) {}
      })

      roomDB.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          _room.postValue(snapshot.getValue(RoomDto::class.java))
        }

        override fun onCancelled(error: DatabaseError) {}
      })

      roomDetailDB.child("playerList").addChildEventListener(object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
          val uid = snapshot.getValue(String::class.java) ?: return
          viewModelScope.launch {
            val user =
              userDB.child(uid).get().await().getValue(UserDto::class.java) ?: return@launch
            _playerList.value = _playerList.value!!.toMutableList().apply { add(user) }
          }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
          val uid = snapshot.getValue(String::class.java) ?: return
          viewModelScope.launch {
            val user = userDB.child(uid).get().await().getValue(UserDto::class.java)
              ?: return@launch
            if (user.uid == "empty") {
              _playerList.value = _playerList.value!!.toMutableList().apply {
                removeAt(indexOf(user))
              }
            } else {
              _playerList.value = _playerList.value!!.toMutableList().apply {
                set(indexOf(user), user)
              }
            }
          }
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
          val uid = snapshot.getValue(String::class.java) ?: return
          viewModelScope.launch {
            val user = userDB.child(uid).get().await().getValue(UserDto::class.java)
              ?: return@launch
            _playerList.value = _playerList.value!!.toMutableList().apply {
              removeAt(indexOf(user))
            }
          }
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onCancelled(error: DatabaseError) {}
      })

      roundDB.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          _round.postValue(snapshot.getValue(RoundDto::class.java))
        }

        override fun onCancelled(error: DatabaseError) {}
      })

      questionDB.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          _question.postValue(snapshot.getValue(QuestionDto::class.java))
        }

        override fun onCancelled(error: DatabaseError) {}
      })

      questionDB.child("drawing").addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          _drawing.postValue(snapshot.getValue(String::class.java))
        }

        override fun onCancelled(error: DatabaseError) {}
      })

      chattingDB.addChildEventListener(object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
          val chattingDto = snapshot.getValue(ChattingDto::class.java) ?: return
          _chattingList.value = _chattingList.value!!.toMutableList().apply { add(chattingDto) }
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onChildRemoved(snapshot: DataSnapshot) {}

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onCancelled(error: DatabaseError) {}
      })
    }
  }

  fun exitRoom() {
    viewModelScope.launch {
      userService.exitRoom(RoomActionDto(uid = user.value!!.uid))
    }
  }

  fun playGame() {
    viewModelScope.launch {
      userService.startGame(RoomActionDto(rid = room.value!!.rid))
    }
  }


  companion object {
    const val USER_DB_KEY = "user"
    const val ROOM_DB_KEY = "room"
    const val ROOM_DETAIL_DB_KEY = "roomDetail"
    const val ROUND_DB_KEY = "round"
    const val QUESTION_DB_KEY = "question"
    const val CHATTING_DB_KEY = "chatting"
  }
}