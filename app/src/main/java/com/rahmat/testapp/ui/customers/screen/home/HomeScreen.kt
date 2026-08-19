package com.rahmat.testapp.ui.customers.screen.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rahmat.testapp.R
import com.rahmat.testapp.common.utils.getGreetingMessage
import com.rahmat.testapp.domain.model.Menu
import com.rahmat.testapp.ui.common.ObserveAsEvents
import com.rahmat.testapp.ui.common.UiEvent
import com.rahmat.testapp.ui.components.CategoryItem
import com.rahmat.testapp.ui.components.EmptyMenuState
import com.rahmat.testapp.ui.components.MenuCard
import com.rahmat.testapp.ui.components.skeleton.MainCategoriesSkeleton
import com.rahmat.testapp.ui.components.skeleton.MainHeaderSkeleton
import com.rahmat.testapp.ui.components.skeleton.MenuCardSkeleton
import com.rahmat.testapp.ui.components.skeleton.SearchBarSkeleton
import com.rahmat.testapp.ui.components.skeleton.rememberShimmerAlpha
import com.rahmat.testapp.ui.navigation.Routes
import com.rahmat.testapp.ui.theme.AppColors
import com.rahmat.testapp.ui.theme.AppDimens
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onCategoryClick: (String, String) -> Unit,
    onMenuClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val onRefresh = remember { { viewModel.getHomeData(force = true) } }
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredMenus by viewModel.filteredMenus.collectAsState()

    ObserveAsEvents(viewModel.uiEvent) { event ->
        when (event) {
            is UiEvent.ShowToast -> {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    HomeContent(
        isInitialLoading = uiState is HomeUiState.Loading,
        isRefreshing = isRefreshing,
        latestMenus = filteredMenus,
        errorMessage = (uiState as? HomeUiState.Error)?.message,
        onRefresh = onRefresh,
        onCategoryClick = onCategoryClick,
        onMenuClick = onMenuClick,
        onSearchQueryChange = {viewModel.onSearchQueryChange(it)},
        searchQuery = searchQuery,
        onCartClick = { id, name -> viewModel.addMenuToCart(id, name) }
    )
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val greeting = remember { getGreetingMessage() }

        Column(modifier = Modifier.weight(1f)) {
            Text(greeting, fontSize = AppDimens.titleLarge, fontWeight = FontWeight.ExtraBold, color = AppColors.TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Temukan Menu\nFavoritmu", fontSize = 16.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
        }
        Image(
            painter = painterResource(id = R.drawable.img_study_illustration),
            contentDescription = null,
            modifier = Modifier.size(100.dp)
        )
    }
}

@Composable
fun SearchBarSection(
    onSearchQueryChange: (String) -> Unit,
    searchQuery: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {onSearchQueryChange(it)},
            placeholder = { Text("Search Menu", color = AppColors.TextSecondary) },
            modifier = Modifier.weight(1f).scale(1f, 0.9f),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = AppColors.BorderSecondary,
                focusedBorderColor = AppColors.BorderPrimary,
            ),
            singleLine = true
        )
        Box(
            modifier = Modifier.size(48.dp).background(AppColors.SearchButton, shape = RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
fun CategoriesSection(
    onCategoryClick: (String, String) -> Unit
) {
    Column {
        Text("Categories", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
        Spacer(modifier = Modifier.height(AppDimens.paddingItem))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            CategoryItem("All", R.drawable.ic_all_category,
                onClick = { onCategoryClick("semua menu", "Semua") })
            CategoryItem("Foods", R.drawable.ic_food_category,
                onClick = { onCategoryClick("makanan", "Makanan") })
            CategoryItem("Drinks", R.drawable.ic_drink_category,
                onClick = { onCategoryClick("Minuman", "Minuman") })
            CategoryItem("Discount", R.drawable.ic_discount,
                onClick = { onCategoryClick("Diskon", "Diskon") })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    isInitialLoading: Boolean,
    isRefreshing: Boolean,
    latestMenus: List<Menu>,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onCategoryClick: (String, String) -> Unit,
    onMenuClick: (Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    searchQuery: String,
    onCartClick: (Int, String) -> Unit
) {
    val context = LocalContext.current
    val pullToRefreshState = rememberPullToRefreshState()
    val shimmerAlpha = if (isInitialLoading) rememberShimmerAlpha() else 0f
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
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    start = AppDimens.HorizontalScreenPadding,
                    end = AppDimens.HorizontalScreenPadding,
                    top = 0.dp,
                    bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(AppDimens.paddingItem)
            ) {
                when{
                    isInitialLoading && latestMenus.isEmpty() ->{
                        item(key = "header_skeleton") { MainHeaderSkeleton(shimmerAlpha) }
                        item(key = "search_skeleton") { SearchBarSkeleton(shimmerAlpha) }
                        item(key = "categories_skeleton") { MainCategoriesSkeleton(shimmerAlpha) }
                        item(key = "label_skeleton") {
                            Box(modifier = Modifier.fillMaxWidth(0.2f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(Color.LightGray.copy(alpha = shimmerAlpha)))
                        }
                        items(3, contentType = { "skeleton" }) {
                            MenuCardSkeleton(shimmerAlpha = shimmerAlpha)
                        }
                    }
                    else -> {
                        item(key = "header") { HeaderSection() }
                        item(key = "search_bar") { SearchBarSection( onSearchQueryChange = onSearchQueryChange, searchQuery = searchQuery ) }
                        item(key = "categories_section") { CategoriesSection(onCategoryClick = { title, name ->
                            onCategoryClick(title, name)
                        }) }
                        item(key = "label_menu") {
                            Text("Menu", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        errorMessage?.let {
                            item(key = "error_banner") {
                                Text(text = it, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        if(latestMenus.isEmpty() && !isInitialLoading && !isRefreshing) {
                            item(key = "empty_menu"){
                                EmptyMenuState(
                                    resId = R.drawable.ic_search,
                                    message = "Menu Tidak Ditemukan"
                                )
                            }
                        }else{
                            items(items = latestMenus, key = { it.id }, contentType = { "menu_card" }) { item ->
                                MenuCard(
                                    menu = item,
                                    onItemClick = { onMenuClick(item.id) },
                                    onCartClick = { onCartClick(item.id, item.name) }
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}


