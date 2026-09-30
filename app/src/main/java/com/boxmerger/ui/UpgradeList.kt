package com.boxmerger.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.boxmerger.GameViewModel
import com.boxmerger.model.BigNumber
import com.boxmerger.model.Currencies
import com.boxmerger.model.CurrencyManager
import com.boxmerger.model.UpgradeDefinition

@Composable
fun UpgradeList(
    viewModel: GameViewModel,
    pageId: String
) {
    val state = viewModel.state
    val definitions = viewModel.upgradeManager.getDefinitionsForPage(pageId)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (definition in definitions) {
            if (!definition.isVisible(state)) continue

            val level = viewModel.upgradeManager.getLevel(definition.id)
            val unlocked = definition.isUnlocked(state)

            UpgradeRecordItem(
                definition = definition,
                currentLevel = level,
                effectText = definition.effectDescription(level),
                cost = viewModel.upgradeManager.getCurrentCost(definition),
                currencyManager = viewModel.currencyManager,
                isUnlocked = unlocked,
                onBuy = { viewModel.buyUpgrade(definition) }
            )
        }
    }
}

@Composable
fun UpgradeRecordItem(
    definition: UpgradeDefinition,
    currentLevel: Int,
    effectText: String,
    cost: Map<String, BigNumber>,
    currencyManager: CurrencyManager,
    isUnlocked: Boolean,
    onBuy: () -> Unit
) {
    val canAfford = isUnlocked && currencyManager.hasEnough(cost)
    val name = "${definition.name} (Lvl $currentLevel)"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBuy,
                enabled = canAfford,
                modifier = Modifier.size(52.dp),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Buy Upgrade",
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = effectText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (cost.isNotEmpty()) {
                        Text(
                            text = "| Cost:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        cost.forEach { (currencyId, amount) ->
                            val currency = Currencies.byId(currencyId)
                            if (currency != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconResolver.CurrencyIcon(
                                        currency = currency,
                                        size = 16.dp
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = amount.toEngineeringString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}