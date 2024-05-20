package com.dtd.chaincatch.home.model.service

import com.dtd.chaincatch.home.model.dto.RoomActionDto
import com.dtd.chaincatch.home.model.dto.RoomDto
import com.dtd.chaincatch.user.model.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserService {
  @POST("/createUser")
  suspend fun createUser(@Body userDto: UserDto)

  @POST("/createRoom")
  suspend fun createRoom(@Body roomDto: RoomDto)

  @POST("/enterRoom")
  suspend fun enterRoom(@Body roomActionDto: RoomActionDto)

  @POST("/exitRoom")
  suspend fun exitRoom(@Body roomActionDto: RoomActionDto)

  @POST("/startGame")
  suspend fun startGame(@Body roomActionDto: RoomActionDto)
}