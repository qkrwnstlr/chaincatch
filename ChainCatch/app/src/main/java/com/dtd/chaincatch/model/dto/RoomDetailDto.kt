package com.dtd.chaincatch.model.dto

data class RoomDetailDto(
  val rid: String = "",
  val playerList: List<String> = listOf(),
  val waitingList: List<String> = listOf(),
  val playerCount: Map<String, Int> = mapOf()
)