package com.rahmat.testapp.ui.employee.screen

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rahmat.testapp.R
import com.rahmat.testapp.common.utils.theme.ThemeViewModel
import com.rahmat.testapp.ui.components.CustomBackgroundLayout
import com.rahmat.testapp.ui.employee.navigation.TabRowItem
import com.rahmat.testapp.ui.theme.AppDimens
import kotlinx.coroutines.launch
import androidx.compose.material3.Tab
import androidx.compose.ui.tooling.preview.Preview
import com.rahmat.testapp.ui.customers.screen.order.MainOrderScreen
import com.rahmat.testapp.ui.employee.screen.finish.EmployeeFinishedOrderScreeen
import com.rahmat.testapp.ui.employee.screen.pending.EmployeePendingOrderScreeen
import com.rahmat.testapp.ui.employee.screen.process.EmployeeProcessOrderScreeen
import com.rahmat.testapp.ui.employee.screen.queue.EmployeeQueueOrderScreen
import dagger.hilt.android.lifecycle.HiltViewModel

@Composable
fun EmployeeMainScreen(
    viewModel: ThemeViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val screens = listOf(
        TabRowItem.Pending,
        TabRowItem.Queue,
        TabRowItem.Process,
        TabRowItem.Finish
    )

    val pagerState = rememberPagerState(pageCount = { screens.size })
    val selectedTabIndex = pagerState.currentPage

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
                }) { Text("Ya, Keluar") }
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
            ) {
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
                                modifier = Modifier
                                    .scale(0.8f)
                                    .padding(0.dp)
                            )
                        }
                    },
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Rounded.NightsStay, contentDescription = null) }
                )
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Column(Modifier.statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(horizontal = AppDimens.HorizontalScreenPadding)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            modifier = Modifier.size(AppDimens.IconSizeMedium),
                            onClick = {
                                scope.launch {
                                    drawerState.apply {
                                        if (isClosed) open() else close()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_sidebar),
                                contentDescription = "Menu Icon",
                                modifier = Modifier.size(AppDimens.IconSizeSmall),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            modifier = Modifier.weight(1f),
                            text = screens[selectedTabIndex].title
                                .uppercase()
                                .chunked(1)
                                .joinToString(" "),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(
                            modifier = Modifier.size(AppDimens.IconSizeMedium),
                            onClick = {}
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_notification),
                                contentDescription = "Notification Icon",
                                modifier = Modifier.size(AppDimens.IconSizeSmall),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(Modifier.height(AppDimens.PaddingMedium))

                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        divider = { HorizontalDivider() }
                    ) {
                        screens.forEachIndexed { index, item ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                text = {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selectedTabIndex == index)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { page ->
                when (page) {
                    0 -> EmployeePendingOrderScreeen()
                    1 -> EmployeeQueueOrderScreen()
                    2 -> EmployeeProcessOrderScreeen()
                    3 -> EmployeeFinishedOrderScreeen()
                }
            }
        }
    }
}
