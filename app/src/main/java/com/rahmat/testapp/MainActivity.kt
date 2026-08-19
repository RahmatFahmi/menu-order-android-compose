package com.rahmat.testapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.messaging.FirebaseMessaging
import com.rahmat.testapp.common.utils.theme.ThemeViewModel
import com.rahmat.testapp.ui.navigation.AppNavGraph
import com.rahmat.testapp.ui.theme.TestAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FCM_PERMISSION", "Izin notifikasi disetujui oleh pengguna.")
            Toast.makeText(this, "Izin notifikasi diaktifkan!", Toast.LENGTH_SHORT).show()
        } else {
            Log.w("FCM_PERMISSION", "Izin notifikasi ditolak oleh pengguna.")
            Toast.makeText(this, "Izin notifikasi ditolak.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val splashscreen = installSplashScreen()
//        var keepSplashScreen = true
//        super.onCreate(savedInstanceState)
//        splashscreen.setKeepOnScreenCondition { keepSplashScreen }
//        lifecycleScope.launch {
//            delay(5000)
//            keepSplashScreen = false
//        }
        enableEdgeToEdge()
        // 1. Cek dan minta izin notifikasi
        checkAndRequestNotificationPermission()

        // 2. Cek Token FCM HP ini di Logcat
        fetchAndLogFcmToken()

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()

            TestAppTheme(darkTheme = themeViewModel.isDark) {
                AppNavGraph(themeViewModel = themeViewModel)
            }
        }
    }

    private fun checkAndRequestNotificationPermission() {
        val sdkVersion = Build.VERSION.SDK_INT
        Log.d("FCM_PERMISSION", "Versi Android Device (API Level): $sdkVersion")

        if (sdkVersion >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                Log.d("FCM_PERMISSION", "Meminta izin POST_NOTIFICATIONS ke pengguna...")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Log.d("FCM_PERMISSION", "Izin POST_NOTIFICATIONS sudah aktif sebelumnya.")
            }
        } else {
            Log.d("FCM_PERMISSION", "Android < 13 detected. Izin notifikasi aktif secara otomatis.")
        }
    }

    private fun fetchAndLogFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("FCM_TOKEN", "Gagal mengambil FCM Token", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM_TOKEN", "FCM Token HP saat ini: $token")
        }
    }
}