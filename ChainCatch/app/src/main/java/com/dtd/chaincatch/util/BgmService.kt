package com.dtd.chaincatch.util

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import com.dtd.chaincatch.R

class BgmService : Service() {
  private lateinit var mediaPlayer: MediaPlayer

  override fun onBind(intent: Intent): IBinder? {
    return null
  }

  override fun onCreate() {
    super.onCreate()
    mediaPlayer = MediaPlayer.create(this, R.raw.bgm3)
    mediaPlayer.isLooping = true
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    mediaPlayer.start()
    return super.onStartCommand(intent, flags, startId)
  }

  override fun onDestroy() {
    mediaPlayer.stop()
    mediaPlayer.release()
    super.onDestroy()
  }
}