package com.rahmat.testapp.common.utils.theme

import android.content.Context

class ThemeManager(context: Context) {
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    fun setDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("is_dark_mode", isDark).apply()
    }

    fun isDarkMode(): Boolean {
        return prefs.getBoolean("is_dark_mode", false)
    }
}