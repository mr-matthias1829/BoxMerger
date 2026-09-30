package com.boxmerger.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.boxmerger.GameViewModel
import com.boxmerger.model.*

@Composable
fun GemUpgradesScreen(viewModel: GameViewModel) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Gem Upgrades",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        val upgrades = mutableListOf(gemMoreBoxesUpgrade, gemMorePrestigeUpgrade, gemIncreaseChanceUpgrade)
        if (viewModel.autoMergerUnlocked) {
            upgrades.add(gemAutoSpeedUpgrade)
        }

        for (definition in upgrades) {
            val level = viewModel.upgradeManager.getLevel(definition.id)
            UpgradeRecordItem(
                definition = definition,
                currentLevel = level,
                effectText = definition.effectDescription(level),
                cost = viewModel.upgradeManager.getCurrentCost(definition),
                currencyManager = viewModel.currencyManager,
                onBuy = { viewModel.buyUpgrade(definition) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
