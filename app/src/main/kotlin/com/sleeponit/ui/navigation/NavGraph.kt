package com.sleeponit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sleeponit.ui.add.AddPurchaseScreen
import com.sleeponit.ui.edit.EditPurchaseScreen
import com.sleeponit.ui.list.PurchaseListScreen
import com.sleeponit.ui.settings.SettingsScreen
import com.sleeponit.ui.stats.StatsScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            PurchaseListScreen(
                onAddClick = { navController.navigate("add") },
                onSettingsClick = { navController.navigate("settings") },
                onEditClick = { purchase -> navController.navigate("edit/${purchase.id}") },
                onStatsClick = { navController.navigate("stats") }
            )
        }
        composable("add") {
            AddPurchaseScreen(onDone = { navController.popBackStack() })
        }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = "edit/{purchaseId}",
            arguments = listOf(navArgument("purchaseId") { type = NavType.LongType })
        ) {
            EditPurchaseScreen(onDone = { navController.popBackStack() })
        }
        composable("stats") {
            StatsScreen(onBack = { navController.popBackStack() })
        }
    }
}
