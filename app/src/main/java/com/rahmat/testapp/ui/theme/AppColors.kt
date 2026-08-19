package com.rahmat.testapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {

    val ButtonPrimary = Orange_55
    val ButtonPrimaryPressed = Orange_65
    val TextInputEmployee = ButtonPrimary

    val MenuCardBackground: Color
        @Composable
        get() = MaterialTheme.colorScheme.secondaryContainer
    val CategoryBackground: Color
        @Composable
        get() = MenuCardBackground
    val AccentMenu = Orange_110
    val CartButton = AccentMenu
    val BorderPrimary = AccentMenu
    val BorderSecondary = Orange_50
    val SearchButton = BlueGray60

    val TextPrimary: Color
        @Composable
        get() = MaterialTheme.colorScheme.onBackground
    val TextSecondary = Gray_90

    val TextOnComponent : Color
        @Composable
        get() = MaterialTheme.colorScheme.onSurface

}