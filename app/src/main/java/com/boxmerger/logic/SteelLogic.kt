// logic/SteelLogic.kt
package com.boxmerger.logic

import com.boxmerger.model.*
import kotlin.math.pow

/**
 * Upgrades bought on the Steel page. The two auto-merge steel upgrades live in
 * SpecialityLogic (they are "unique mechanic" upgrades, paid for with steel).
 *
 * NOTE: all costs / growth numbers below are first-draft placeholders. Tune freely.
 */
object SteelLogic {
    const val PAGE_ID = "steel"

    /** Steel upgrade 1: each level makes every steel payout 1.1x bigger (compounding). */
    val steelValueUpgrade = UpgradeDefinition(
        id = "steel_value",
        name = "Steel Value",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.STEEL.id to BigNumber.of(5.0)),
        scalingFunction = { 1.2 },   // cost must grow faster than the 1.1 value, or steel snowballs
        maxLevel = 500,
        effectFormula = { level -> 1.1.pow(level.toDouble()) },
        effectDescription = { level ->
            "Steel Value: x${formatMultiplier(1.1.pow(level.toDouble()))}"
        }
    )

    /** Steel upgrade 4: box multiplier. */
    val steelMoreBoxesUpgrade = UpgradeDefinition(
        id = "steel_more_boxes",
        name = "Box Multiplier",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.STEEL.id to BigNumber.of(30.0)),
        scalingFunction = { 1.25 },
        maxLevel = 500,
        effectFormula = { level -> 1.07.pow(level.toDouble()) },
        effectDescription = { level ->
            "Box Multiplier: x${formatMultiplier(1.07.pow(level.toDouble()))}"
        }
    )

    /** Steel upgrade 5: gem multiplier (affects gem drops AND level-up gems). */
    val steelMoreGemsUpgrade = UpgradeDefinition(
        id = "steel_more_gems",
        name = "Gem Multiplier",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.STEEL.id to BigNumber.of(90.0)),
        scalingFunction = { 1.33 },
        maxLevel = 500,
        effectFormula = { level -> 1.07.pow(level.toDouble()) },
        effectDescription = { level ->
            "Gem Multiplier: x${formatMultiplier(1.07.pow(level.toDouble()))}"
        }
    )

    /**
     * Steel upgrade 6: fewer merges needed per level-up.
     * effectFormula returns the multiplier on the needed merges (1.0 = no change).
     * Only shown once leveling is unlocked.
     */
    val steelFasterLevelsUpgrade = UpgradeDefinition(
        id = "steel_faster_levels",
        name = "Faster Level-Ups",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.STEEL.id to BigNumber.of(100.0)),
        scalingFunction = { 1.8 },
        maxLevel = 25,                // 25 levels x 2% = half the merges at most
        effectFormula = { level -> 1.0 - level * 0.02 },
        effectDescription = { level ->
            "Merges per level-up: -${formatPercent(level * 2.0)}%"
        },
        isVisible = { state -> state.hasFlag("leveling_unlocked") }
    )

    val ALL: List<UpgradeDefinition> = listOf(
        steelValueUpgrade,
        steelMoreBoxesUpgrade,
        steelMoreGemsUpgrade,
        steelFasterLevelsUpgrade
    )
}