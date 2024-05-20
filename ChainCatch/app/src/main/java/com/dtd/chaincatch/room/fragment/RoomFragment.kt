package com.dtd.chaincatch.room.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import com.dtd.chaincatch.R
import com.dtd.chaincatch.config.BaseFragment
import com.dtd.chaincatch.databinding.FragmentRoomBinding
import com.dtd.chaincatch.room.RoomActivity
import com.dtd.chaincatch.room.viewmodel.RoomViewModel

private const val TAG = "RoomFragment_싸피"

class RoomFragment :
  BaseFragment<FragmentRoomBinding>(FragmentRoomBinding::bind, R.layout.fragment_room) {
  private val viewModel by activityViewModels<RoomViewModel>()
  private lateinit var roomActivity: RoomActivity

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    roomActivity = requireActivity() as RoomActivity
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    requireActivity().onBackPressedDispatcher.addCallback {
      viewModel.exitRoom()
    }

    viewModel.user.observe(viewLifecycleOwner) {
      if (it.currentRid == null) roomActivity.finish()
    }

    viewModel.playerList.observe(viewLifecycleOwner) {
      // TODO : playerList 초기화
      Log.d(TAG, "playerList: $it")
    }

    viewModel.chatting.observe(viewLifecycleOwner) {
      // TODO : chattingList 초기화
      Log.d(TAG, "chatting: $it")
    }

    viewModel.drawing.observe(viewLifecycleOwner) {
      // TODO : imageView 초기화
      Log.d(TAG, "drawing: $it")
    }

    viewModel.question.observe(viewLifecycleOwner) {
      // TODO : questionView 초기화
      // state == Waiting -> 누구 차롄지 dialog 띄우기
      // state == Success -> 누가 맞췄는지 dialog 띄우기
      // state == Fail -> 실패했다고 dialog 띄우기
      // uid가 자기자신이면 그림그리기 모드로 변경
      // uid가 자기자신이 아니면 문제맞추기 모드로 변경
      Log.d(TAG, "question: $it")
    }

    viewModel.round.observe(viewLifecycleOwner) {
      // TODO : round 정보 초기화
      // state == Finished -> nft 다이얼로그 띄우기
      Log.d(TAG, "round: $it")
    }

    viewModel.room.observe(viewLifecycleOwner) {
      // TODO : room 정보 초기화
      Log.d(TAG, "room: $it")
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    // TODO : 호출안되는 버그 수정
    viewModel.exitRoom()
  }
}