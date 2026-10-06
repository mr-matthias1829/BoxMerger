// logic/PrestigeLogic.kt
package com.boxmerger.logic

import com.boxmerger.model.*
import java.util.Locale
import kotlin.math.pow

object PrestigeLogic {
    const val PAGE_ID = "prestige"

    val presMoreBoxesUpgrade = UpgradeDefinition(
        id = "pres_more_boxes",
        name = "Box Multiplier",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.PRESTIGE.id to BigNumber.of(400.0)),
        scalingFunction = { level ->
            when {
                level <= 5 -> 10.0
                level <= 10 -> 30.0
                level <= 25 -> 100.0
                else -> 250.0
            }
        },
        maxLevel = 500,
        effectFormula = { level ->
            when {
                level <= 10 -> 5.0.pow(level.toDouble())
                else -> 9765625.0 * (4.0.pow((level - 10).toDouble()))
            }
        },
        effectDescription = { level ->
            val mult = when {
                level <= 10 -> 5.0.pow(level.toDouble())
                else -> 9765625.0 * (4.0.pow((level - 10).toDouble()))
            }
            "Multiplier: x${formatMultiplier(mult)}"
        }
    )

    val presMoreGemsUpgrade = UpgradeDefinition(
        id = "pres_more_gems",
        name = "Gem Multiplier",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.PRESTIGE.id to BigNumber.of(50.0)),
        scalingFunction = { 1.7 },
        maxLevel = 500,
        effectFormula = { level -> 1.1.pow(level.toDouble()) },
        effectDescription = { level ->
            "Gem Multiplier: x${formatMultiplier(1.1.pow(level.toDouble()))}"
        }
    )

    val presMorePrestigeUpgrade = UpgradeDefinition(
        id = "pres_more_prestige",
        name = "Prestige Multiplier",
        pageId = PAGE_ID,
        baseCost = mapOf(Currencies.PRESTIGE.id to BigNumber.of(30.0)),
        scalingFunction = { 2.5 },
        maxLevel = 26,
        effectFormula = { level -> 1.16.pow(level.toDouble()) },
        effectDescription = { level ->
            "Multiplier: x${formatMultiplier(1.16.pow(level.toDouble()))}"
        }
    )

    val ALL: List<UpgradeDefinition> = listOf(
        presMoreBoxesUpgrade,
        presMoreGemsUpgrade,
        presMorePrestigeUpgrade
    )
}