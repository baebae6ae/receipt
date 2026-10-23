package com.baebae.receipt.ui.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.baebae.receipt.ads.AdBanner
import com.baebae.receipt.appContainer
import com.baebae.receipt.ui.archive.ArchiveScreen
import com.baebae.receipt.ui.onboarding.OnboardingScreen
import com.baebae.receipt.ui.permissions.PermissionsScreen
import com.baebae.receipt.ui.result.LIVE_WEEK
import com.baebae.receipt.ui.result.ResultScreen
import com.baebae.receipt.ui.screens.HomeScreen
import com.baebae.receipt.ui.settings.HiddenAppsScreen
import com.baebae.receipt.ui.settings.LicensesScreen
import com.baebae.receipt.ui.settings.PrivacyScreen
import com.baebae.receipt.ui.settings.SettingsScreen
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun ReceiptApp(pendingWeek: Long? = null, onPendingWeekHandled: () -> Unit = {}) {
    val prefs = appContainer().prefs
    val navController = rememberNavController()
    val startDestination = remember {
        if (prefs.onboardingDone) Destination.Home.route else Destination.Onboarding.route
    }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showTabs = bottomTabs.any { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.destination.route } == true
    }
    val openPermissions = { navController.navigate(Destination.Permissions.route(fromOnboarding = false)) }
    val container = appContainer()
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    // 새로 발행된 카드를 처음 열 때만 전면 광고(하루 횟수 제한)를 거친다. 결과 화면 자체에는 광고가 없다.
    val openPublished: (Long) -> Unit = { week ->
        scope.launch {
            val card = container.cards.get(LocalDate.ofEpochDay(week))
            val navigate = { navController.navigate(Destination.Result.route(week)) }
            if (card != null && !card.revealed && activity != null) {
                container.ads.showInterstitialThen(activity, navigate)
            } else {
                navigate()
            }
        }
    }

    // 알림에서 들어온 경우 해당 주 카드로 바로 간다.
    LaunchedEffect(pendingWeek) {
        if (pendingWeek != null && prefs.onboardingDone) openPublished(pendingWeek)
        if (pendingWeek != null) onPendingWeekHandled()
    }

    Scaffold(
        bottomBar = {
            if (showTabs) {
                ReceiptTabBar(navController = navController, currentRoute = currentDestination?.route)
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Destination.Onboarding.route) {
                fun finish(next: String) {
                    prefs.onboardingDone = true
                    navController.navigate(next) {
                        popUpTo(Destination.Onboarding.route) { inclusive = true }
                    }
                }
                OnboardingScreen(
                    onConnectRealData = { finish(Destination.Permissions.route(fromOnboarding = true)) },
                    onSkip = { finish(Destination.Home.route) },
                )
            }
            composable(
                route = Destination.Permissions.route,
                arguments = listOf(
                    navArgument(Destination.Permissions.ARG_ONBOARDING) {
                        type = NavType.BoolType
                        defaultValue = false
                    },
                ),
            ) { entry ->
                val fromOnboarding = entry.arguments?.getBoolean(Destination.Permissions.ARG_ONBOARDING) ?: false
                PermissionsScreen(
                    onDone = {
                        if (fromOnboarding) {
                            navController.navigate(Destination.Home.route) {
                                popUpTo(Destination.Permissions.route) { inclusive = true }
                            }
                        } else {
                            navController.popBackStack()
                        }
                    },
                )
            }
            composable(Destination.Home.route) {
                HomeScreen(
                    onOpenResult = { navController.navigate(Destination.Result.route(LIVE_WEEK)) },
                    onOpenPublished = openPublished,
                    onOpenPermissions = openPermissions,
                )
            }
            composable(
                route = Destination.Result.route,
                arguments = listOf(navArgument(Destination.Result.ARG_WEEK) { type = NavType.LongType }),
            ) { entry ->
                ResultScreen(weekEpochDay = entry.arguments?.getLong(Destination.Result.ARG_WEEK) ?: LIVE_WEEK)
            }
            composable(Destination.Archive.route) {
                ArchiveScreen(onOpen = { navController.navigate(Destination.Result.route(it.weekStart.toEpochDay())) })
            }
            composable(Destination.Settings.route) {
                SettingsScreen(
                    onOpenPermissions = openPermissions,
                    onOpenHiddenApps = { navController.navigate(Destination.HiddenApps.route) },
                    onOpenPrivacy = { navController.navigate(Destination.Privacy.route) },
                    onOpenLicenses = { navController.navigate(Destination.Licenses.route) },
                    bottomContent = { AdBanner() },
                )
            }
            composable(Destination.HiddenApps.route) { HiddenAppsScreen() }
            composable(Destination.Privacy.route) { PrivacyScreen() }
            composable(Destination.Licenses.route) { LicensesScreen() }
        }
    }
}

@Composable
private fun ReceiptTabBar(navController: NavHostController, currentRoute: String?) {
    Column(modifier = Modifier.navigationBarsPadding()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
        Row(modifier = Modifier.fillMaxWidth()) {
            bottomTabs.forEach { tab ->
                val selected = currentRoute == tab.destination.route
                TextButton(
                    onClick = {
                        navController.navigate(tab.destination.route) {
                            // 온보딩은 첫 이동 때 스택에서 빠지므로, 탭 이동의 기준점은 홈으로 둔다.
                            popUpTo(Destination.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = stringResource(tab.label),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) MaterialTheme.colorScheme.onBackground
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
