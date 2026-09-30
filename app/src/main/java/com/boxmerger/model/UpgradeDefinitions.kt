package com.boxmerger.model

import java.util.Locale
import kotlin.math.pow

private fun getSpawnRate(level: Int): Double {
    val x = level.toDouble()
    return 5.0 * ((x + 9.7786) / 10.7786).pow(-1.3739)
}

// box upgrades found in the respective page file


// --- Gem Upgrades ---
val gemMoreBoxesUpgrade = UpgradeDefinition(
    id = "gem_more_boxes",
    name = "Box Multiplier",
    baseCost = mapOf(CurrencyType.GEM to BigNumber(1.0)),
    scalingFunction = { level -> 1.6 },
    maxLevel = 125,
    effectFormula = { level -> 1.0 + (Math.pow(1.2, level.toDouble()-1)-1) },
    effectDescription = { level -> "Multiplier: x${String.format(Locale.US, "%.2f", 1.0 + (Math.pow(1.2, level.toDouble()-1) - 1))}" })

val gemMorePrestigeUpgrade = UpgradeDefinition(
    id = "gem_more_prestige",
    name = "Prestige Multiplier",
    baseCost = mapOf(CurrencyType.GEM to BigNumber(5.0)),
    scalingFunction = { level -> 1.5 },
    maxLevel = 50,
    effectFormula = { level -> 1.0 + (level - 1) * 0.2 },
    effectDescription = { level -> "Multiplier: x${String.format(Locale.US, "%.2f", 1.0 + (level - 1) * 0.25)}" }
)

val gemIncreaseChanceUpgrade = UpgradeDefinition(
    id = "gem_increase_chance",
    name = "Gem Drop Chance",
    baseCost = mapOf(CurrencyType.GEM to BigNumber(10.0)),
    scalingFunction = { level -> 1.8 },
    maxLevel = 40,
    effectFormula = { level -> minOf(5.0, 1.0 + (level - 1) * 0.1) },
    effectDescription = { level -> "Chance: ${String.format(Locale.US, "%.1f", minOf(5.0, 1.0 + (level - 1) * 0.1))}%" }
)

val gemAutoSpeedUpgrade = UpgradeDefinition(
    id = "gem_auto_speed",
    name = "Auto-Merger Speed",
    baseCost = mapOf(CurrencyType.GEM to BigNumber(50.0)),
    scalingFunction = { level -> 1.6 },
    maxLevel = 75,
    effectFormula = { level -> 6.0 * Math.pow(
        (level + 10) / 10.7786,
        -1.3739)},
    effectDescription = { level -> "Auto-Merger Speed: ${String.format(Locale.US, "%.2fs", 6.0 * Math.pow(
        (level + 10) / 10.7786,
        -1.3739))}" }
)

// --- Prestige Upgrades ---
val presMoreBoxesUpgrade = UpgradeDefinition(
    id = "pres_more_boxes",
    name = "Box Multiplier",
    baseCost = mapOf(CurrencyType.PRESTIGE to BigNumber(400.0)),
    scalingFunction = { level -> if (level <= 5) 10.0 else if (level <= 12) 12.0 else 15.0 },
    maxLevel = 30,
    effectFormula = { level -> 1.0 + (Math.pow(5.0, level.toDouble()-1)-1) },
    effectDescription = { level -> "Multiplier: x${1.0 + (Math.pow(5.0, level.toDouble()-1)-1)}" }
)

val presMoreGemsUpgrade = UpgradeDefinition(
    id = "pres_more_gems",
    name = "Gem Multiplier",
    baseCost = mapOf(CurrencyType.PRESTIGE to BigNumber(50.0)),
    scalingFunction = { level -> 1.9 },
    maxLevel = 30,
    effectFormula = { level -> 1.0 + (Math.pow(1.1, level.toDouble()-1)-1) },
    effectDescription = { level -> "Gem Multiplier: x${String.format(Locale.US, "%.2f", 1.0 + (Math.pow(1.1, level.toDouble()-1)-1))}" }
)

val presMorePrestigeUpgrade = UpgradeDefinition(
    id = "pres_more_prestige",
    name = "Prestige Boost",
    baseCost = mapOf(CurrencyType.PRESTIGE to BigNumber(30.0)),
    scalingFunction = { level -> 2.5 },
    maxLevel = 30,
    effectFormula = { level -> 1.0 + (Math.pow(1.16, level.toDouble()-1)-1) },
    effectDescription = { level -> "Multiplier: x${String.format(Locale.US, "%.2f", 1.0 + (Math.pow(1.16, level.toDouble()-1)-1))}" }
)
