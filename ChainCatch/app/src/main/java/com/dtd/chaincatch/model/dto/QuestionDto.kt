package com.dtd.chaincatch.model.dto

data class QuestionDto(
  val uid: String = "",
  val answer: String = "",
  val state: String = "",
  val startTime: Long = 0,
  val drawing: String = "",
  val successorUid: String = "",
)
