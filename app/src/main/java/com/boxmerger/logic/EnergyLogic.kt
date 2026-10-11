// logic/EnergyLogic.kt
package com.boxmerger.logic

import com.boxmerger.model.*
import kotlin.math.pow

/**
 * Upgrades bought on the Energy page. Energy itself is generated passively
 * (see Gains.energyPerSecond). The two weak cross-multipliers between energy and
 * steel live in SpecialityLogic.
 *
 * NOTE: all costs / growth numbers below are first-draft placeholders. Tune freely.
 */
object EnergyLogic {
    const val PAGE_ID = "energy"

    /** Doubles the energy gained per level. Costs prestige and scales fairly fast. */
    val energyMultiplierUpgrade = UpgradeDefinition(
        id = "energy_multiplier",
        name = "Energy Multiplier",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.PRESTIGE.id to BigNumber.of(100.0)),
        scalingFunction = { 150.5 },
        maxLevel = 500,
        effectFormula = { level -> 2.0.pow(level.toDouble()) },
        effectDescription = { level ->
            "Energy Gain: x${formatMultiplier(2.0.pow(level.toDouble()))}"
        }
    )

    /**
     * Slightly faster box spawns. effectFormula is the multiplier on the spawn
     * INTERVAL (1.0 = unchanged, 0.99^level = 1% faster per level).
     */
    val energyFasterSpawnsUpgrade = UpgradeDefinition(
        id = "energy_faster_spawns",
        name = "Faster Spawns",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.ENERGY.id to BigNumber.of(1000.0)),
        scalingFunction = { 1.6 },
        maxLevel = 500,
        effectFormula = { level -> 0.99.pow(level.toDouble()) },
        effectDescription = { level ->
            "Spawn Time: -${formatPercent((1.0 - 0.99.pow(level.toDouble())) * 100.0)}%"
        },
        onPurchase = { vm -> vm.restartSpawner() }
    )

    /**
     * Stronger held-prestige boost. Each held prestige gives +1% boxes by default;
     * every level adds +0.5%, up to +99% (198 levels). effectFormula returns the
     * boost per held prestige as a fraction (0.01 = 1%).
     */
    val energyStrongerPrestigeUpgrade = UpgradeDefinition(
        id = "energy_stronger_prestige",
        name = "Stronger Prestige",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.ENERGY.id to BigNumber.of(4000.0)),
        scalingFunction = { 1.89 },
        maxLevel = 198,               // 198 x 0.5% = +99%
        effectFormula = { level -> 0.01 + level * 0.005 },
        effectDescription = { level ->
            "Held Prestige Boost: +${formatPercent((0.01 + level * 0.005) * 100.0)}% each"
        }
    )

    /**
     * Slightly faster auto-merger. effectFormula is the multiplier on the
     * auto-merge DELAY (1.0 = unchanged, 0.99^level = 1% faster per level).
     */
    val energyFasterAutoUpgrade = UpgradeDefinition(
        id = "energy_faster_auto",
        name = "Faster Auto-Merger",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.ENERGY.id to BigNumber.of(15700.0)),
        scalingFunction = { 1.78 },
        maxLevel = 500,
        effectFormula = { level -> 0.99.pow(level.toDouble()) },
        effectDescription = { level ->
            "Auto-Merge Time: -${formatPercent((1.0 - 0.99.pow(level.toDouble())) * 100.0)}%"
        },
        isVisible = { state -> state.hasFlag("auto_merger_unlocked") }
    )

    val ALL: List<UpgradeDefinition> = listOf(
        energyMultiplierUpgrade,
        energyFasterSpawnsUpgrade,
        energyStrongerPrestigeUpgrade,
        energyFasterAutoUpgrade,
    )
}