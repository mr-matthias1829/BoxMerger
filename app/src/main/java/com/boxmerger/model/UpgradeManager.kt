package com.boxmerger.model

import androidx.compose.runtime.mutableStateMapOf

class UpgradeDefinition(
    val id: String,
    val name: String,
    val baseCost: Map<CurrencyType, BigNumber>,
    val scalingFunction: (level: Int) -> Double = { level ->
        when {
            level <= 5 -> 1.5
            level <= 20 -> 2.0
            else -> 2.0 + (level * 0.01)
        }
    },
    val maxLevel: Int = 100,
    val effectFormula: (level: Int) -> Double,
    val effectDescription: (level: Int) -> String
)

class UpgradeManager {
    // Map of upgradeId -> level
    private val _levels = mutableStateMapOf<String, Int>()
    private val definitions = mutableMapOf<String, UpgradeDefinition>()

    fun registerDefinition(definition: UpgradeDefinition) {
        definitions[definition.id] = definition
    }

    fun getLevel(id: String): Int {
        return _levels[id] ?: 1
    }

    fun setLevel(id: String, level: Int) {
        _levels[id] = level
    }

    fun getCost(definition: UpgradeDefinition, level: Int): Map<CurrencyType, BigNumber> {
        var multiplierProduct = 1.0
        for (l in 1 until level) {
            multiplierProduct *= definition.scalingFunction(l)
        }
        return definition.baseCost.mapValues { (_, base) -> base * multiplierProduct }
    }

    fun getCurrentCost(definition: UpgradeDefinition): Map<CurrencyType, BigNumber> {
        return getCost(definition, getLevel(definition.id))
    }

    fun canUpgrade(definition: UpgradeDefinition, currencyManager: CurrencyManager): Boolean {
        val cost = getCurrentCost(definition)
        return currencyManager.hasEnough(cost)
    }

    fun purchaseUpgrade(definition: UpgradeDefinition, currencyManager: CurrencyManager): Boolean {
        val currentLevel = getLevel(definition.id)
        if (currentLevel >= definition.maxLevel) return false
        val cost = getCurrentCost(definition)
        if (currencyManager.spend(cost)) {
            _levels[definition.id] = currentLevel + 1
            return true
        }
        return false
    }

    val spawnTierLevel: Int get() = getLevel("spawn_tier")
    val spawnRateLevel: Int get() = getLevel("spawn_rate")

    fun getSpawnIntervalMs(): Long {
        val rateLevel = getLevel("spawn_rate")
        val def = definitions["spawn_rate"]
        val seconds = if (def != null) {
            def.effectFormula(rateLevel)
        } else {
            // backup
            // max spawnrate of 500 ms tho
            maxOf(0.5, 5.0 - (rateLevel - 1) * 0.35)
        }
        return maxOf(100L, (seconds * 1000.0).toLong())
    }

    fun restore(savedLevels: Map<String, Int>) {
        _levels.clear()
        _levels.putAll(savedLevels)
    }

    fun reset() {
        _levels.clear()
    }

    fun getAllLevels(): Map<String, Int> = _levels.toMap()
}
