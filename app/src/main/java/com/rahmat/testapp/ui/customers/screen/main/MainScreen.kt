package com.rahmat.testapp.ui.customers.screen.main


import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rahmat.testapp.R
import com.rahmat.testapp.common.utils.theme.ThemeViewModel
import com.rahmat.testapp.ui.customers.navigation.BottomBarItem
import com.rahmat.testapp.ui.customers.screen.cart.CartScreen
import com.rahmat.testapp.ui.customers.screen.favorite.FavoriteScreen
import com.rahmat.testapp.ui.customers.screen.home.HomeScreen
import com.rahmat.testapp.ui.customers.screen.order.MainOrderScreen
import com.rahmat.testapp.ui.theme.AppDimens
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    viewModel: ThemeViewModel,
    onCategoryClick: (String, String) -> Unit,
    onMenuClick: (Int) -> Unit,
    onNavigateToPayment: (String) -> Unit,
    onOrderSuccess: () -> Unit,
    onCustomerNotificationClick: () -> Unit
){
    val navController = rememberNavController()
    var titleTopBar by remember { mutableStateOf("B E R A N D A") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current


    val screens = listOf(
        BottomBarItem.Home,
        BottomBarItem.Favorite,
        BottomBarItem.Cart,
        BottomBarItem.History
    )

    var showExitDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = true) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Keluar Aplikasi") },
            text = { Text("Apakah Anda yakin ingin keluar dari OrderApp?") },
            confirmButton = {
                TextButton(onClick = {
                    (context as? Activity)?.finishAffinity()
                }) {
                    Text("Ya, Keluar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(300.dp),
                    drawerContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ){
                    HorizontalDivider()
                    NavigationDrawerItem(
                        label = { Text("Home") },
                        selected = false,
                        onClick = { }
                    )

                    NavigationDrawerItem(
                        label = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tema Gelap")
                                Switch(
                                    checked = viewModel.isDark,
                                    onCheckedChange = { viewModel.toggleTheme() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF3883FB),
                                        checkedTrackColor = Color(0xFFD8E7FF),
                                        uncheckedThumbColor = Color.Gray,
                                        uncheckedTrackColor = Color.LightGray
                                    ),
                                    modifier = Modifier.scale(0.8f).padding(0.dp)

                                )
                            }
                        },
                        selected = false,
                        onClick = { },
                        icon = { Icon(Icons.Rounded.NightsStay, contentDescription = null) }
                    )

                }
            }
        )
        {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    Row(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(horizontal = AppDimens.HorizontalScreenPadding)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    )
                    {
                        IconButton(
                            modifier = Modifier.size(AppDimens.IconSizeMedium),
                            onClick = {
                                scope.launch {
                                    drawerState.apply {
                                        if (isClosed) open() else close()
                                    }
                                }
                            }
                        ){
                            Icon(
                                painter = painterResource(id = R.drawable.ic_sidebar),
                                contentDescription = "Menu Icon",
                                modifier = Modifier
                                    .size(AppDimens.IconSizeSmall),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            modifier = Modifier
                                .weight(1f),
                            text = titleTopBar,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = AppDimens.titleTopBar
                        )

                        IconButton(
                            modifier = Modifier.size(AppDimens.IconSizeMedium),
                            onClick = {onCustomerNotificationClick()}
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_notification),
                                contentDescription = "Notification Icon",
                                modifier = Modifier.size(AppDimens.IconSizeSmall),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier.height(70.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ){
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        val currentScreen = screens.find { it.route == currentDestination?.route }
                        titleTopBar = remember(currentScreen) {
                            currentScreen?.title?.uppercase()?.chunked(1)?.joinToString(" ") ?: "B E R A N D A"
                        }

                        screens.forEach { screens ->
                            NavigationBarItem(
                                label = {Text(text = screens.title)},
                                icon = {
                                    Icon(
                                        painter = painterResource(screens.icon),
                                        contentDescription = screens.title,
                                        modifier = Modifier.size(AppDimens.IconSizeSmall)
                                    )
                                },
                                selected = currentDestination?.hierarchy?.any {it.route == screens.route} == true,
                                onClick = {
                                    navController.navigate(screens.route){
                                        popUpTo(navController.graph.findStartDestination().id){
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                                )
                            )
                        }
                    }
                }
            )
            {innerPadding ->
                NavHost(
                    modifier = Modifier.padding(innerPadding),
                    navController = navController,
                    startDestination = BottomBarItem.Home.route,
                    enterTransition = { fadeIn(tween(300)) },
                    exitTransition = { fadeOut(tween(300)) }
                ){
                    composable(BottomBarItem.Home.route){
                        HomeScreen(onCategoryClick = { title, name ->
                            onCategoryClick(title, name)
                        }, onMenuClick = onMenuClick)
                    }
                    composable(BottomBarItem.Favorite.route){
                        FavoriteScreen(onMenuClick = onMenuClick)
                    }
                    composable(BottomBarItem.Cart.route){
                        CartScreen(
                            onNavigateToPayment = onNavigateToPayment,
                            onOrderSuccess = onOrderSuccess
                        )
                    }
                    composable(BottomBarItem.History.route){
                        MainOrderScreen(onNavigateToPayment = onNavigateToPayment)
                    }
                }
            }
        }


}
