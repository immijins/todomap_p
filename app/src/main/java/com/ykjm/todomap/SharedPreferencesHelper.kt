package com.ykjm.todomap

import android.content.Context

object SharedPreferencesHelper {

    private const val PREFS_NAME = "calendar_todo_prefs"

    fun saveEmojiForDate(context: Context, dateKey: String, emoji: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(dateKey, emoji)
        editor.apply()
    }

    fun getEmojiForDate(context: Context, dateKey: String): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(dateKey, null)
    }
}