package com.rahmat.testapp.ui.common

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
}
