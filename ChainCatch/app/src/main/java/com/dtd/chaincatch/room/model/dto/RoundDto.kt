package com.dtd.chaincatch.room.model.dto

data class RoundDto(
  val rid: String,
  val state: String,
  val currentQuestion: Int,
  val lastQuestionList: List<QuestionDto>
)