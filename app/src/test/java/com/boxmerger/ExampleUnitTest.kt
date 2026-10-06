package com.boxmerger

import com.boxmerger.logic.PrestigeLogic
import com.boxmerger.model.BigNumber
import com.boxmerger.model.UpgradeManager
import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun testNonEngineeringExponentComparison() {
        // Balance = 174M represented as 1.74 * 10^8
        val balance = BigNumber.of(1.74, 8)
        // Cost = 700M represented as 700.0 * 10^6
        val cost = BigNumber.of(700.0, 6)

        // 174M should be LESS than 700M
        assertTrue("174M should be less than 700M", balance < cost)
        assertEquals(6, balance.exponent)
        assertEquals(6, cost.exponent)
        assertEquals(174.0, balance.mantissa, 0.001)
        assertEquals(700.0, cost.mantissa, 0.001)
    }

    @Test
    fun testUpgradeCostOverflow() {
        val upgradeManager = UpgradeManager()
        upgradeManager.registerDefinition(PrestigeLogic.presMoreBoxesUpgrade)

        // Cost for level 250
        val cost250 = upgradeManager.getCost(PrestigeLogic.presMoreBoxesUpgrade, 250)["prestige"]!!
        assertFalse("Cost at L250 should not be ZERO or infinite", cost250.isZero())
        assertTrue("Cost at L250 exponent should be > 0", cost250.exponent > 0)

        // Cost for level 7 should be 700M (400 * 10^5 * 17.5 = 700M)
        val cost7 = upgradeManager.getCost(PrestigeLogic.presMoreBoxesUpgrade, 7)["prestige"]!!
        assertEquals(6, cost7.exponent)
        assertEquals(700.0, cost7.mantissa, 0.001)
    }

    @Test
    fun testPowNormalization() {
        val base = BigNumber.of(10.0)
        val result = base.pow(8.24) // ~1.737e8
        assertEquals(6, result.exponent) // Should be normalized to engineering exponent (multiple of 3)
    }
}