package com.rahmat.testapp.ui.customers.screen.favorite

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.rahmat.testapp.R
import com.rahmat.testapp.domain.model.Menu
import com.rahmat.testapp.ui.components.EmptyMenuState
import com.rahmat.testapp.ui.components.MenuCard
import com.rahmat.testapp.ui.components.skeleton.MenuCardSkeleton
import com.rahmat.testapp.ui.components.skeleton.rememberShimmerAlpha
import com.rahmat.testapp.ui.theme.AppDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    viewModel: FavoriteViewModel = hiltViewModel(),
    onMenuClick: (Int) -> Unit
){
    LaunchedEffect(Unit) {
        viewModel.loadFavorite(force = false)
    }

    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    val shimmerAlpha = rememberShimmerAlpha()
    val onRefresh = remember { { viewModel.loadFavorite(force = true) } }
    val favoriteMenu: List<Menu> = (uiState as? FavoriteUiState.Success)?.favoriteMenus ?: emptyList()
    val loadingGetFavorite: Boolean = uiState is FavoriteUiState.Loading
    val context = LocalContext.current
    val showToast = remember(context) {
        { message: String -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ){
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
        ){
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = AppDimens.HorizontalScreenPadding),
                verticalArrangement = Arrangement.spacedBy(AppDimens.paddingItem)
            ){
                when{
                    loadingGetFavorite && favoriteMenu.isEmpty() -> {
                        items(3, contentType = { "skeleton" }) {
                            MenuCardSkeleton(shimmerAlpha = shimmerAlpha)
                        }
                    }
                    favoriteMenu.isNotEmpty() -> {
                        items(items = favoriteMenu, key = { it.id }, contentType = { "menu_card" }) { item ->
                            MenuCard(
                                menu = item,
                                onItemClick = { onMenuClick(item.id) },
                                onCartClick = { showToast("Added to cart") }
                            )
                        }
                    }
                    else -> {
                        item(key = "empty_favorite") {
                            EmptyMenuState(
                                resId = R.drawable.ic_hearth,
                                message = "Belum Ada Menu Favorit"
                            )
                        }
                    }
                }
            }
        }
    }
}
