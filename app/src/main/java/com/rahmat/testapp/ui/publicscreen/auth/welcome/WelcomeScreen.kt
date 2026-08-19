package com.rahmat.testapp.ui.publicscreen.auth.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahmat.testapp.R
import com.rahmat.testapp.ui.components.CustomBackgroundLayout
import com.rahmat.testapp.ui.components.CustomButton
import com.rahmat.testapp.ui.theme.AppDimens
import com.rahmat.testapp.ui.theme.Red_Terracotta
import com.rahmat.testapp.ui.theme.Teal_Deep
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    onEmployeeClick: () -> Unit,
    onCustomerClick: () -> Unit
){
    var isLoadingEmployee by remember { mutableStateOf(false) }
    var isLoadingCustomer by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    CustomBackgroundLayout(
        resId = R.drawable.bg_welcome_img,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppDimens.HorizontalScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            CustomButton(
                text = "K A R Y A W A N",
                isLoading = isLoadingEmployee,
                enabled = true,
                onClick = {
                    onEmployeeClick()
                },
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                fontSize = 16.sp,
                containerColor = Red_Terracotta
            )
            Spacer(modifier = Modifier.height(16.dp))
            CustomButton(
                text = "P E L A N G G A N",
                isLoading = isLoadingCustomer,
                enabled = true,
                onClick = {
                    onCustomerClick()
                },
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                fontSize = 16.sp,
                containerColor = Teal_Deep
            )
            Spacer(modifier = Modifier.height(42.dp))
        }
    }
}