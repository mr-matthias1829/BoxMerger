// ui/SteelScreen.kt
package com.boxmerger.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.boxmerger.GameViewModel
import com.boxmerger.logic.Gains
import com.boxmerger.logic.SteelLogic
import com.boxmerger.logic.formatPercent
import com.boxmerger.model.Currencies
import java.util.Locale

@Composable
fun SteelScreen(viewModel: GameViewModel) {
    val scrollState = rememberScrollState()

    // Reading the balance makes this screen refresh on every steel payout,
    // so the info card below always matches your current upgrades.
    viewModel.currencyManager.getBalance(Currencies.STEEL)

    // The gain text below is shown for the tier boxes currently spawn at
    val highestTier = viewModel.upgradeManager.spawnTierLevel
    val perManual = Gains.steelPerManualMerge(highestTier)
    val autoChance = Gains.steelAutoChance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Steel",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconResolver.CurrencyIcon(currency = Currencies.STEEL, size = 22.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Every manual merge forges steel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "Manual merge: +${perManual.toPrettyString()} steel (spawn tier: $highestTier)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (highestTier > 100) {
                    val bonus = Gains.steelBaseValue(highestTier) - 1.0
                    Text(
                        text = "Tier bonus: +${String.format(Locale.US, "%.1f", bonus)} base (past tier 100)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Text(
                    text = if (autoChance > 0.0) {
                        "Auto merge: ${formatPercent(autoChance)}% chance for " +
                                "+${Gains.steelPerAutoMerge(highestTier).toPrettyString()} steel"
                    } else {
                        "Auto merges give no steel"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        UpgradeList(
            viewModel = viewModel,
            pageId = SteelLogic.PAGE_ID
        )
    }
}