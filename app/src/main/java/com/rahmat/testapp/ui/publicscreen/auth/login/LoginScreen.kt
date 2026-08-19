package com.rahmat.testapp.ui.publicscreen.auth.login

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahmat.testapp.R
import com.rahmat.testapp.domain.model.User
import com.rahmat.testapp.ui.components.CustomAuthHeader
import com.rahmat.testapp.ui.components.CustomBackgroundLayout
import com.rahmat.testapp.ui.components.CustomButton
import com.rahmat.testapp.ui.components.CustomImage
import com.rahmat.testapp.ui.components.CustomText
import com.rahmat.testapp.ui.components.CustomTextField
import com.rahmat.testapp.ui.components.CustomToast
import com.rahmat.testapp.ui.components.ErrorText
import com.rahmat.testapp.ui.theme.AppColors
import com.rahmat.testapp.ui.theme.AppDimens
import com.rahmat.testapp.ui.theme.Blue_10
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (User) -> Unit
){
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUiState.Success -> {
                val user = (uiState as LoginUiState.Success).user
                onLoginSuccess(user)
            }
            else -> Unit
        }
    }


    CustomBackgroundLayout(
        resId = null,
        modifier = Modifier.fillMaxWidth(),
    )
    {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize(),

            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    CustomToast(message = data.visuals.message)
                }
            }
        )
        {padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = AppDimens.HorizontalScreenPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {

                CustomImage(
                    modifier = Modifier
                        .height(250.dp)
                        .fillMaxWidth(),
                    resId = R.drawable.img_auth,
                    contentDescription = "Study Illustration",
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(AppDimens.PaddingMedium))

                CustomText(
                    text = "Welcome Back",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))
                CustomText(
                    text = "Silahkan Login Terlebih Dahulu",
                    fontSize = 14.sp,
                )

                Spacer(modifier = Modifier.height(AppDimens.PaddingMedium))
                when (uiState) {
                    is LoginUiState.Error -> ErrorText((uiState as LoginUiState.Error).message)
                    else -> Unit
                }
                Spacer(modifier = Modifier.height(AppDimens.PaddingMedium))

                CustomTextField(
                    value = username,
                    onValueChange = {username = it},
                    label = "Username / Email",
                    isPassword = false
                )
                Spacer(modifier = Modifier.height(AppDimens.PaddingMedium))
                CustomTextField(
                    value = password,
                    onValueChange = {password = it},
                    label = "Password",
                    isPassword = true
                )
                Spacer(modifier = Modifier.height(42.dp))

                CustomButton(
                    text = "Login",
                    isLoading = uiState is LoginUiState.Loading,
                    enabled = true,
                    onClick = {
                        if (username.isEmpty() || password.isEmpty()){
                            scope.launch {
                                snackbarHostState.showSnackbar("Username / Password kosong")
                            }
                        }else{
                            viewModel.doLogin(username, password)
                        }
                    },
                    modifier = Modifier
                        .height(50.dp)
                        .scale(1f, 0.9f)
                        .fillMaxWidth(),
                    fontSize = 16.sp,
                )

                Spacer(modifier = Modifier.height(24.dp))


            }
        }

    }
}

