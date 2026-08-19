package com.rahmat.testapp.common.utils.theme

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val themeManager = ThemeManager(application)

    var isDark by mutableStateOf(themeManager.isDarkMode())
        private set

    fun toggleTheme() {
        isDark = !isDark
        themeManager.setDarkMode(isDark)
        Log.d("Test Dark", "toggleTheme: $isDark")

    }
}