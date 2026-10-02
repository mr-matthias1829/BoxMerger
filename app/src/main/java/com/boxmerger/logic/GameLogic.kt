// logic/GameLogic.kt
package com.boxmerger.logic

import com.boxmerger.model.*
import java.util.Locale
import kotlin.math.pow

fun getSpawnRate(level: Int): Double {
    val x = level.toDouble()
    return 6.0 * ((x + 9.7786) / 10.7786).pow(-1.3739)
}

object GameLogic {
    const val PAGE_ID = "game"

    val spawnTierUpgrade = UpgradeDefinition(
        id = "spawn_tier",
        name = "Spawn Tier",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.BOXES.id to BigNumber.of(20e3)),
        scalingFunction = { level ->
            when {
                level <= 5 -> 2.25 // player catches up
                level <= 10 -> 3.0 // player stays
                level <= 19 -> 4.0 // player loses
                level <= 100 -> 5.0
                else -> 6.5
            }
        },
        effectFormula = { level -> level.toDouble() },
        effectDescription = { level -> "Spawn Tier: $level" },
        onPurchase = { vm ->
            val newTier = vm.upgradeManager.spawnTierLevel
            vm.upgradeGridItemsToTier(newTier)
        }
    )

    val spawnRateUpgrade = UpgradeDefinition(
        id = "spawn_rate",
        name = "Spawn Rate",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.BOXES.id to BigNumber.of(1e3)),
        scalingFunction = { level ->
            when {
                level <= 10 -> 2.2
                level <= 20 -> 3.1
                else -> 4.3
            }
        },
        effectFormula = { level -> getSpawnRate(level) },
        effectDescription = { level ->
            val seconds = getSpawnRate(level)
            if (seconds >= 1.0) {
                "Spawn Rate: ${formatSeconds(seconds)}s"
            } else {
                "Spawn Rate: ${(seconds * 1000).toInt()}ms"
            }
        },
        onPurchase = { vm -> vm.restartSpawner() }
    )

    val ALL: List<UpgradeDefinition> = listOf(spawnTierUpgrade, spawnRateUpgrade)
}