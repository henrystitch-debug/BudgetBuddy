package com.github.budgetbuddy.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OverviewScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F8F1))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Overview", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B3A1B))
        Text("October 2023", color = Color(0xFF4A7A4A))

        Spacer(modifier = Modifier.height(20.dp))

        // Donut Chart Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Spending Breakdown", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp).padding(16.dp)) {
                    CircularProgressIndicator(
                        progress = 0.4f,
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 24.dp,
                        color = Color(0xFF3B6D11),
                        trackColor = Color(0xFFE8F5E9)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$1,200", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Total Spent", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OverviewPreview() {
    OverviewScreen()
}
