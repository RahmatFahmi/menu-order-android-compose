package com.rahmat.testapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color // HARUS IMPORT INI, BUKAN android.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.rahmat.testapp.ui.theme.Blue_10
import com.rahmat.testapp.ui.theme.Gray_10

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    cornerRadius: Int = 10,
    // Pastikan tipe data Color di sini merujuk ke androidx.compose.ui.graphics.Color
    focusedColor: Color = Blue_10,
    unfocusedColor: Color = Gray_10, // Biasanya unfocused pakai warna abu-abu
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    cursorColor: Color = Gray_10
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        modifier = modifier
            .scale(1f, 0.9f)
            .fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = focusedColor,
            focusedLabelColor = focusedColor,
            cursorColor = cursorColor,
            unfocusedBorderColor = unfocusedColor,
            unfocusedLabelColor = unfocusedColor,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor
        ),
        trailingIcon = {
            if (isPassword) {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = image,
                        contentDescription = null,
                        tint = unfocusedColor
                    )
                }
            }
        }
    )
}