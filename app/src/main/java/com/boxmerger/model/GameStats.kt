// model/GameStats.kt
package com.boxmerger.model

class GameStats {
    private val longs = mutableMapOf<String, Long>()
    private val doubles = mutableMapOf<String, Double>()

    fun get(key: String): Long = longs[key] ?: 0L
    fun getDouble(key: String): Double = doubles[key] ?: 0.0

    fun set(key: String, value: Long) { longs[key] = value }
    fun setDouble(key: String, value: Double) { doubles[key] = value }

    fun increment(key: String, amount: Long = 1L) {
        longs[key] = get(key) + amount
    }

    fun addDouble(key: String, amount: Double) {
        doubles[key] = getDouble(key) + amount
    }

    fun all(): Map<String, Long> = longs.toMap()
    fun allDoubles(): Map<String, Double> = doubles.toMap()

    fun restore(longMap: Map<String, Long>, doubleMap: Map<String, Double> = emptyMap()) {
        longs.clear()
        longs.putAll(longMap)
        doubles.clear()
        doubles.putAll(doubleMap)
    }

    fun reset() {
        longs.clear()
        doubles.clear()
    }
}