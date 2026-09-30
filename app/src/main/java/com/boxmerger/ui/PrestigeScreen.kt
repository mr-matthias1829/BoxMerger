package com.boxmerger.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.boxmerger.GameViewModel
import com.boxmerger.logic.PrestigeLogic

@Composable
fun PrestigeScreen(viewModel: GameViewModel) {
    val scrollState = rememberScrollState()
    val gain = viewModel.calculatePrestigeGain()
    val canPrestige = viewModel.canPrestige

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Prestige",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Prestige resets your grid, boxes, and standard upgrades (Spawn Tier & Spawn Rate) in exchange for Prestige currency. Holding Prestige also grants +1% boost to boxes per Prestige.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Big Prestige Gain Button
        Button(
            onClick = { viewModel.performPrestige() },
            enabled = canPrestige,
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = "Prestige",
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (canPrestige)
                    "Prestige (+${gain.toEngineeringString()})"
                else
                    "Locked (Requires Tier 20+)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Prestige Upgrades",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Just this — no manual list, no manual loop
        UpgradeList(
            viewModel = viewModel,
            pageId = PrestigeLogic.PAGE_ID
        )
    }
}