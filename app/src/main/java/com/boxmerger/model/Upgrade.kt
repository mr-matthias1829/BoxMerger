// model/Upgrade.kt
package com.boxmerger.model

import androidx.compose.runtime.mutableStateMapOf
import com.boxmerger.GameViewModel

/**
 * Condition evaluated at render time. Receives the game state so it can
 * decide visibility. Keep these pure and cheap.
 */
typealias Condition = (GameState) -> Boolean

/**
 * An upgrade definition. All predicates are optional; default = always visible.
 *
 * - isVisible: if false, upgrade is hidden entirely.
 * - isUnlocked: if false, upgrade shows as locked (greyed out, no buy).
 * - onUnlock: called once when isUnlocked transitions false -> true.
 *
 * Effect formulas return Double for simple multipliers; for values that can
 * grow beyond Double range, use BigNumber-based formulas.
 */
class UpgradeDefinition(
    val id: String,
    val name: String,
    val pageId: String,
    val baseCost: Map<String, BigNumber>,          // currencyId -> amount
    val scalingFunction: (level: Int) -> Double = { level ->
        when {
            level <= 5 -> 1.5
            level <= 20 -> 2.0
            else -> 2.0 + (level * 0.01)
        }
    },
    val maxLevel: Int = 100,
    val effectFormula: (level: Int) -> Double,
    val effectDescription: (level: Int) -> String,
    val isVisible: Condition = { true },
    val isUnlocked: Condition = { true },
    val onUnlock: ((GameViewModel) -> Unit)? = null,
    val onPurchase: ((GameViewModel) -> Unit)? = null
)

class UpgradeManager {
    private val _levels = mutableStateMapOf<String, Int>()
    private val definitions = mutableMapOf<String, UpgradeDefinition>()

    /** Track which upgrades have had onUnlock fired */
    private val firedUnlocks = mutableSetOf<String>()

    fun registerDefinition(definition: UpgradeDefinition) {
        definitions[definition.id] = definition
        if (!_levels.containsKey(definition.id)) {
            _levels[definition.id] = 0
        }
    }

    fun registerAll(defs: List<UpgradeDefinition>) {
        for (d in defs) registerDefinition(d)
    }

    fun getDefinition(id: String): UpgradeDefinition? = definitions[id]

    fun getDefinitionsForPage(pageId: String): List<UpgradeDefinition> {
        return definitions.values.filter { it.pageId == pageId }
    }

    fun getLevel(id: String): Int = _levels[id] ?: 0

    fun setLevel(id: String, level: Int) {
        _levels[id] = level
    }

    fun getCost(definition: UpgradeDefinition, level: Int): Map<String, BigNumber> {
        var multiplierProduct = BigNumber.ONE
        for (l in 0 until level - 1) {
            multiplierProduct = multiplierProduct * definition.scalingFunction(l)
        }
        return definition.baseCost.mapValues { (_, base) -> base * multiplierProduct }
    }

    fun getCurrentCost(definition: UpgradeDefinition): Map<String, BigNumber> {
        return getCost(definition, getLevel(definition.id) + 1)
    }

    fun canUpgrade(definition: UpgradeDefinition, currencyManager: CurrencyManager): Boolean {
        if (getLevel(definition.id) >= definition.maxLevel) return false
        val cost = getCurrentCost(definition)
        return currencyManager.hasEnough(cost)
    }

    fun purchaseUpgrade(
        definition: UpgradeDefinition,
        currencyManager: CurrencyManager
    ): Boolean {
        val currentLevel = getLevel(definition.id)
        if (currentLevel >= definition.maxLevel) return false
        val cost = getCurrentCost(definition)
        if (currencyManager.spend(cost)) {
            _levels[definition.id] = currentLevel + 1
            return true
        }
        return false
    }

    /** Call this each frame/tick to check unlock transitions */
    fun checkUnlocks(viewModel: GameViewModel) {
        for (def in definitions.values) {
            if (def.onUnlock == null) continue
            if (def.id in firedUnlocks) continue
            if (def.isUnlocked(viewModel.state)) {
                firedUnlocks.add(def.id)
                def.onUnlock.invoke(viewModel)
            }
        }
    }

    fun restore(savedLevels: Map<String, Int>) {
        for ((id, level) in savedLevels) {
            _levels[id] = level
        }
    }

    fun reset() {
        _levels.clear()
        firedUnlocks.clear()
    }

    fun getAllLevels(): Map<String, Int> = _levels.toMap()

    val spawnTierLevel: Int
        get() = 1 + getLevel("spawn_tier")

    val spawnRateLevel: Int
        get() = getLevel("spawn_rate")
}