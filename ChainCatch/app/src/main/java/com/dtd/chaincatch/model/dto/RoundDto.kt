package com.dtd.chaincatch.model.dto

data class RoundDto(
  val rid: String = "",
  val state: String = "",
  val currentQuestion: Int = 0,
  val lastQuestionList: List<QuestionDto> = listOf()
)