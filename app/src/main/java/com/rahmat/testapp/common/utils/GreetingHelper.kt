package com.rahmat.testapp.common.utils

import java.util.Calendar

fun getGreetingMessage(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..4   -> "Selamat Malam! 🌙"
        in 5..11  -> "Selamat Pagi! ☀️"
        in 12..14 -> "Selamat Siang! 🌤️"
        in 15..17 -> "Selamat Sore! 🌇"
        in 18..20 -> "Selamat Malam! 🌆"
        else      -> "Selamat Malam! 🌙"
    }
}