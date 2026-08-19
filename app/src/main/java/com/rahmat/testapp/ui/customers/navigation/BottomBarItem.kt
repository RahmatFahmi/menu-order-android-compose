package com.rahmat.testapp.ui.customers.navigation

import com.rahmat.testapp.R


sealed class BottomBarItem(val route: String, val title: String, val icon: Int){

    object Home : BottomBarItem("home", "Beranda", R.drawable.ic_home)
    object Favorite : BottomBarItem("favorite", "Favorit", R.drawable.ic_heart_filled)
    object Cart : BottomBarItem("cart", "Keranjang", R.drawable.ic_cart_filled)
    object History : BottomBarItem("history", "Riwayat", R.drawable.ic_history)
}