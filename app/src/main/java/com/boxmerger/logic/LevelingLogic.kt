// logic/LevelingLogic.kt
package com.boxmerger.logic

import com.boxmerger.GameViewModel
import com.boxmerger.logic.Gains.levelUpGems
import com.boxmerger.model.BigNumber
import kotlin.math.pow

object LevelingLogic {
    const val PAGE_ID = "leveling"

    /**
     * Merges needed to go from [level] to [level + 1].
     * Level 0 -> 12 merges, Level 1 -> 14, Level 2 -> 16, ...
     */
    fun mergesNeededForNextLevel(level: Int): Long {
        val base = 12L
        val levelScale = 2L * level

        // this is meant to bring some more variation than just simply +2 each level
        var milestoneScale = 0L

        // every 5 add +1
        milestoneScale += level / 5

        // every 25 add +7
        milestoneScale += 7L * (level / 25)

        return base + levelScale + milestoneScale
    }

    /**
     * Gems awarded upon reaching [level].
     * Returns a BigNumber so it can scale with multipliers.
     */
    fun gemsAwardedOnLevelUp(level: Int): BigNumber {
        return Gains.levelUpGems(level)
    }

    /**
     * Multiplier applied to block/box earnings at [level].
     * Level 0 -> x1.00, Level 10 -> x1.50, etc.
     * Returns Double because it's always a small multiplier.
     */
    fun blockEarningsMultiplier(level: Int): Double {
        var base = 1.0 + 0.05 * level
        if (level < 25) return base

        var mult = 1.01.pow(level - 24.0)
        return base * mult
    }

    /**
     * Minimum level required to perform a Level Reset.
     */
    fun levelResetRequirement(): Int = 50

    /**
     * Permanent prestige-gain multiplier gained by resetting at [level].
     * Level 50 -> +1.00, Level 100 -> +2.00.
     * Returns Double because it's a small percentage boost.
     */
    fun levelResetPrestigeBoostGain(level: Int): Double {
        return 0.025 * level
    }

    /**
     * Formats the reset boost as a percentage string.
     */
    fun formatResetPrestigeBoost(level: Int): String {
        val pct = (levelResetPrestigeBoostGain(level) * 100)
        return "+${formatPercent(pct)}%"
    }

    /**
     * Helper to format a percentage with sensible decimal places.
     * e.g. 12.5 -> "12.5", 100.0 -> "100", 1.25 -> "1.25"
     */
    private fun formatPercent(pct: Double): String {
        return when {
            pct == pct.toLong().toDouble() -> String.format(java.util.Locale.US, "%.0f", pct)
            pct < 1.0 -> String.format(java.util.Locale.US, "%.2f", pct)
            else -> String.format(java.util.Locale.US, "%.1f", pct)
        }
    }
}