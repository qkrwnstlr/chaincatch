package com.dtd.chaincatch

import android.app.Application
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 앱이 실행될 때 1번만 실행이 됨
class ApplicationClass : Application() {
  companion object {

    // 전역변수 문법을 통해 Retrofit 인스턴스를 앱 실행 시 1번만 생성하여 사용 (싱글톤 객체)
    lateinit var wRetrofit: Retrofit
    const val STORE_ID = 1
  }

  override fun onCreate() {
    super.onCreate()

    // 앱이 처음 생성되는 순간, retrofit 인스턴스를 생성
    wRetrofit = Retrofit.Builder()
      .baseUrl("https://app-mqqqc7oa4q-uc.a.run.app")
      .addConverterFactory(GsonConverterFactory.create(gson))
      .build()
  }

  //GSon은 엄격한 json type을 요구하는데, 느슨하게 하기 위한 설정.
  // success, fail등 문자로 리턴될 경우 오류 발생한다. json 문자열이 아니라고..
  val gson: Gson = GsonBuilder().setLenient().create()
}