package com.example.rupaygo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rupaygo.TokenListScreen
import com.example.rupaygo.ui.components.HomeScreen
import com.example.rupaygo.ui.screens.*

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(navController)
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(navController)
        }

        composable(Routes.HOME) {
            HomeScreen(navController)
        }

        composable(Routes.ISSUE) {
            IssueScreen()
        }

        composable(Routes.TOKENS) {
            TokenListScreen()
        }

        composable(Routes.TRANSFER) {
            TransferScreen(navController)
        }

        composable(Routes.SEND) {
            SendScreen()
        }

        composable(Routes.RECEIVE) {
            ReceiveScreen()
        }
        composable(Routes.TRANSACTIONS) {
            TransactionScreen()
        }
        composable(Routes.DEBUG) {
            DebugScreen()
        }

        composable(Routes.GUIDE) {
            GuideScreen()
        }
        composable(Routes.ADD_MONEY) {
            AddMoneyScreen(navController)
        }
        /*composable(Routes.WITHDRAW) {
            WithdrawScreen(navController)
        }*/



    }
}
