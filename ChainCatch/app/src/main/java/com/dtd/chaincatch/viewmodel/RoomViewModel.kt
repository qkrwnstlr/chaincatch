package com.dtd.chaincatch.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dtd.chaincatch.ApplicationClass
import com.dtd.chaincatch.model.dto.RoomActionDto
import com.dtd.chaincatch.model.dto.RoomDto
import com.dtd.chaincatch.model.service.UserService
import com.dtd.chaincatch.model.dto.ChattingDto
import com.dtd.chaincatch.model.dto.QuestionDto
import com.dtd.chaincatch.model.dto.RoundDto
import com.dtd.chaincatch.model.dto.UserDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "RoomViewModel_싸피"

class RoomViewModel : ViewModel() {
  private var auth: FirebaseAuth = Firebase.auth

  private val userDB = Firebase.database.getReference(USER_DB_KEY)
  private lateinit var roomDB: DatabaseReference
  private lateinit var roomDetailDB: DatabaseReference
  private lateinit var roundDB: DatabaseReference
  private lateinit var questionDB: DatabaseReference
  private lateinit var drawingDB: DatabaseReference
  private lateinit var chattingDB: DatabaseReference

  private val userService by lazy { ApplicationClass.wRetrofit.create(UserService::class.java) }

  private val _user = MutableLiveData<UserDto>()
  val user: LiveData<UserDto> get() = _user

  private val _playerList = MutableLiveData<List<UserDto>>()
  val playerList: LiveData<List<UserDto>> get() = _playerList
  private val _playerCount = MutableLiveData<Map<String, Int>>()
  val playerCount: LiveData<Map<String, Int>> get() = _playerCount

  private val _room = MutableLiveData<RoomDto?>()
  val room: LiveData<RoomDto?> get() = _room

  private val _round = MutableLiveData<RoundDto?>()
  val round: LiveData<RoundDto?> get() = _round

  private val _question = MutableLiveData<QuestionDto?>()
  val question: LiveData<QuestionDto?> get() = _question

  private val _drawing = MutableLiveData<String?>()
  val drawing: LiveData<String?> get() = _drawing

  private val _newChatting = MutableLiveData<ChattingDto>()
  val chatting: LiveData<ChattingDto> get() = _newChatting

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
      drawingDB = Firebase.database.getReference("${DRAWING_DB_KEY}/${rid}")

      currentUserDB.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          _user.postValue(snapshot.getValue(UserDto::class.java))
        }

        override fun onCancelled(error: DatabaseError) {}
      })

      roomDB.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          val room = snapshot.getValue(RoomDto::class.java)
          if (_room.value?.state != room?.state) _room.postValue(room)
        }

        override fun onCancelled(error: DatabaseError) {}
      })

      roomDetailDB.child("playerList").addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          val ti = object : GenericTypeIndicator<List<String>>() {}
          val uidList = snapshot.getValue(ti)
          viewModelScope.launch {
            val playerList = mutableListOf<UserDto>()
            uidList?.filter { it != "empty" }?.forEach {
              val user = userDB.child(it).get().await().getValue(UserDto::class.java)
                ?: return@forEach
              playerList.add(user)
            }
            _playerList.postValue(playerList)
          }
        }

        override fun onCancelled(error: DatabaseError) {}
      })

      roomDetailDB.child("playerCount").addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          val ti = object : GenericTypeIndicator<HashMap<String, Int>>() {}
          val playerCount = snapshot.getValue(ti) ?: return
          _playerCount.postValue(playerCount)
        }

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
          val questionDto = snapshot.getValue(QuestionDto::class.java)
          _question.postValue(questionDto)
        }

        override fun onCancelled(error: DatabaseError) {}
      })

      drawingDB.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
          _drawing.postValue(snapshot.getValue(String::class.java))
        }

        override fun onCancelled(error: DatabaseError) {}
      })

      chattingDB.addChildEventListener(object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
          val chattingDto = snapshot.getValue(ChattingDto::class.java) ?: return
          _newChatting.postValue(chattingDto)
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

  fun drawing(base64: String) {
    if (::questionDB.isInitialized) drawingDB.setValue(base64)
  }

  fun sendChatting(content: String) {
    val chattingDto = ChattingDto(user.value!!.uid, user.value!!.nickname, content)
    chattingDB.push().setValue(chattingDto)
  }

  fun startGame() {
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
    const val DRAWING_DB_KEY = "drawing"
  }
}