// model/Currency.kt
package com.boxmerger.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * A currency type. Icons resolved via IconResolver.
 * topBarPages: set of page IDs where this currency shows in the top bar.
 *   Empty set = always show (or never if showInTopBar is false).
 */
data class Currency(
    val id: String,
    val displayName: String,
    val iconName: String,          // "icon_boxes", "icon_gem", etc.
    val fallbackEmoji: String,     // used if PNG not found
    val showInTopBar: Boolean = true,
    val topBarPages: Set<String> = emptySet()  // empty = all pages
)

object Currencies {
    val BOXES = Currency("boxes", "Boxes", "icon_boxes", "📦")
    val GEM = Currency("gem", "Gems", "icon_gem", "💎")
    val PRESTIGE = Currency("prestige", "Prestige", "icon_prestige", "⚡")

    val ALL = listOf(BOXES, GEM, PRESTIGE)

    fun byId(id: String): Currency? = ALL.firstOrNull { it.id == id }
}

class CurrencyManager {
    private val _balances = mutableStateMapOf<String, BigNumber>()

    init {
        for (c in Currencies.ALL) {
            _balances[c.id] = BigNumber.ZERO
        }
    }

    fun getBalance(currencyId: String): BigNumber {
        return _balances[currencyId] ?: BigNumber.ZERO
    }

    fun getBalance(currency: Currency): BigNumber = getBalance(currency.id)

    /** Expose as state for Compose */
    fun balanceState(currencyId: String): BigNumber {
        return _balances[currencyId] ?: BigNumber.ZERO
    }

    fun add(currencyId: String, amount: BigNumber) {
        if (amount.compareTo(BigNumber.ZERO) <= 0) return
        val current = getBalance(currencyId)
        _balances[currencyId] = current + amount
    }

    fun add(currency: Currency, amount: BigNumber) = add(currency.id, amount)

    fun add(currencyId: String, amount: Double) = add(currencyId, BigNumber.of(amount))

    fun hasEnough(costs: Map<String, BigNumber>): Boolean {
        for ((id, cost) in costs) {
            if (getBalance(id).compareTo(cost) < 0) return false
        }
        return true
    }

    fun spend(costs: Map<String, BigNumber>): Boolean {
        if (!hasEnough(costs)) return false
        for ((id, cost) in costs) {
            _balances[id] = getBalance(id) - cost
        }
        return true
    }

    fun setBalance(currencyId: String, amount: BigNumber) {
        _balances[currencyId] = amount
    }

    fun resetForPrestige() {
        _balances[Currencies.BOXES.id] = BigNumber.ZERO
    }

    fun resetAll() {
        for (c in Currencies.ALL) {
            _balances[c.id] = BigNumber.ZERO
        }
    }

    fun getAllBalances(): Map<String, BigNumber> = _balances.toMap()
}