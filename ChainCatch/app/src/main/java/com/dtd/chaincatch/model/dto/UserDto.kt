package com.dtd.chaincatch.model.dto

import java.io.Serializable

data class UserDto(
  val uid: String = "",
  val profileImg: Int = 0,
  val nickname: String = "",
  val nftAddress: String = "",
  val experience: Int = 1,
  val currentRid: String? = null
): Serializable {
  override fun equals(other: Any?): Boolean {
    if(other is UserDto) return this.uid == other.uid
    return false
  }

  override fun hashCode(): Int {
    return uid.hashCode()
  }
}