package com.dtd.chaincatch.util

import android.content.res.Resources
import kotlin.math.ceil

val Int.dp: Int
  get() {
    return if (this == 0) {
      0
    } else ceil((Resources.getSystem().displayMetrics.density * this).toDouble()).toInt()
  }