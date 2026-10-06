package com.boxmerger.logic

import com.boxmerger.model.*

object SpecialityLogic {
    const val PAGE_ID = "speciality_upgrades"

    val doubleSpawnUpgrade = UpgradeDefinition(
        id = "spec_double_spawn",
        name = "Double Box Spawns",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.GEM.id to BigNumber.of(25.0)),
        scalingFunction = { level ->
            when {
                level <= 10 -> 1.25
                level <= 20 -> 1.5
                level <= 40 -> 2.0
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
            if (level >= 1) "Unlocked 'Buy Max' on Game Screen"
            else "Unlocks a button to max main upgrades"
        }
    )

    val ALL: List<UpgradeDefinition> = listOf(
        doubleSpawnUpgrade,
        maxButtonUpgrade
    )
}