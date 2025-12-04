package com.xzd.motherboardguider.utils

import android.content.Context
import android.content.SharedPreferences

object PrefsManager {
    private const val PREFS_NAME = "MotherboardGuiderPrefs"
    private const val KEY_TOKEN = "token"
    private const val KEY_LANGUAGE = "language"

    /**
     * 获取 SharedPreferences 实例
     */
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 保存 token
     */
    fun saveToken(context: Context, token: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(KEY_TOKEN, token)
        editor.apply()
    }

    /**
     * 获取 token
     */
    fun getToken(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_TOKEN, null)
    }

    /**
     * 清除 token（用于登出）
     */
    fun clearToken(context: Context) {
        val editor = getSharedPreferences(context).edit()
        editor.remove(KEY_TOKEN)
        editor.apply()
    }

    /**
     * 保存语言设置
     * @param language 语言代码，如 "zh"（中文）、"en"（英文）
     */
    fun saveLanguage(context: Context, language: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(KEY_LANGUAGE, language)
        editor.apply()
    }

    /**
     * 获取保存的语言设置
     * @return 语言代码，默认为 "zh"（中文）
     */
    fun getLanguage(context: Context): String {
        return getSharedPreferences(context).getString(KEY_LANGUAGE, "zh") ?: "zh"
    }
}



