package com.dtd.chaincatch.view.fragment

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.dtd.chaincatch.R
import com.dtd.chaincatch.config.BaseFragment
import com.dtd.chaincatch.databinding.ChattingBubbleBinding
import com.dtd.chaincatch.databinding.DialogBrushSettingsBinding
import com.dtd.chaincatch.databinding.FragmentRoomBinding
import com.dtd.chaincatch.model.dto.UserDto
import com.dtd.chaincatch.util.SeekBarUserChangeListener
import com.dtd.chaincatch.util.base64ToBitmap
import com.dtd.chaincatch.util.toBase64
import com.dtd.chaincatch.view.activity.RoomActivity
import com.dtd.chaincatch.viewmodel.RoomViewModel
import com.dtd.chaincatch.widget.UserCardView
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.raed.rasmview.RasmContext
import com.raed.rasmview.brushtool.data.Brush
import com.raed.rasmview.brushtool.data.BrushesRepository
import com.raed.rasmview.state.RasmState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask
import kotlin.math.max
import kotlin.math.roundToInt

private const val TAG = "RoomFragment_싸피"

class RoomFragment :
  BaseFragment<FragmentRoomBinding>(FragmentRoomBinding::bind, R.layout.fragment_room) {
  private val viewModel by activityViewModels<RoomViewModel>()
  private lateinit var roomActivity: RoomActivity

  private lateinit var brushDialogBinding: DialogBrushSettingsBinding
  private lateinit var brushDialog: AlertDialog.Builder

  private lateinit var colorDialog: MaterialColorPickerDialog.Builder

  private var startDialog: AlertDialog? = null
  private var waitingDialog: AlertDialog? = null
  private var finishDialog: AlertDialog? = null
  private var turnDialog: AlertDialog? = null
  private var successDialog: AlertDialog? = null
  private var failDialog: AlertDialog? = null

  private lateinit var rasmContext: RasmContext
  private lateinit var rasmState: RasmState

  private lateinit var timer: Timer

  private fun showStartDialog() {
    startDialog?.dismiss()
    startDialog = AlertDialog.Builder(requireContext()).setMessage("곧 게임이 시작됩니다.").show()
  }

  private fun closeStartDialog() {
    startDialog?.dismiss()
    turnDialog = null
  }

  private fun showWaitingDialog() {
    waitingDialog?.dismiss()
    waitingDialog = AlertDialog.Builder(requireContext()).setMessage("이미 게임이 진행 중 입니다. 대기해 주세요").show()
  }

  private fun showFinishDialog() {
    finishDialog?.dismiss()
    finishDialog = AlertDialog.Builder(requireContext()).setMessage("게임이 종료되었습니다.").show()
  }

  private fun closeFinishDialog() {
    finishDialog?.dismiss()
    finishDialog = null
  }

  private fun showTurnDialog(nickname: String) {
    turnDialog?.dismiss()
    closeStartDialog()
    turnDialog = AlertDialog.Builder(requireContext()).setMessage("${nickname}님의 차례입니다.").show()
  }

  private fun closeTurnDialog() {
    turnDialog?.dismiss()
    turnDialog = null
  }

  private fun showSuccessDialog(uid: String, answer: String) {
    successDialog?.dismiss()
    successDialog = AlertDialog.Builder(requireContext()).setMessage(
      "${uid}님이 정답을 맞췄습니다.\n" + "정답 : $answer"
    ).show()
  }

  private fun closeSuccessDialog() {
    successDialog?.dismiss()
    successDialog = null
  }

  private fun showFailDialog(answer: String) {
    failDialog?.dismiss()
    failDialog = AlertDialog.Builder(requireContext()).setMessage(
      "시간이 초과되었습니다.\n정답 : $answer"
    ).show()
  }

  private fun closeFailDialog() {
    failDialog?.dismiss()
    failDialog = null
  }

  private fun initTimerButton() {
    binding.timeTv.visibility = View.VISIBLE // View.GONE
  }

  private fun initStartButton() {
    binding.startButton.setOnClickListener {
      viewModel.startGame()
    }
  }

  private fun initPlayerList() {
    initPlayerBGColor()
  }

  private fun initPlayerBGColor() {
    binding.user1.setBgColor(Color.parseColor("#f8f439"))
    binding.user2.setBgColor(Color.parseColor("#80dbe6"))
    binding.user3.setBgColor(Color.parseColor("#77cd62"))
    binding.user4.setBgColor(Color.parseColor("#c958b2"))
    binding.user5.setBgColor(Color.parseColor("#f02a37"))
  }

  private fun initBrushDialog() {
    brushDialogBinding = DialogBrushSettingsBinding.inflate(layoutInflater)
    brushDialog = AlertDialog.Builder(requireContext())
      .setTitle("Brush Setting")
      .setView(brushDialogBinding.root)
      .setPositiveButton("OK") { _, _ -> }
      .setNegativeButton("Cancel") { _, _ -> }
    colorDialog = MaterialColorPickerDialog.Builder(requireContext())
      .setTitle("Brush Color")
      .setColorShape(ColorShape.SQAURE)
      .setColorListener { color, _ ->
        binding.colorBtn.setBackgroundColor(color)
        rasmContext.brushColor = color
      }

    brushDialogBinding.brushSpinner.apply {
      adapter = ArrayAdapter(
        requireContext(),
        android.R.layout.simple_list_item_1,
        Brush.entries.toTypedArray()
      )
      onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
          val brushesRepository = BrushesRepository(resources)
          val brush = adapter.getItem(p2) as Brush
          rasmContext.brushConfig = brushesRepository.get(brush)

          val brushConfig = rasmContext.brushConfig
          brushDialogBinding.brushSizeSeekBar.progress = (99 * brushConfig.size).roundToInt()
          brushDialogBinding.brushFlowSeekBar.progress = (99 * brushConfig.flow).roundToInt()
          brushDialogBinding.brushOpacitySeekBar.progress = (99 * brushConfig.opacity).roundToInt()
        }

        override fun onNothingSelected(p0: AdapterView<*>?) {}
      }
    }

    brushDialogBinding.brushSizeSeekBar.apply {
      setOnSeekBarChangeListener(SeekBarUserChangeListener { v ->
        rasmContext.brushConfig.size = (v + 1) / 100f
      })
    }

    brushDialogBinding.brushFlowSeekBar.apply {
      setOnSeekBarChangeListener(SeekBarUserChangeListener { v ->
        rasmContext.brushConfig.flow = (v + 1) / 100f
      })
    }

    brushDialogBinding.brushOpacitySeekBar.apply {
      setOnSeekBarChangeListener(SeekBarUserChangeListener { v ->
        rasmContext.brushConfig.opacity = (v + 1) / 100f
      })
    }
  }

  private fun toggleMode(isDrawing: Boolean) {
    if (isDrawing) {
      binding.dvQuestioner.visibility = View.VISIBLE
      binding.toolList.visibility = View.VISIBLE
      binding.questionTv.visibility = View.VISIBLE

      binding.ivSolver.visibility = View.GONE

      binding.chattingEt.isEnabled = false
    } else {
      binding.dvQuestioner.visibility = View.GONE
      binding.toolList.visibility = View.GONE
      binding.questionTv.visibility = View.GONE

      binding.ivSolver.visibility = View.VISIBLE

      binding.chattingEt.isEnabled = true
    }
  }

  private fun initDrawingView() {
    rasmContext = binding.dvQuestioner.rasmContext
    rasmState = rasmContext.state

    rasmState.addOnStateChangedListener {
      val drawingBitmap: Bitmap = rasmContext.exportRasm()
      viewModel.drawing(drawingBitmap.toBase64())
    }

    rasmState.addOnStateChangedListener {
      binding.undoBtn.isEnabled = rasmState.canCallUndo()
      binding.redoBtn.isEnabled = rasmState.canCallRedo()
    }

    rasmContext.brushColor = 0x000000
    binding.undoBtn.isEnabled = rasmState.canCallUndo()
    binding.redoBtn.isEnabled = rasmState.canCallRedo()

    binding.colorBtn.setOnClickListener {
      colorDialog.show()
    }
    binding.brushBtn.setOnClickListener {
      if (brushDialogBinding.root.parent != null) {
        ((brushDialogBinding.root.parent) as ViewGroup).removeView(brushDialogBinding.root)
      }
      brushDialog.show()
    }
    binding.redoBtn.setOnClickListener { rasmState.redo() }
    binding.undoBtn.setOnClickListener { rasmState.undo() }
    binding.clearBtn.setOnClickListener { rasmContext.clear() }
  }

  private fun parseProfileImage(profileImage: Int): Int {
    return when (profileImage) {
      CAT_CHEESE -> R.drawable.cat_cheese_face
      CAT_GREY -> R.drawable.cat_grey_face
      CAT_FISH -> R.drawable.cat_fish_face
      CAT_RAINBOW -> R.drawable.cat_rainbow_face
      else -> R.drawable.cat_cheese_face
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    roomActivity = requireActivity() as RoomActivity
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val profileList: List<UserCardView> = listOf(
      binding.user1,
      binding.user2,
      binding.user3,
      binding.user4,
      binding.user5,
    )

    val chattingList: List<ChattingBubbleBinding> = listOf(
      binding.balloon1,
      binding.balloon2,
      binding.balloon3,
      binding.balloon4,
      binding.balloon5,
    )

    profileList.forEach { it.visibility = View.INVISIBLE }
    chattingList.forEach { it.balloon.visibility = View.INVISIBLE }

    viewModel.user.observe(viewLifecycleOwner) {
      if (it.currentRid == null) roomActivity.finish()
    }

    viewModel.playerList.observe(viewLifecycleOwner) {
      Log.d(TAG, "onViewCreated: ${it.size}")
      var index = 0
      for (i in 0 until it.size) {
        val userDto = it[index]
        with(profileList[index]) {
          setUserImage(parseProfileImage(userDto.profileImg))
          setUerNickname(userDto.nickname)
          setUserAnswerCnt("${userDto.experience}")
          visibility = View.VISIBLE
        }
        index++
      }
      for (i in index until 5) profileList[index].visibility = View.INVISIBLE
    }

    viewModel.chatting.observe(viewLifecycleOwner) {
      val index = viewModel.playerList.value?.indexOf(UserDto(uid = it.uid))?.takeIf { it >= 0 }
      if (index == null) return@observe
      with(chattingList[index]) {
        chatting = it
        balloon.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
          delay(2000)
          balloon.visibility = View.INVISIBLE
        }
      }
    }

    viewModel.drawing.observe(viewLifecycleOwner) {
      if (it == null) binding.ivSolver.setImageResource(0)
      else binding.ivSolver.setImageBitmap(base64ToBitmap(it))
    }

    viewModel.question.observe(viewLifecycleOwner) { question ->
      toggleMode(question?.uid == viewModel.user.value!!.uid)

      if (question == null) return@observe
      binding.questionTv.text = question.answer

      when (question.state) {
        "Waiting" -> {
          val nickname = viewModel.playerList.value?.find { user ->
            user.uid == question.uid
          }?.nickname ?: return@observe
          rasmContext.clear()
          showTurnDialog(nickname)
          closeSuccessDialog()
          closeFailDialog()
        }

        "Playing" -> {
          closeTurnDialog()
          timer = Timer()
          val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
              viewLifecycleOwner.lifecycleScope.launch {
                val leftTime = 120 - (System.currentTimeMillis() - question.startTime) / 1000
                val minute = String.format("%02d", max(leftTime / 60, 0))
                val second = String.format("%02d", max(leftTime % 60, 0))
                binding.timeTv.text = "$minute : $second"
              }
            }
          }
          timer.schedule(timerTask, 0, 1000)
          binding.timeTv.visibility = View.VISIBLE
        }

        "Success" -> {
          timer.cancel()
          binding.timeTv.visibility = View.INVISIBLE // View.GONE
          showSuccessDialog(question.successorUid, question.answer)
        }

        "Fail" -> {
          timer.cancel()
          binding.timeTv.visibility = View.INVISIBLE // View.GONE
          showFailDialog(question.answer)
        }
      }
    }

    viewModel.round.observe(viewLifecycleOwner) {
      if (it == null) {
        closeFinishDialog()
      } else if (it.state == "Finished") {
        showFinishDialog()
        closeSuccessDialog()
        closeFailDialog()
      }
    }

    viewModel.room.observe(viewLifecycleOwner) {
      // TODO : room 정보 초기화
      if (it?.state == "Playing") {
        if(viewModel.playerList.value?.contains(viewModel.user.value) == true) {
          showStartDialog()
        } else {
          showWaitingDialog()
        }
      }
      if (it?.state == "Waiting" && it.manager == viewModel.user.value!!.uid) {
        binding.startButton.visibility = View.VISIBLE
      } else {
        binding.startButton.visibility = View.GONE
      }
    }

    initDrawingView()
    initBrushDialog()
    initPlayerList()
    initStartButton()
    initTimerButton()
    binding.chattingEt.setOnKeyListener { _, keyCode, _ ->
      when (keyCode) {
        KeyEvent.KEYCODE_ENTER -> {
          val content = binding.chattingEt.text.toString()
          if (content.isNotBlank()) {
            viewModel.sendChatting(content)
            binding.chattingEt.text.clear()
          }
        }
      }
      false
    }
  }

  companion object {
    private const val CAT_CHEESE = 0
    private const val CAT_GREY = 1
    private const val CAT_FISH = 2
    private const val CAT_RAINBOW = 3
  }
}