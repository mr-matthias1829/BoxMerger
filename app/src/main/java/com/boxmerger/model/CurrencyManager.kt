package com.boxmerger.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class CurrencyType {
    BOXES,
    GEM,
    PRESTIGE
}

class CurrencyManager {
    var boxes by mutableStateOf(BigNumber.ZERO)
        private set
    var gems by mutableStateOf(BigNumber.ZERO)
        private set
    var prestige by mutableStateOf(BigNumber.ZERO)
        private set

    fun getBalance(type: CurrencyType): BigNumber {
        return when (type) {
            CurrencyType.BOXES -> boxes
            CurrencyType.GEM -> gems
            CurrencyType.PRESTIGE -> prestige
        }
    }

    fun add(type: CurrencyType, amount: BigNumber) {
        if (amount.compareTo(BigNumber.ZERO) <= 0) return
        when (type) {
            CurrencyType.BOXES -> boxes += amount
            CurrencyType.GEM -> gems += amount
            CurrencyType.PRESTIGE -> prestige += amount
        }
    }

    fun add(type: CurrencyType, amount: Double) {
        add(type, BigNumber(amount))
    }

    fun hasEnough(costs: Map<CurrencyType, BigNumber>): Boolean {
        for ((type, cost) in costs) {
            if (getBalance(type).compareTo(cost) < 0) return false
        }
        return true
    }

    fun spend(costs: Map<CurrencyType, BigNumber>): Boolean {
        if (!hasEnough(costs)) return false
        for ((type, cost) in costs) {
            when (type) {
                CurrencyType.BOXES -> boxes = boxes - cost
                CurrencyType.GEM -> gems = gems - cost
                CurrencyType.PRESTIGE -> prestige = prestige - cost
            }
        }
        return true
    }

    fun restore(savedBoxes: BigNumber, savedGems: BigNumber, savedPrestige: BigNumber) {
        boxes = savedBoxes
        gems = savedGems
        prestige = savedPrestige
    }

    fun resetForPrestige() {
        boxes = BigNumber.ZERO
    }

    fun resetAll() {
        boxes = BigNumber.ZERO
        gems = BigNumber.ZERO
        prestige = BigNumber.ZERO
    }
}
