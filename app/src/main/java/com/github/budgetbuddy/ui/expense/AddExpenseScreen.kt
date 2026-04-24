package com.github.budgetbuddy.ui.expense

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.budgetbuddy.viewmodel.ExpenseViewModel
import kotlin.collections.emptyList

@Composable
fun ExpenseListScreen(
    viewModel: ExpenseViewModel = viewModel()  // Java ViewModel, no issue
) {
    val expenses by viewModel.allExpenses.observeAsState(emptyList())

    LazyColumn {
        items(expenses) { expense ->
            ExpenseRow(expense)
        }
    }
}

fun observeAsState(emptyList: Any) {
        TODO("Not yet implemented")
}

@Composable
fun ExpenseRow(x0: Int) {
    TODO("Not yet implemented")
}

fun observeAsState(emptyList: Any) {
        TODO("Not yet implemented")
}
