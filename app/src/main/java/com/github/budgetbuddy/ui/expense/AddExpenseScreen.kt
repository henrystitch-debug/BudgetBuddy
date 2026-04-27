package com.github.budgetbuddy.ui.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.budgetbuddy.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onSave: (Double, String, String) -> Unit = { _, _, _ -> },
    onCancel: () -> Unit = {}
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food") }

    val categories = listOf(
        CategoryItem("Food", R.drawable.outline_change_circle_24),
        CategoryItem("Home", R.drawable.outline_change_circle_24),
        CategoryItem("Transport", R.drawable.outline_change_circle_24),
        CategoryItem("School", R.drawable.outline_change_circle_24),
        CategoryItem("Health", R.drawable.outline_change_circle_24),
        CategoryItem("Shopping", R.drawable.outline_change_circle_24),
        CategoryItem("Fun", R.drawable.outline_change_circle_24),
        CategoryItem("Other", R.drawable.outline_change_circle_24)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8E1))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.add_expense),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 32.dp)
                    .align(Alignment.Center)
            )
            TextButton(
                onClick = onCancel,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = Color(0xFFA32D2D)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Amount and Note Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3C4)),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.amount),
                    fontSize = 14.sp,
                    color = Color(0xFF8A6E00)
                )
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.number)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A3800)
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFFC9A800),
                        unfocusedIndicatorColor = Color(0xFFC9A800)
                    )
                )
                TextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    placeholder = { Text(stringResource(R.string.add_note)) },
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 14.sp,
                        color = Color(0xFF4A3800)
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.category),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4A3800),
            modifier = Modifier.align(Alignment.Start).padding(start = 8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(categories) { category ->
                CategoryGridItem(
                    category = category,
                    isSelected = selectedCategory == category.name,
                    onClick = { selectedCategory = category.name }
                )
            }
        }

        Button(
            onClick = { onSave(amount.toDoubleOrNull() ?: 0.0, note, selectedCategory) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A3800)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = stringResource(R.string.save_expense),
                color = Color(0xFFFFF8E1)
            )
        }
    }
}

@Composable
fun CategoryGridItem(
    category: CategoryItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .background(
                    if (isSelected) Color(0xFFC9A800) else Color(0xFFFFF3C4),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                painter = painterResource(id = category.iconRes),
                contentDescription = category.name,
                tint = Color(0xFF4A3800)
            )
        }
        Text(
            text = category.name,
            fontSize = 12.sp,
            color = Color(0xFF4A3800),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

data class CategoryItem(val name: String, val iconRes: Int)

@Preview(showBackground = true)
@Composable
fun AddExpensePreview() {
    AddExpenseScreen()
}
