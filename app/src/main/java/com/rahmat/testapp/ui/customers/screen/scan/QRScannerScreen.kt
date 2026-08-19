package com.rahmat.testapp.ui.customers.screen.scan

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.rahmat.testapp.domain.model.User
import com.rahmat.testapp.ui.components.ErrorText
import com.rahmat.testapp.ui.components.QRScannerOverlay
import com.rahmat.testapp.ui.publicscreen.auth.login.LoginUiState
import com.rahmat.testapp.ui.publicscreen.auth.login.LoginViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QRScannerScreen(
    onQrCodeDetected: (User) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
    }

    // Navigasi saat Sukses
    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            val user = (uiState as LoginUiState.Success).user
            onQrCodeDetected(user)
        }
    }

    if (cameraPermissionState.status.isGranted) {
        ScannerCameraView(viewModel)
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Mohon izinkan akses kamera untuk scan QR Meja")
        }
    }
}

@Composable
fun ScannerCameraView(
    viewModel: LoginViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    // 1. Inisialisasi CameraController sekali saja
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
            bindToLifecycle(lifecycleOwner)
        }
    }

    // 2. Bungkus logika Scanner dalam fungsi yang bisa dipanggil ulang
    val startScanning = remember {
        {
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
            val scanner = BarcodeScanning.getClient(options)

            cameraController.setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(context),
                MlKitAnalyzer(
                    listOf(scanner),
                    COORDINATE_SYSTEM_VIEW_REFERENCED,
                    ContextCompat.getMainExecutor(context)
                ) { result ->
                    val barcodeResults = result.getValue(scanner)
                    // Pastikan tidak memproses scan jika sedang loading
                    if (!barcodeResults.isNullOrEmpty() && uiState !is LoginUiState.Loading) {
                        val rawValue = barcodeResults.firstOrNull()?.rawValue
                        if (rawValue != null) {
                            cameraController.clearImageAnalysisAnalyzer() // Stop scanner
                            context.vibratePhone()
                            viewModel.connectTable(rawValue) // Panggil API
                        }
                    }
                }
            )
        }
    }

    // 3. Jalankan scanner otomatis saat pertama kali masuk
    LaunchedEffect(Unit) {
        startScanning()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Tampilan Kamera (Layer paling bawah)
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    this.controller = cameraController
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Kotak Scanner
        QRScannerOverlay(instructionText = "Arahkan ke QR Code di Meja")

        // Tampilan Loading
        if (uiState is LoginUiState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        if (uiState is LoginUiState.Error) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = {
                    viewModel.resetState()
                    startScanning()
                },
                title = { Text(text = "Scan Gagal") },
                text = { Text(text = (uiState as LoginUiState.Error).message ?: "Terjadi kesalahan") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.resetState() // Hilangkan state error
                        startScanning()       // Aktifkan kamera lagi
                    }) {
                        Text("Coba Lagi")
                    }
                }
            )
        }
    }
}

fun Context.vibratePhone() {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(150)
    }
}