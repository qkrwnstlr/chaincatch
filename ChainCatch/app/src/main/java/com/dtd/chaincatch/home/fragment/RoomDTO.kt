package com.dtd.chaincatch.home.fragment

import java.io.Serializable

data class RoomDTO(
  val rid: Int = -1,
  val title: String,
  val manager: String,
  val currentUser: Int,
  val maxUser: Int = 5
) : Serializable