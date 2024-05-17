package com.dtd.chaincatch.home.model.dto

data class Room(
  val rid: String,
  val title: String,
  val manager: String,
  val maxUser: Long = 5,
  val currentUser: Long = 1,
) {
  override fun equals(other: Any?): Boolean {
    if (other is Room) return rid == other.rid
    return false
  }

  override fun hashCode(): Int {
    return super.hashCode()
  }
}
