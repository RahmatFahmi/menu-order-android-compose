package com.rahmat.testapp.ui.customers.screen.product


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rahmat.testapp.R
import com.rahmat.testapp.domain.model.Menu
import com.rahmat.testapp.ui.common.ObserveAsEvents
import com.rahmat.testapp.ui.common.UiEvent
import com.rahmat.testapp.ui.components.EmptyMenuState
import com.rahmat.testapp.ui.components.MenuCard
import com.rahmat.testapp.ui.components.skeleton.MenuCardSkeleton
import com.rahmat.testapp.ui.components.skeleton.rememberShimmerAlpha
import com.rahmat.testapp.ui.theme.AppColors
import com.rahmat.testapp.ui.theme.AppDimens

@Composable
fun ProductListScreen(
    viewModel: ProductViewModel = hiltViewModel(),
    categoryTitle: String? = null,
    categoryName: String? = null,
    onBackClick: () -> Unit,
    onMenuClick: (Int) -> Unit = {}
){
    LaunchedEffect(categoryName) {
        viewModel.getProduct(false)
    }

    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val onRefresh = remember { { viewModel.getProduct(force = true) } }
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
    ProductListContent(
        isInitialLoading = uiState is ProductUiState.Loading,
        isRefreshing = isRefreshing,
        latestMenus = filteredMenus,
        errorMessage = (uiState as? ProductUiState.Error)?.message,
        categoryTitle = categoryTitle,
        onRefresh = onRefresh,
        onBackClick = onBackClick,
        onMenuClick = onMenuClick,
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        searchQuery = searchQuery,
        onCartClick = { id, name -> viewModel.addMenuToCart(id, name) }
    )


}

@Composable
fun ProductHeaderSection(
    title: String? = "S E M U A  M E N U",
    onBackClick: () -> Unit
){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                onBackClick()
            },
            modifier = Modifier.size(AppDimens.IconSizeMedium)
        ){
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_left),
                contentDescription = null,
                modifier = Modifier.size(AppDimens.IconSizeMedium),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title?.uppercase()?.chunked(1)?.joinToString(" ") ?: "S E M U A  M E N U",
                modifier = Modifier.align(Alignment.Center),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = AppDimens.titleTopBar
            )
        }
    }
}

@Composable
fun ProductSearchBarSection(
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
            placeholder = { Text("Cari Menu...", color = AppColors.TextSecondary) },
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
            Icon(
                painter = painterResource(id = R.drawable.ic_filter),
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListContent(
    isInitialLoading: Boolean,
    isRefreshing: Boolean,
    latestMenus: List<Menu>,
    errorMessage: String?,
    categoryTitle: String?,
    onRefresh: () -> Unit,
    onBackClick: () -> Unit,
    onMenuClick: (Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    searchQuery: String,
    onCartClick: (Int, String) -> Unit
){
    val context = LocalContext.current
    val viewModel: ProductViewModel = hiltViewModel()
    val pullToRefreshState = rememberPullToRefreshState()
    val shimmerAlpha = if (isInitialLoading) rememberShimmerAlpha() else 0f
    val showToast = remember(context) {
        { message: String -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
    }

    val listPadding = remember {
        PaddingValues(
            start = AppDimens.HorizontalScreenPadding,
            end = AppDimens.HorizontalScreenPadding,
            top = 8.dp,
            bottom = 32.dp
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
    ) { paddingValues ->

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ){
            val menus = latestMenus
            val isLoading = isInitialLoading

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
                    contentPadding = listPadding,
                    verticalArrangement = Arrangement.spacedBy(AppDimens.paddingItem)
                ){
                    when {
                        isLoading && menus.isEmpty() -> {
                            item(key = "search_bar") { ProductSearchBarSection(onSearchQueryChange, searchQuery) }
                            items(3, contentType = { "skeleton" }) {
                                MenuCardSkeleton(shimmerAlpha = shimmerAlpha)
                            }
                        }
                        else -> {
                            item(key = "header"){ ProductHeaderSection(categoryTitle,onBackClick)}
                            item(key = "search_bar"){ ProductSearchBarSection(onSearchQueryChange, searchQuery)}

                            errorMessage?.let {
                                item(key = "error_banner") {
                                    Text(text = it, color = MaterialTheme.colorScheme.error)
                                }
                            }
                            if(menus.isEmpty()){
                                item(key = "empty_menu"){
                                    EmptyMenuState(
                                        resId = R.drawable.ic_search,
                                        message = "Menu Tidak Ditemukan"
                                    )
                                }
                            }else{
                                items(items = menus, key = { it.id }, contentType = { "menu_card" }) { item ->
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
}
