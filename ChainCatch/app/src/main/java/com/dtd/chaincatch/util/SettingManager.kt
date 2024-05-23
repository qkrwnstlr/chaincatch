package com.dtd.chaincatch.util

import android.content.Context
import android.content.SharedPreferences


class SettingsManager(context: Context) {
  private val sharedPreferences: SharedPreferences =
    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

  var soundSetting: Boolean
    get() = sharedPreferences.getBoolean(SOUND_SETTING, true)
    set(isEnabled) {
      val editor = sharedPreferences.edit()
      editor.putBoolean(SOUND_SETTING, isEnabled)
      editor.apply()
    }

  fun getSoundSettingVal(): Boolean {
    return sharedPreferences.getBoolean(SOUND_SETTING, true)
  }

  companion object {
    private const val PREF_NAME = "settings"
    private const val SOUND_SETTING = "sound_setting"
  }
}