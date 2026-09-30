// logic/GemUpgradesLogic.kt
package com.boxmerger.logic

import com.boxmerger.model.*
import java.util.Locale
import kotlin.math.pow

object GemUpgradesLogic {
    const val PAGE_ID = "gem_upgrades"

    val gemMoreBoxesUpgrade = UpgradeDefinition(
        id = "gem_more_boxes",
        name = "Box Multiplier",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.GEM.id to BigNumber.of(1.0)),
        scalingFunction = { 1.22 },
        maxLevel = 500,
        effectFormula = { level -> 1.0 + (1.1.pow(level - 1) - 1) },
        effectDescription = { level ->
            "Multiplier: x${String.format(Locale.US, "%.2f", 1.0 + (1.1.pow(level - 1) - 1))}"
        }
    )

    val gemMorePrestigeUpgrade = UpgradeDefinition(
        id = "gem_more_prestige",
        name = "Prestige Multiplier",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.GEM.id to BigNumber.of(5.0)),
        scalingFunction = { 1.36 },
        maxLevel = 500,
        effectFormula = { level -> 1.0 + (level - 1) * 0.2 },
        effectDescription = { level ->
            "Multiplier: x${String.format(Locale.US, "%.2f", 1.0 + (level - 1) * 0.25)}"
        }
    )

    val gemIncreaseChanceUpgrade = UpgradeDefinition(
        id = "gem_increase_chance",
        name = "Gem Drop Chance",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.GEM.id to BigNumber.of(10.0)),
        scalingFunction = { 1.1 },
        maxLevel = 40,
        effectFormula = { level -> minOf(5.0, 1.0 + (level - 1) * 0.1) },
        effectDescription = { level ->
            "Chance: ${String.format(Locale.US, "%.1f", minOf(5.0, 1.0 + (level - 1) * 0.1))}%"
        }
    )

    val gemAutoSpeedUpgrade = UpgradeDefinition(
        id = "gem_auto_speed",
        name = "Auto-Merger Speed",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.GEM.id to BigNumber.of(50.0)),
        scalingFunction = {  level ->
            when {
                level <= 15 -> 1.1
                level <= 40 -> 1.16
                else -> 1.21
            } },
        maxLevel = 75,
        effectFormula = { level ->
            6.0 * ((level + 10) / 10.7786).pow(-1.3739)
        },
        effectDescription = { level ->
            val sec = 6.0 * ((level + 10) / 10.7786).pow(-1.3739)
            "Auto-Merger Speed: ${String.format(Locale.US, "%.2fs", sec)}"
        },
        isVisible = { state -> state.hasFlag("auto_merger_unlocked") }
    )

    val ALL: List<UpgradeDefinition> = listOf(
        gemMoreBoxesUpgrade,
        gemMorePrestigeUpgrade,
        gemIncreaseChanceUpgrade,
        gemAutoSpeedUpgrade
    )
}