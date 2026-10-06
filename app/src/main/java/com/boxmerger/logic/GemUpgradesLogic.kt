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
        scalingFunction = { 1.21 },
        maxLevel = 500,
        effectFormula = { level -> 1.0 + (1.1.pow(level - 1) - 1) },
        effectDescription = { level ->
            "Multiplier: x${formatMultiplier(1.0 + (1.1.pow(level - 1) - 1))}"
        }
    )

    val gemMorePrestigeUpgrade = UpgradeDefinition(
        id = "gem_more_prestige",
        name = "Prestige Multiplier",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.GEM.id to BigNumber.of(5.0)),
        scalingFunction = { 1.29 },
        maxLevel = 500,
        effectFormula = { level -> 1.0 + (level - 1) * 0.2 },
        effectDescription = { level ->
            "Multiplier: x${formatMultiplier(1.0 + (level - 1) * 0.2)}"
        }
    )

    val gemIncreaseChanceUpgrade = UpgradeDefinition(
        id = "gem_increase_chance",
        name = "Gem Drop Chance",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.GEM.id to BigNumber.of(10.0)),
        scalingFunction = { level ->
            when {
                level <= 5 -> 1.1
                level <= 10 -> 1.15
                level <= 15 -> 1.4
                level <= 20 -> 1.52
                level <= 25 -> 2.15
                level <= 30 -> 2.3
                level <= 35 -> 3.5
                else -> 4.0
            }},
        maxLevel = 40,
        effectFormula = { level -> minOf(10.0, 2.0 + (level - 1) * 0.2) },
        effectDescription = { level ->
            "Chance: ${formatPercent(minOf(10.0, 2.0 + (level - 1) * 0.2))}%"
        }
    )

    val gemAutoSpeedUpgrade = UpgradeDefinition(
        id = "gem_auto_speed",
        name = "Auto-Merger Speed",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.GEM.id to BigNumber.of(20.0)),
        scalingFunction = { level ->
            when {
                level <= 15 -> 1.09
                level <= 25 -> 1.145
                level <= 40 -> 1.19
                level <= 60 -> 1.28
                else -> 1.39
            }},
        maxLevel = 75,
        effectFormula = { level ->
            6.0 * ((level + 10) / 10.0).pow(-1.3739)
        },
        effectDescription = { level ->
            val sec = 6.0 * ((level + 10) / 10.0).pow(-1.3739)
            if (sec >= 1.0) {
                "Auto-Merger Speed: ${formatSeconds(sec)}s"
            } else {
                "Auto-Merger Speed: ${(sec * 1000).toInt()}ms"
            }
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

// ---------------------------------------------------------------------------
// Shared formatting helpers for upgrade descriptions.
// Use these everywhere so numbers look consistent.
// ---------------------------------------------------------------------------

/**
 * Formats a multiplier like x1.50, x12.3, x1.23K, x4.56M.
 */
fun formatMultiplier(value: Double): String {
    return BigNumber.of(value).toPrettyString()
}

/**
 * Formats a percentage like 12.5, 100, 0.25.
 */
fun formatPercent(pct: Double): String {
    return when {
        pct == pct.toLong().toDouble() -> String.format(Locale.US, "%.0f", pct)
        pct < 1.0 -> String.format(Locale.US, "%.2f", pct)
        else -> String.format(Locale.US, "%.1f", pct)
    }
}

/**
 * Formats seconds like 2.50, 0.75.
 */
fun formatSeconds(sec: Double): String {
    return String.format(Locale.US, "%.2f", sec)
}