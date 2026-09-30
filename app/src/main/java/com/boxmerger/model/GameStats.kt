// model/GameState.kt
package com.boxmerger.model

/**
 * Snapshot of state relevant for conditions (upgrades, achievements, tabs).
 * Keep this lightweight; it's recreated often.
 */
class GameState(
    val currencyManager: CurrencyManager,
    val upgradeManager: UpgradeManager,
    val stats: GameStats,
    val flags: Set<String>
) {
    fun hasFlag(flag: String): Boolean = flag in flags
    fun currency(id: String): BigNumber = currencyManager.getBalance(id)
    fun upgradeLevel(id: String): Int = upgradeManager.getLevel(id)
    fun stat(key: String): Long = stats.get(key)
}

class GameStats {
    private val values = mutableMapOf<String, Long>()

    fun get(key: String): Long = values[key] ?: 0L
    fun set(key: String, value: Long) { values[key] = value }
    fun increment(key: String, amount: Long = 1L) {
        values[key] = get(key) + amount
    }
    fun all(): Map<String, Long> = values.toMap()
    fun restore(map: Map<String, Long>) {
        values.clear()
        values.putAll(map)
    }
    fun reset() { values.clear() }
}