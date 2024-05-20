package com.dtd.chaincatch.room.model.dto

data class QuestionDto(
  val uid: String,
  val answer: String,
  val state: String,
  val startTime: Long,
)
