package com.dtd.chaincatch.user.model.dto

import java.io.Serializable

data class UserDto(
  val uid: String = "",
  val nickname: String = "",
  val experience: Int = 1,
  val currentRid: String? = null
): Serializable