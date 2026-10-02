package com.boxmerger.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boxmerger.GameViewModel
import com.boxmerger.model.Achievement

@Composable
fun AchievementsScreen(viewModel: GameViewModel) {
    val all = viewModel.achievementManager.getAll()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Achievements",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val visible = viewModel.achievementManager.getVisible()
            items(visible) { achievement ->
                AchievementItem(
                    achievement = achievement,
                    isUnlocked = viewModel.achievementManager.isUnlocked(achievement.id)
                )
            }
        }
    }
}

@Composable
fun AchievementItem(achievement: Achievement, isUnlocked: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Correct resolver — looks for achievement.iconName
            IconResolver.AchievementIcon(
                achievement = achievement,
                isUnlocked = isUnlocked,
                size = 48.dp,
                fallback = {
                    Text(
                        text = if (isUnlocked) achievement.fallbackEmoji else "🔒",
                        fontSize = 28.sp
                    )
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}