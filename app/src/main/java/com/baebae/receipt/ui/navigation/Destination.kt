package com.baebae.receipt.ui.navigation

import androidx.annotation.StringRes
import com.baebae.receipt.R

sealed class Destination(val route: String) {
    data object Onboarding : Destination("onboarding")
    data object Home : Destination("home")
    data object Archive : Destination("archive")
    data object Settings : Destination("settings")
    data object HiddenApps : Destination("settings/hidden-apps")
    data object Privacy : Destination("settings/privacy")
    data object Licenses : Destination("settings/licenses")

    /** week = 그 주 월요일의 epochDay, 진행 중인 주는 LIVE_WEEK(-1). */
    data object Result : Destination("result/{week}") {
        const val ARG_WEEK = "week"
        fun route(weekEpochDay: Long) = "result/$weekEpochDay"
    }

    data object Permissions : Destination("permissions?onboarding={onboarding}") {
        const val ARG_ONBOARDING = "onboarding"
        fun route(fromOnboarding: Boolean) = "permissions?onboarding=$fromOnboarding"
    }
}

class TabItem(val destination: Destination, @StringRes val label: Int)

// 하단 탭에 노출되는 화면. 온보딩과 결과 카드는 전체 화면으로 띄운다.
val bottomTabs = listOf(
    TabItem(Destination.Home, R.string.tab_home),
    TabItem(Destination.Archive, R.string.tab_archive),
    TabItem(Destination.Settings, R.string.tab_settings),
)
