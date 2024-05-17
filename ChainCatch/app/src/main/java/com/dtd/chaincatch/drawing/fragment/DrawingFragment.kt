package com.dtd.chaincatch.drawing.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import java.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dtd.chaincatch.databinding.FragmentDrawingBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.raed.rasmview.brushtool.data.Brush
import com.raed.rasmview.brushtool.data.BrushesRepository
import com.raed.rasmview.state.RasmState
import java.io.ByteArrayOutputStream


private const val TAG = "DrawingFragment"

class DrawingFragment : Fragment() {

  private var _binding: FragmentDrawingBinding? = null
  private val binding: FragmentDrawingBinding
    get() = _binding!!

  private lateinit var drawingDB: DatabaseReference
  private lateinit var drawingRef: DatabaseReference

  private fun initFirebaseDatabase() {
    drawingDB = Firebase.database.getReference("message")
    val drawingKey = drawingDB.push().key
    drawingRef = drawingDB.child(drawingKey!!)

    drawingRef.addValueEventListener(object : ValueEventListener {
      override fun onDataChange(snapshot: DataSnapshot) {
        Log.d(TAG, "onDataChange: ")
        if (drawingKey != snapshot.key) return
        val data = snapshot.getValue(String::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          val bitmap = byteArrayToBitmap(Base64.getDecoder().decode(data))
          binding.imageView.setImageBitmap(bitmap)
        }
      }

      override fun onCancelled(error: DatabaseError) {}
    })
  }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    initFirebaseDatabase()
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentDrawingBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val rasmContext = binding.drawingView.rasmContext

    // brush
    val brushesRepository = BrushesRepository(resources)
    rasmContext.brushConfig = brushesRepository.get(Brush.Marker)
    rasmContext.brushColor = Color.RED
//    rasmContext.brushColor = 0xff2187bb.toInt() //ARGB

    val rasmState: RasmState = rasmContext.state

    rasmState.addOnStateChangedListener {
      val drawingBitmap: Bitmap = rasmContext.exportRasm()
      drawingRef.setValue(drawingBitmap.toBase64())
    }

    binding.undo.setOnClickListener {
      rasmState.undo()
    }
    binding.redo.setOnClickListener {
      rasmState.redo()
    }
    rasmState.addOnStateChangedListener {
      binding.undo.isEnabled = rasmState.canCallUndo()
      binding.redo.isEnabled = rasmState.canCallRedo()
    }
    binding.undo.isEnabled = rasmState.canCallUndo()
    binding.redo.isEnabled = rasmState.canCallRedo()
  }

  fun byteArrayToBitmap(bytearr: ByteArray): Bitmap {
    return BitmapFactory.decodeByteArray(bytearr, 0, bytearr.size)
  }

}

fun Bitmap.toByteArray(): ByteArray {
  val stream = ByteArrayOutputStream()
  this.compress(Bitmap.CompressFormat.PNG, 100, stream)
  return stream.toByteArray()
}

fun Bitmap.toBase64(): String {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    String(Base64.getEncoder().encode(this.toByteArray()))
  } else {
    ""
  }
}
