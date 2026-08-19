package com.rahmat.testapp.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rahmat.testapp.common.utils.theme.ThemeViewModel
import com.rahmat.testapp.data.repository.AuthRepositoryImpl
import com.rahmat.testapp.ui.customers.screen.detail.ProductDetailScreen
import com.rahmat.testapp.ui.customers.screen.main.MainScreen
import com.rahmat.testapp.ui.customers.screen.notification.CustomerNotificationScreen
import com.rahmat.testapp.ui.customers.screen.payment.PaymentScreen
import com.rahmat.testapp.ui.customers.screen.product.ProductListScreen
import com.rahmat.testapp.ui.customers.screen.scan.QRScannerScreen
import com.rahmat.testapp.ui.employee.screen.EmployeeMainScreen
import com.rahmat.testapp.ui.publicscreen.auth.login.LoginScreen
import com.rahmat.testapp.ui.publicscreen.auth.login.LoginViewModel
import com.rahmat.testapp.ui.publicscreen.auth.register.RegistrationScreen
import com.rahmat.testapp.ui.publicscreen.auth.welcome.SplashScreen
import com.rahmat.testapp.ui.publicscreen.auth.welcome.WelcomeScreen
import okhttp3.Route

@Composable
fun AppNavGraph(themeViewModel: ThemeViewModel){
    val navController = rememberNavController()


    NavHost(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        navController = navController,
        startDestination = Routes.Splash,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) +
                    scaleIn(initialScale = 0.9f, animationSpec = tween(300))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(200))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) +
                    scaleIn(initialScale = 0.9f, animationSpec = tween(300))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(200)) +
                    scaleOut(targetScale = 0.9f, animationSpec = tween(200))
        }
    ){

        composable(Routes.Splash){
            SplashScreen(onNavigateToNextScreen = {
                navController.navigate(Routes.WelcomeScreen){
                    popUpTo(Routes.Splash){
                        inclusive = true
                    }
                }
            })
        }

        composable(Routes.Login){
            val loginViewModel: LoginViewModel = hiltViewModel()
            LoginScreen(onLoginSuccess = { user ->
                if (user.role == "karyawan"){
                    navController.navigate(Routes.EmployeeMainScreen){
                        popUpTo(Routes.Login){
                            inclusive = true
                        }
                    }
                }
            },
                viewModel = loginViewModel
            )
        }

        composable(Routes.ScanQR) {
            QRScannerScreen(
                onQrCodeDetected = { user ->
                    navController.navigate(Routes.MainScreen){
                        popUpTo(Routes.ScanQR){
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Routes.MainScreen){
            MainScreen(
                viewModel = themeViewModel,
                onCategoryClick = { title, name ->
                    navController.navigate("productList/$title/$name")
                },
                onMenuClick = { id ->
                    navController.navigate("detail/$id")
                },
                onNavigateToPayment = { url ->
                    navController.navigate("payment/$url")
                },
                onOrderSuccess = {
                    //todo
                },
                onCustomerNotificationClick = {
                    navController.navigate(Routes.CustomerNotificatioScreen)
                }
            )
        }

        composable (Routes.EmployeeMainScreen){
            EmployeeMainScreen(viewModel = themeViewModel)
        }

        composable(Routes.WelcomeScreen){
            WelcomeScreen(
                onEmployeeClick = {
                    navController.navigate(Routes.Login)
                },
                onCustomerClick = {
                    navController.navigate(Routes.ScanQR)
                }
            )
        }
        composable (
            route = Routes.ProductListScreen,
            arguments = listOf(
                navArgument("categoryName") { type = NavType.StringType },
                navArgument("categoryTitle") { type = NavType.StringType }
            )
        ){ backStackEntry ->
            val name = backStackEntry.arguments?.getString("categoryName") ?: "all"
            val title = backStackEntry.arguments?.getString("categoryTitle") ?: "Semua Menu"

            ProductListScreen(
                categoryTitle = title,
                categoryName = name,
                onBackClick = { navController.popBackStack() },
                onMenuClick = { id ->
                    navController.navigate("detail/$id")
                }
            )
        }

        composable(
            route = Routes.DetailScreen,
            arguments = listOf(
                navArgument("menuId") { type = NavType.IntType }
            )
        ){
            ProductDetailScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onMenuClick = { id ->
                    navController.navigate("detail/$id")
                }
            )
        }

        composable(
            route = Routes.PaymentScreen,
            arguments = listOf(navArgument("snapUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            // Mengambil URL yang di-encode dari argument navigasi
            val encodedUrl = backStackEntry.arguments?.getString("snapUrl") ?: ""
            // Decode kembali menjadi URL murni
            val decodedUrl = java.net.URLDecoder.decode(encodedUrl, java.nio.charset.StandardCharsets.UTF_8.toString())

            PaymentScreen(
                snapUrl = decodedUrl,
                onNavigateBack = { navController.popBackStack() },
                onPaymentSuccess = {
//                    navController.navigate("home_customer") {
//                        popUpTo("cart_screen") { inclusive = true }
//                    }
                },
                onPaymentPending = {
//                    navController.navigate("orders_history") {
//                        popUpTo("cart_screen") { inclusive = true }
//                    }
                }
            )
        }

        composable(route = Routes.CustomerNotificatioScreen){
            CustomerNotificationScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

    }

}

