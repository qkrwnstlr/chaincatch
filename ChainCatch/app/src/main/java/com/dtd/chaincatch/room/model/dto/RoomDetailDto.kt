package com.dtd.chaincatch.room.model.dto

data class RoomDetailDto(
  val rid: String,
  val playerList: List<String>,
  val waitingList: List<String>
)