package com.dtd.chaincatch.model.dto

import java.io.Serializable

data class RoomDto(
  val rid: String = "",
  val title: String = "",
  val manager: String = "",
  val maxUser: Int = 5,
  val currentUser: Int = 1,
  val state: String = "",
): Serializable {
  override fun equals(other: Any?): Boolean {
    if (other is RoomDto) return rid == other.rid
    return false
  }

  override fun hashCode(): Int {
    return super.hashCode()
  }
}
