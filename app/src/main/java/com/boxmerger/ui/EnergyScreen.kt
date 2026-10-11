// ui/EnergyScreen.kt
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
import com.boxmerger.logic.EnergyLogic
import com.boxmerger.logic.Gains
import com.boxmerger.model.Currencies

@Composable
fun EnergyScreen(viewModel: GameViewModel) {
    val scrollState = rememberScrollState()

    // Reading the balance refreshes this screen as energy ticks in
    viewModel.currencyManager.getBalance(Currencies.ENERGY)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Energy",
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
                    IconResolver.CurrencyIcon(currency = Currencies.ENERGY, size = 22.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Energy is generated passively",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = "+${Gains.energyPerSecond().toPrettyString()} energy per second",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        UpgradeList(
            viewModel = viewModel,
            pageId = EnergyLogic.PAGE_ID
        )
    }
}