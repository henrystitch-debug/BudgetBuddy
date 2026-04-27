package com.github.budgetbuddy.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.budgetbuddy.ui.expense.AddExpenseScreen
import com.github.budgetbuddy.ui.overview.OverviewScreen

// 1. Define the possible screens (routes) as strings
object Routes {
    const val MAIN = "main"
    const val OVERVIEW = "overview"
    const val ADD_EXPENSE = "add_expense"
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    // 2. NavHost defines the "map" of your app. 
    // It says: "If the current route is X, show Screen X"
    NavHost(
        navController = navController,
        startDestination = Routes.MAIN
    ) {
        // 3. Define the Main Screen
        composable(Routes.MAIN) {
            MainScreen(
                onNavigateToOverview = {
                    navController.navigate(Routes.OVERVIEW)
                },
                onNavigateToAddExpense = {
                    navController.navigate(Routes.ADD_EXPENSE)
                }
            )
        }

        // 4. Define the Overview Screen
        composable(Routes.OVERVIEW) {
            OverviewScreen()
        }

        // 5. Define the Add Expense Screen
        composable(Routes.ADD_EXPENSE) {
            AddExpenseScreen(
                onCancel = {
                    navController.popBackStack() // Go back to previous screen
                },
                onSave = { amount, note, category ->
                    // Handle saving logic here
                    navController.popBackStack()
                }
            )
        }
    }
}
