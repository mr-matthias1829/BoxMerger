package com.boxmerger.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.boxmerger.GameViewModel
import java.util.Locale

private fun fmt(n: Long): String = String.format(Locale.US, "%,d", n)

@Composable
fun StatsScreen(viewModel: GameViewModel) {
    // playtimeSeconds changes every second, so reading it keeps this screen live
    val hours = viewModel.playtimeSeconds / 3600
    val minutes = (viewModel.playtimeSeconds % 3600) / 60
    val seconds = viewModel.playtimeSeconds % 60
    val playtimeStr = if (hours > 0)
        String.format("%dh %dm %ds", hours, minutes, seconds)
    else
        String.format("%dm %ds", minutes, seconds)

    val stats = viewModel.stats

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Game Statistics",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        StatSection("Merging")
        StatRow("Manual Merges", fmt(stats.get("manual_merges")))
        StatRow("Automatic Merges", fmt(stats.get("auto_merges")))
        StatRow("Total Merges", fmt(stats.get("total_merges")))

        if (viewModel.hasFlag("steel_unlocked")) {
            StatSection("Steel")
            // Number of payouts, unaffected by how much each payout was worth
            StatRow("Raw Steel Collects", fmt(stats.get("steel_collects")))
        }

        StatSection("Records")
        StatRow("Highest Tier Reached", fmt(stats.get("highest_tier")))
        if (viewModel.totalPrestiges > 0) {
            StatRow("Highest Prestige Reached", viewModel.highestPrestige.toPrettyString())
        }
        if (viewModel.hasFlag("leveling_unlocked")) {
            StatRow("Highest Level Reached", fmt(stats.get("highest_level")))
        }

        StatSection("Progress")
        StatRow("Total Playtime", playtimeStr)
        StatRow("Total Prestiges", fmt(stats.get("total_prestiges")))
        if (viewModel.hasFlag("level_reset_unlocked")) {
            StatRow("Total Level Resets", fmt(viewModel.levelResets.toLong()))
        }
    }
}

@Composable
fun StatSection(title: String) {
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
    )
    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}