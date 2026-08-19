package com.rahmat.testapp.ui.employee.navigation

sealed class TabRowItem(val route: String, val title: String) {
    object Pending : TabRowItem("PendingOrder", "Tertunda")
    object Queue : TabRowItem("AntreanOrder", "Antrean")
    object Process : TabRowItem("ProcessOrder", "Proses")
    object Finish : TabRowItem("FinishOrder", "Selesai")
}