package com.boxmerger.logic

import com.boxmerger.model.*
import kotlin.math.pow

object SpecialityLogic {
    const val PAGE_ID = "speciality_upgrades"

    val doubleSpawnUpgrade = UpgradeDefinition(
        id = "spec_double_spawn",
        name = "Double Box Spawns",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.GEM.id to BigNumber.of(25.0)),
        scalingFunction = { level ->
            when {
                level <= 10 -> 1.2
                level == 20 -> 2.2
                level < 20 -> 1.4
                level == 40 -> 5.0
                level < 40 -> 1.9
                level == 60 -> 8.0
                else -> 3.0
            } },
        maxLevel = 81, // 10% + 80 * 0.5% = 50%
        effectFormula = { level ->
            if (level <= 0) 0.0 else minOf(50.0, 10.0 + (level - 1) * 0.5)
        },
        effectDescription = { level ->
            val chance = if (level <= 0) 0.0 else minOf(50.0, 10.0 + (level - 1) * 0.5)
            "Double Spawn Chance: ${formatPercent(chance)}%"
        }
    )

    val maxButtonUpgrade = UpgradeDefinition(
        id = "spec_max_button",
        name = "Buy Max",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.PRESTIGE.id to BigNumber.of(2.5e9)),
        scalingFunction = { 1.0 },
        maxLevel = 1,
        effectFormula = { level -> if (level >= 1) 1.0 else 0.0 },
        effectDescription = { level ->
            if (level >= 1) "Unlocked 'Buy Max'"
            else "Unlocks a button to max main upgrades"
        }
    )

    // ---- Steel-powered upgrades (hidden until Steel is unlocked) ----
    // NOTE: costs are first-draft placeholders. Tune freely.

    /** Steel upgrade 2: chance that an auto merge pays out steel. 0% until bought. */
    val steelAutoChanceUpgrade = UpgradeDefinition(
        id = "spec_steel_auto_chance",
        name = "Auto merge steel chance",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.STEEL.id to BigNumber.of(1200.0)),
        scalingFunction = { 1.24 },
        maxLevel = 46, // 0.5% at level 1, +0.1% per level -> 5% at level 46
        effectFormula = { level ->
            if (level <= 0) 0.0 else minOf(5.0, 0.4 + level*0.1)
        },
        effectDescription = { level ->
            val chance = if (level <= 0) 0.0 else minOf(5.0, 0.4 + level*0.1)
            "Chance: ${formatPercent(chance)}%"
        },
        isVisible = { state -> state.hasFlag("steel_unlocked") }
    )

    /**
     * Steel upgrade 3: how much steel a successful auto roll gives, as a fraction of
     * a manual merge (0.02 = 2%). Starts at 0.02. Only shown once the chance upgrade is bought.
     */
    val steelAutoValueUpgrade = UpgradeDefinition(
        id = "spec_steel_auto_value",
        name = "Auto merge steel value",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.STEEL.id to BigNumber.of(3600.0)),
        scalingFunction = { 1.4 },
        maxLevel = 31, // 0.02 + 31 * 0.01 = 0.33
        effectFormula = { level -> 0.02 + level * 0.01 },
        effectDescription = { level ->
            "Value: ${formatPercent((0.02 + level * 0.01) * 100.0)}% of a manual merge"
        },
        isVisible = { state ->
            state.hasFlag("steel_unlocked") && state.upgradeLevel("spec_steel_auto_chance") >= 1
        }
    )


    // ---- Steel <-> Energy cross multipliers (need BOTH steel and energy unlocked) ----
    // Both are intentionally weak: x1.02 per level, compounding.

    /** Energy multiplier, paid for with steel. */
    val energyFromSteelUpgrade = UpgradeDefinition(
        id = "spec_energy_mult",
        name = "Energy multiplier",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.STEEL.id to BigNumber.of(500.0)),
        scalingFunction = { 1.2 },
        maxLevel = 500,
        effectFormula = { level -> 1.02.pow(level.toDouble()) },
        effectDescription = { level ->
            "Energy Gain: x${formatMultiplier(1.02.pow(level.toDouble()))}"
        },
        isVisible = { state -> state.hasFlag("steel_unlocked") && state.hasFlag("energy_unlocked") }
    )

    /** Steel multiplier, paid for with energy. */
    val steelFromEnergyUpgrade = UpgradeDefinition(
        id = "spec_steel_mult",
        name = "Steel multiplier",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.ENERGY.id to BigNumber.of(1000.0)),
        scalingFunction = { 1.5 },
        maxLevel = 500,
        effectFormula = { level -> 1.02.pow(level.toDouble()) },
        effectDescription = { level ->
            "Steel Gain: x${formatMultiplier(1.02.pow(level.toDouble()))}"
        },
        isVisible = { state -> state.hasFlag("steel_unlocked") && state.hasFlag("energy_unlocked") }
    )


        val ALL: List<UpgradeDefinition> = listOf(
        doubleSpawnUpgrade,
        maxButtonUpgrade,
        steelAutoChanceUpgrade,
        steelAutoValueUpgrade,
        energyFromSteelUpgrade,
        steelFromEnergyUpgrade
    )

}