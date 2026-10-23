package com.baebae.receipt.ui.navigation

import androidx.annotation.StringRes
import com.baebae.receipt.R

sealed class Destination(val route: String) {
    data object Onboarding : Destination("onboarding")
    data object Home : Destination("home")
    data object Result : Destination("result")
    data object Archive : Destination("archive")
    data object Settings : Destination("settings")
}

class TabItem(val destination: Destination, @StringRes val label: Int)

// 하단 탭에 노출되는 화면. 온보딩과 결과 카드는 전체 화면으로 띄운다.
val bottomTabs = listOf(
    TabItem(Destination.Home, R.string.tab_home),
    TabItem(Destination.Archive, R.string.tab_archive),
    TabItem(Destination.Settings, R.string.tab_settings),
)
