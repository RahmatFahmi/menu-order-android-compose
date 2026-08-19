package com.rahmat.testapp.ui.publicscreen.auth.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahmat.testapp.R
import com.rahmat.testapp.ui.components.CustomAuthHeader
import com.rahmat.testapp.ui.components.CustomBackgroundLayout
import com.rahmat.testapp.ui.components.CustomButton
import com.rahmat.testapp.ui.components.CustomText
import com.rahmat.testapp.ui.components.CustomTextField
import com.rahmat.testapp.ui.components.CustomToast
import com.rahmat.testapp.ui.theme.AppDimens
import com.rahmat.testapp.ui.theme.Blue_10
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegistrationScreen(
    onBackToLoginClick: () -> Unit
){
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordConfirmation by remember { mutableStateOf("") }
    var scope = rememberCoroutineScope()
    var showToast by remember { mutableStateOf<String?>(null) }

    CustomBackgroundLayout(
        resId = null,
        modifier = Modifier.fillMaxWidth(),
    )
    {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ){padding->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = AppDimens.HorizontalScreenPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                CustomAuthHeader(
                    resId = R.drawable.ic_star,
                    contentDescription = "Star Icon",
                    title = "The Universe",
                    modifier = Modifier.fillMaxWidth(),
                    size = 40.dp
                )
                Spacer(modifier = Modifier.height(100.dp))

                CustomText(
                    text = "Sign Up",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))
                CustomText(
                    text = "Silahkan Melakukan Regitrasi Terlebih Dahulu",
                    fontSize = 14.sp,
                )

                Spacer(modifier = Modifier.height(32.dp))
                CustomTextField(
                    value = username,
                    onValueChange = {username = it},
                    label = "Username",
                    isPassword = false
                )
                Spacer(modifier = Modifier.height(AppDimens.PaddingMedium))
                CustomTextField(
                    value = email,
                    onValueChange = {email = it},
                    label = "Email",
                    isPassword = false
                )

                Spacer(modifier = Modifier.height(AppDimens.PaddingMedium))
                CustomTextField(
                    value = password,
                    onValueChange = {password = it},
                    label = "Password",
                    isPassword = true
                )
                Spacer(modifier = Modifier.height(AppDimens.PaddingMedium))
                CustomTextField(
                    value = passwordConfirmation,
                    onValueChange = {passwordConfirmation = it},
                    label = "Konfirmasi Password",
                    isPassword = true
                )

                Spacer(modifier = Modifier.height(42.dp))

                CustomButton(
                    text = "Regitration",
                    isLoading = isLoading,
                    enabled = true,
                    onClick = {
                        isLoading = !isLoading
                        if(isLoading){
                            scope.launch{
                                delay(2000)
                                isLoading = !isLoading
                            }
                        }
                    },
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth(),
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row{
                    CustomText(
                        text = "Already Have An Account?",
                        fontSize = 14.sp,
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    CustomText(
                        text = "Sign In",
                        fontSize = 14.sp,
                        color = Blue_10,
                        fontStyle = FontStyle.Italic,
                        onClick = {
                            showToast = "Register Toast"
                            onBackToLoginClick()
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun RegistrationScreenPreview(){
    RegistrationScreen(
        onBackToLoginClick = {}
    )
}