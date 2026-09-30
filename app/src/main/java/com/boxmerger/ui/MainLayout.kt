package com.boxmerger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.boxmerger.GameViewModel

@Composable
fun MainLayout(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    var selectedTab by remember { mutableStateOf(0) }
    var showBottomNav by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Currency Bar
        Surface(
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Boxes
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Boxes",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = viewModel.currencyManager.boxes.toEngineeringString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Gems
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Gems",
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = viewModel.currencyManager.gems.toEngineeringString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                // Prestige
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Prestige",
                        tint = Color(0xFF00BCD4),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = viewModel.currencyManager.prestige.toEngineeringString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        // Main Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> GameScreen(viewModel = viewModel)
                1 -> PrestigeScreen(viewModel = viewModel)
                2 -> GemUpgradesScreen(viewModel = viewModel)
                3 -> AchievementsScreen()
                4 -> StatsScreen(viewModel = viewModel)
                5 -> SettingsScreen(viewModel = viewModel)
            }
        }

        // Navigation Reorganized:
        // Top row (Tabs 0, 1, 2): Main Game, Prestige, Gem Upgrades
        // Bottom row (Tabs 3, 4, 5): Achievements, Stats, Settings
        if (showBottomNav) {
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Top row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButtonWithTooltip(
                            icon = Icons.Default.Home,
                            label = "Game",
                            isSelected = selectedTab == 0,
                            onClick = { selectedTab = 0 }
                        )
                        IconButtonWithTooltip(
                            icon = Icons.Default.Bolt,
                            label = "Prestige",
                            isSelected = selectedTab == 1,
                            onClick = { selectedTab = 1 }
                        )
                        IconButtonWithTooltip(
                            icon = Icons.Default.Diamond,
                            label = "Gem Upgrades",
                            isSelected = selectedTab == 2,
                            onClick = { selectedTab = 2 }
                        )
                    }
                    // Bottom row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButtonWithTooltip(
                            icon = Icons.Default.EmojiEvents,
                            label = "Achievements",
                            isSelected = selectedTab == 3,
                            onClick = { selectedTab = 3 }
                        )
                        IconButtonWithTooltip(
                            icon = Icons.Default.BarChart,
                            label = "Stats",
                            isSelected = selectedTab == 4,
                            onClick = { selectedTab = 4 }
                        )
                        IconButtonWithTooltip(
                            icon = Icons.Default.Settings,
                            label = "Settings",
                            isSelected = selectedTab == 5,
                            onClick = { selectedTab = 5 }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: GameViewModel) {
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { showResetDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Reset")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Hard Reset Data")
        }

        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Hard Reset") },
                text = { Text("Are you sure you want to delete all saved data and restart from zero? This cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.hardReset()
                            showResetDialog = false
                        }
                    ) {
                        Text("Yes, Reset", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun IconButtonWithTooltip(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier
            .size(width = 110.dp, height = 40.dp)
            .padding(horizontal = 2.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
    }
}
