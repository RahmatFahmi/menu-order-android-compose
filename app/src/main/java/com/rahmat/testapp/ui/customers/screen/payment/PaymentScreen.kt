package com.rahmat.testapp.ui.customers.screen.payment

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.rahmat.testapp.ui.theme.AppColors
import com.rahmat.testapp.ui.theme.AppDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    snapUrl: String,
    onNavigateBack: () -> Unit,
    onPaymentSuccess: () -> Unit,
    onPaymentPending: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = AppDimens.HorizontalScreenPadding)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 1. KIRI: Tombol Back
                IconButton(
                    modifier = Modifier.size(AppDimens.IconSizeMedium),
                    onClick = onNavigateBack
                ) {
                    Icon(
                        // Menggunakan ikon panah back otomatis sesuai arah bahasa HP
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali ke Keranjang",
                        modifier = Modifier.size(AppDimens.IconSizeSmall),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 2. TENGAH: Judul Halaman (Mengambil sisa ruang layar)
                Text(
                    modifier = Modifier.weight(1f),
                    text = "Pembayaran Aman",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center, // Teks rata tengah
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = AppDimens.titleTopBar
                )

                // 3. KANAN: Spacer Kosong penyeimbang (Ukurannya disamakan dengan IconButton kiri)
                Spacer(modifier = Modifier.size(AppDimens.IconSizeMedium))
            }
        }
    ) { innerPadding ->

        // Memanggil WebView secara Full Screen di dalam Scaffold
        PaymentWebView(
            snapUrl = snapUrl,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            onPaymentSuccess = {
                Toast.makeText(context, "Pembayaran Berhasil!", Toast.LENGTH_LONG).show()
                onPaymentSuccess()
            },
            onPaymentPending = {
                Toast.makeText(context, "Menunggu Pembayaran.", Toast.LENGTH_LONG).show()
                onPaymentPending()
            },
            onPaymentFailed = {
                Toast.makeText(context, "Pembayaran Gagal / Dibatalkan", Toast.LENGTH_LONG).show()
                onNavigateBack() // Pulang ke keranjang jika gagal
            }
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PaymentWebView(
    snapUrl: String,
    onPaymentSuccess: () -> Unit,
    onPaymentPending: () -> Unit,
    onPaymentFailed: () -> Unit,
    modifier: Modifier = Modifier
) {
    // State untuk mengontrol apakah halaman masih loading atau tidak
    var isLoadingWeb by remember { mutableStateOf(true) }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true

                    // Mengubah warna background default WebView agar tidak terlalu silau putih (opsional)
                    setBackgroundColor(0) // 0 berarti transparan, dia akan mengikuti warna background Scaffold kamu

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoadingWeb = true // Mulai loading saat halaman diminta
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoadingWeb = false // Matikan loading saat halaman selesai dirender total
                        }

                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            url?.let {
                                when {
                                    it.contains("status_code=200") || it.contains("success") -> {
                                        onPaymentSuccess()
                                        return true
                                    }
                                    it.contains("status_code=201") || it.contains("pending") -> {
                                        onPaymentPending()
                                        return true
                                    }
                                    it.contains("status_code=202") || it.contains("failed") -> {
                                        onPaymentFailed()
                                        return true
                                    }
                                }
                            }
                            return false
                        }
                    }
                }
            },
            update = { webView ->
                webView.loadUrl(snapUrl)
            }
        )

        // Jika web masih loading, tampilkan indikator loading di atas WebView
        if (isLoadingWeb) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = AppColors.AccentMenu // Sesuaikan dengan warna tema aplikasimu
            )
        }
    }
}