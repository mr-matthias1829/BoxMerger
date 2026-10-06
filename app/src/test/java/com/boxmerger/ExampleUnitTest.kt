package com.boxmerger

import com.boxmerger.logic.PrestigeLogic
import com.boxmerger.model.BigNumber
import com.boxmerger.model.UpgradeManager
import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {

    // below are tests to test the custom bignum system
    // this is since there's been some... INCIDENTS, during early testing
    // having any of them occur again WILL be problematic

    @Test
    fun testNonEngineeringExponentComparison() {
        // this bug thought that 1.7e8 > 700e6 was true
        // but it isnt, since 700e6 = 7e8, which is more and thus false
        // originally it looked ONLY at exponents first and thought "hm, the first one bigger, so its true!"

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
        // bug: costs werent bignum

        val upgradeManager = UpgradeManager()
        upgradeManager.registerDefinition(PrestigeLogic.presMoreBoxesUpgrade)

        // Cost for level 250
        val cost250 = upgradeManager.getCost(PrestigeLogic.presMoreBoxesUpgrade, 250)["prestige"]!!
        assertFalse("Cost at L250 should not be ZERO or infinite", cost250.isZero())
        assertTrue("Cost at L250 exponent should be > 0", cost250.exponent > 0)

        // Cost for level 7 (purchase #7: 400 * 10^6 = 400M)
        val cost7 = upgradeManager.getCost(PrestigeLogic.presMoreBoxesUpgrade, 7)["prestige"]!!
        assertEquals(6, cost7.exponent)
        assertEquals(400.0, cost7.mantissa, 0.001)
    }

    @Test
    fun testPowNormalization() {
        // system that turns bignum to multiple of 3
        // if we didnt do this, it'd be more steps to make readable and to compare
        // plus i personally find it way more readable

        val base = BigNumber.of(10.0)
        val result = base.pow(8.24) // ~1.737e8
        assertEquals(6, result.exponent) // Should be normalized to engineering exponent (multiple of 3)
    }
}