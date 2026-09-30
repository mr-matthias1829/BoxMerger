package com.boxmerger.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.boxmerger.GameViewModel

@Composable
fun StatsScreen(viewModel: GameViewModel) {
    val hours = viewModel.playtimeSeconds / 3600
    val minutes = (viewModel.playtimeSeconds % 3600) / 60
    val seconds = viewModel.playtimeSeconds % 60
    val playtimeStr = if (hours > 0) String.format("%dh %dm %ds", hours, minutes, seconds)
    else String.format("%dm %ds", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Game Statistics", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        StatRow("Manual Merges", "${viewModel.manualMerges}")
        StatRow("Automatic Merges", "${viewModel.autoMerges}")
        StatRow("Total Merges", "${viewModel.totalMerges}")
        StatRow("Total Playtime", playtimeStr)
        StatRow("Total Prestiges", "${viewModel.totalPrestiges}")
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}
