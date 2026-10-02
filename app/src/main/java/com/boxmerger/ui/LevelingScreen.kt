// ui/LevelingScreen.kt
package com.boxmerger.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.boxmerger.GameViewModel
import com.boxmerger.logic.LevelingLogic
import com.boxmerger.logic.formatPercent
import com.boxmerger.model.Currencies

@Composable
fun LevelingScreen(viewModel: GameViewModel) {
    var showResetDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val level = viewModel.playerLevel
    val levelingMerges = viewModel.levelingMerges
    val neededMerges = viewModel.mergesNeededForNextLevel()
    val progress = (levelingMerges.toFloat() / neededMerges.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Leveling",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Level icon — looks for assets/icons/icon_level.png
                        IconResolver.NamedIcon(
                            name = "icon_level",
                            fallbackEmoji = "📈",
                            size = 32.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Level $level",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = "Next: Lvl ${level + 1}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$levelingMerges / $neededMerges Merges",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Next level reward — uses BigNumber display
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconResolver.CurrencyIcon(
                        currency = Currencies.GEM,
                        size = 18.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Next level reward: ${LevelingLogic.gemsAwardedOnLevelUp(level + 1).toPrettyString()} Gems",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Active Bonuses Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Current Bonuses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Block boost — uses the BOXES currency PNG
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconResolver.CurrencyIcon(
                        currency = Currencies.BOXES,
                        size = 24.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val blockPct = (LevelingLogic.blockEarningsMultiplier(level) - 1.0) * 100
                    Text(
                        text = "Box Boost: +${formatPercent(blockPct)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                when {
                    viewModel.hasFlag("level_reset_unlocked") -> {
                        // Prestige boost — uses the PRESTIGE currency PNG
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconResolver.CurrencyIcon(
                                currency = Currencies.PRESTIGE,
                                size = 24.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val boostPct = viewModel.levelResetPrestigeBoost * 100
                            Text(
                                text = "Prestige Boost: +${formatPercent(boostPct)}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Resets — uses a generic named icon
                        /*
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconResolver.NamedIcon(
                                name = "icon_reset",
                                fallbackEmoji = "🔄",
                                size = 24.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Total Level Resets: ${viewModel.levelResets}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                         */
                    }
                }
            }
        }

        // Level Reset Card
        when {
            viewModel.hasFlag("level_reset_unlocked") -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Level Reset",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "After reaching Level 50, you can Level Reset! Resetting resets your level to 0, but grants a prestige boost for every level sacrificed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        when {
                            !viewModel.hasFlag("level_reset_unlocked") -> {
                                Text(
                                    text = "Locked: Reach Level 50 to unlock Level Reset",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                        alpha = 0.7f
                                    )
                                )
                            }

                            level < LevelingLogic.levelResetRequirement() -> {
                                Text(
                                    text = "Locked: Requires Level ${LevelingLogic.levelResetRequirement()} " +
                                            "(Current: Level $level / ${LevelingLogic.levelResetRequirement()})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                        alpha = 0.7f
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {},
                                    enabled = false,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Level Reset (Requires Level ${LevelingLogic.levelResetRequirement()})")
                                }
                            }

                            else -> {
                                Text(
                                    text = "Resetting now at Level $level will grant " +
                                            "${LevelingLogic.formatResetPrestigeBoost(level)} Prestige boost!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { showResetDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Perform Level Reset")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Confirm Level Reset") },
            text = {
                Text("Are you sure you want to Level Reset? Your level will reset to 0, and you will gain ${LevelingLogic.formatResetPrestigeBoost(level)} Prestige boost!")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.performLevelReset()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset Level")
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