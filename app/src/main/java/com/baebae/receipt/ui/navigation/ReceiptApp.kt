package com.baebae.receipt.ui.navigation

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.baebae.receipt.ui.screens.ArchiveScreen
import com.baebae.receipt.ui.screens.HomeScreen
import com.baebae.receipt.ui.screens.OnboardingScreen
import com.baebae.receipt.ui.screens.ResultScreen
import com.baebae.receipt.ui.screens.SettingsScreen

@Composable
fun ReceiptApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showTabs = bottomTabs.any { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.destination.route } == true
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
            // 첫 실행 여부 저장은 온보딩 구현 단계에서 붙인다.
            startDestination = Destination.Onboarding.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Destination.Onboarding.route) {
                OnboardingScreen(
                    onStart = {
                        navController.navigate(Destination.Home.route) {
                            popUpTo(Destination.Onboarding.route) { inclusive = true }
                        }
                    },
                )
            }
            composable(Destination.Home.route) {
                HomeScreen(onOpenResult = { navController.navigate(Destination.Result.route) })
            }
            composable(Destination.Result.route) {
                ResultScreen()
            }
            composable(Destination.Archive.route) { ArchiveScreen() }
            composable(Destination.Settings.route) { SettingsScreen() }
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
