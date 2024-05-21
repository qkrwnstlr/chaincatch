package com.dtd.chaincatch.room.fragment

import android.app.AlertDialog
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.dtd.chaincatch.R
import com.dtd.chaincatch.config.BaseFragment
import com.dtd.chaincatch.databinding.DialogBrushSettingsBinding
import com.dtd.chaincatch.databinding.FragmentRoomBinding
import com.dtd.chaincatch.room.RoomActivity
import com.dtd.chaincatch.room.adapter.PlayerListAdapter
import com.dtd.chaincatch.room.viewmodel.RoomViewModel
import com.dtd.chaincatch.util.SeekBarUserChangeListener
import com.dtd.chaincatch.util.base64ToBitmap
import com.dtd.chaincatch.util.toBase64
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.raed.rasmview.RasmContext
import com.raed.rasmview.brushtool.data.Brush
import com.raed.rasmview.brushtool.data.BrushesRepository
import com.raed.rasmview.state.RasmState
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

  private lateinit var rasmContext: RasmContext
  private lateinit var rasmState: RasmState

  private lateinit var playerListAdapter: PlayerListAdapter

  private lateinit var timer: Timer

  private fun initTimerButton() {
    binding.timeTv.visibility = View.GONE
  }

  private fun initStartButton() {
    binding.startButton.setOnClickListener {
      viewModel.startGame()
    }
  }

  private fun initPlayerList() {
    playerListAdapter = PlayerListAdapter()
    binding.playerList.adapter = playerListAdapter
    binding.playerList.layoutManager = LinearLayoutManager(requireContext()).apply {
      orientation = LinearLayoutManager.VERTICAL
    }
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
      binding.drawingView.visibility = View.VISIBLE
      binding.toolList.visibility = View.VISIBLE
      binding.questionTv.visibility = View.VISIBLE

      binding.imageView.visibility = View.GONE

      binding.chattingEt.isEnabled = false
    } else {
      binding.drawingView.visibility = View.GONE
      binding.toolList.visibility = View.GONE
      binding.questionTv.visibility = View.GONE

      binding.imageView.visibility = View.VISIBLE

      binding.chattingEt.isEnabled = true
    }
  }

  private fun initDrawingView() {
    rasmContext = binding.drawingView.rasmContext
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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    roomActivity = requireActivity() as RoomActivity
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewModel.user.observe(viewLifecycleOwner) {
      if (it.currentRid == null) roomActivity.finish()
    }

    viewModel.playerList.observe(viewLifecycleOwner) {
      if (::playerListAdapter.isInitialized) playerListAdapter.submitList(it)
    }

    viewModel.chatting.observe(viewLifecycleOwner) {
      // TODO : chattingList 초기화
      Log.d(TAG, "chatting: $it")
    }

    viewModel.drawing.observe(viewLifecycleOwner) {
      Log.d(TAG, "drawing: $it")
      if (it == null) binding.imageView.setImageResource(0)
      else binding.imageView.setImageBitmap(base64ToBitmap(it))
    }

    viewModel.question.observe(viewLifecycleOwner) { question ->
      toggleMode(question?.uid == viewModel.user.value!!.uid)

      if (question == null) return@observe
      binding.questionTv.text = question.answer

      when (question.state) {
        "Waiting" -> {
          val nickname = viewModel.playerList.value?.find { user -> user.uid == question.uid }
          AlertDialog.Builder(requireContext())
            .setMessage("${nickname}님의 차례입니다.")
            .setPositiveButton("OK") { _, _ -> }.show()
        }

        "Playing" -> {
          timer = Timer()
          val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
              lifecycleScope.launch {
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
          binding.timeTv.visibility = View.GONE
          AlertDialog.Builder(requireContext())
            .setMessage(
              "${question.successorUid}님이 정답을 맞췄습니다.\n" + "정답 : ${question.answer}"
            )
            .setPositiveButton("OK") { _, _ -> }.show()
        }

        "Fail" -> {
          timer.cancel()
          binding.timeTv.visibility = View.GONE
          AlertDialog.Builder(requireContext())
            .setMessage(
              "시간이 초과되었습니다.\n" + "정답 : ${question.answer}"
            )
            .setPositiveButton("OK") { _, _ -> }.show()
        }
      }
    }

    viewModel.round.observe(viewLifecycleOwner) {
      // TODO : round 정보 초기화
      // state == Finished -> nft 다이얼로그 띄우기
      if (it == null) return@observe
      if (it.state == "Finished") {
        AlertDialog.Builder(requireContext())
          .setMessage("라운드가 종료되었습니다.")
          .setPositiveButton("OK") { _, _ -> }.show()
      }
      Log.d(TAG, "round: $it")
    }

    viewModel.room.observe(viewLifecycleOwner) {
      // TODO : room 정보 초기화
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
  }
}