package com.dtd.chaincatch.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import java.io.ByteArrayOutputStream
import java.util.Base64


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

fun base64ToBitmap(base64: String): Bitmap? {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val byteArr = Base64.getDecoder().decode(base64)
    return BitmapFactory.decodeByteArray(byteArr, 0, byteArr.size)
  }
  return null
}