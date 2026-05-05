package com.sleeponit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sleeponit.ui.add.AddPurchaseScreen
import com.sleeponit.ui.list.PurchaseListScreen
import com.sleeponit.ui.settings.SettingsScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            PurchaseListScreen(
                onAddClick = { navController.navigate("add") },
                onSettingsClick = { navController.navigate("settings") }
            )
        }
        composable("add") {
            AddPurchaseScreen(onDone = { navController.popBackStack() })
        }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
