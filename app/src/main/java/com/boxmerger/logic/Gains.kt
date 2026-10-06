// logic/Gains.kt
package com.boxmerger.logic

import com.boxmerger.model.BigNumber
import com.boxmerger.model.Currencies
import com.boxmerger.model.CurrencyManager
import com.boxmerger.model.GridItem
import com.boxmerger.model.UpgradeManager
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Every "how much do I gain" calculation lives here.
 *
 * Call Gains.xxx() from anywhere — GameViewModel, LevelingLogic, UI, popups.
 * Every method returns the FINAL, fully-multiplied value. Never multiply at
 * the call site.
 *
 * Multiplier methods return Double for display purposes. Full gain methods
 * return BigNumber where values can exceed Double range.
 */
object Gains {

    private lateinit var currencyManager: CurrencyManager
    private lateinit var upgradeManager: UpgradeManager
    private var playerLevelProvider: () -> Int = { 0 }
    private var levelResetPrestigeBoostProvider: () -> Double = { 0.0 }

    /**
     * Called once from GameViewModel.init. Everything after this can call the
     * methods below without passing anything.
     */
    fun init(
        currencyManager: CurrencyManager,
        upgradeManager: UpgradeManager,
        playerLevelProvider: () -> Int,
        levelResetPrestigeBoostProvider: () -> Double
    ) {
        this.currencyManager = currencyManager
        this.upgradeManager = upgradeManager
        this.playerLevelProvider = playerLevelProvider
        this.levelResetPrestigeBoostProvider = levelResetPrestigeBoostProvider
    }

    // -------------------------------------------------------------------------
    // Multipliers — exposed so the UI can display them if it wants.
    // These are always small enough for Double.
    // -------------------------------------------------------------------------

    fun gemMultiplier(): Double {
        return PrestigeLogic.presMoreGemsUpgrade.effectFormula(
            upgradeManager.getLevel(PrestigeLogic.presMoreGemsUpgrade.id)
        )
    }

    fun boxMultiplier(): Double {
        val gemBoxes = GemUpgradesLogic.gemMoreBoxesUpgrade.effectFormula(
            upgradeManager.getLevel(GemUpgradesLogic.gemMoreBoxesUpgrade.id)
        )
        val presBoxes = PrestigeLogic.presMoreBoxesUpgrade.effectFormula(
            upgradeManager.getLevel(PrestigeLogic.presMoreBoxesUpgrade.id)
        )
        val levelBoost = LevelingLogic.blockEarningsMultiplier(playerLevelProvider())
        val prestigeHeld = currencyManager.getBalance(Currencies.PRESTIGE).toDouble()
        val prestigeBoost = 1.0 + 0.01 * prestigeHeld
        return gemBoxes * presBoxes * levelBoost * prestigeBoost
    }

    fun prestigeMultiplier(): Double {
        val gemPrestige = GemUpgradesLogic.gemMorePrestigeUpgrade.effectFormula(
            upgradeManager.getLevel(GemUpgradesLogic.gemMorePrestigeUpgrade.id)
        )
        val presPrestige = PrestigeLogic.presMorePrestigeUpgrade.effectFormula(
            upgradeManager.getLevel(PrestigeLogic.presMorePrestigeUpgrade.id)
        )
        val resetBoost = 1.0 + levelResetPrestigeBoostProvider()
        return gemPrestige * presPrestige * resetBoost
    }

    // -------------------------------------------------------------------------
    // Full gain methods — return BigNumber where values can grow large.
    // -------------------------------------------------------------------------

    /**
     * How many gems a single successful drop is worth, after all multipliers.
     */
    fun gemDropValue(): BigNumber {
        return BigNumber.of(1.0 * gemMultiplier())
    }

    /**
     * How many gems reaching [level] gives, after all multipliers.
     */
    fun levelUpGems(level: Int): BigNumber {
        var base = 0.5
        if (level <= 25) {
            base += + (level / 1) * 0.04
        } else {
            base += 1 // lvl 25 in above formula
            base += + (level - 25) * 0.005
        }

        return BigNumber.of(base * gemMultiplier())
    }

    /**
     * Boxes awarded for a merge that produced [newTier].
     * Uses BigNumber because tier can grow large.
     */
    fun boxMergeValue(newTier: Int): BigNumber {
        return BigNumber.of(10.0 * newTier)
    }

    /**
     * Boxes earned per second from the current grid, after all multipliers.
     * Uses BigNumber because 3^tier can exceed Double range at high tiers.
     */
    fun passiveBoxPerSecond(gridItems: List<GridItem?>): BigNumber {
        var total = BigNumber.ZERO
        for (item in gridItems) {
            if (item != null) {
                val tierValue = BigNumber.of(3.0).pow((item.tier - 1).toDouble())
                total += tierValue
            }
        }
        if (total.isZero()) return BigNumber.ZERO
        return total * boxMultiplier()
    }

    /**
     * Prestige currency earned by prestiging at the current box balance.
     * Uses BigNumber because box balance can be huge.
     */



    // Boxes above this count with reduced strength
    private val PRESTIGE_SOFTCAP = BigNumber.of(1e80)
    // 1.0 = no slowdown, lower = harder slowdown past the cap
    private const val PRESTIGE_SOFTCAP_POWER = 0.5

    fun prestigeGain(): BigNumber {
        val boxes = currencyManager.getBalance(Currencies.BOXES)
        val threshold = BigNumber.of(1e6)

        if (boxes <= threshold) return BigNumber.ZERO

        var effective = boxes - threshold

        // Soft cap: past 1e16, only (excess ratio)^0.5 counts
        if (effective > PRESTIGE_SOFTCAP) {
            effective = PRESTIGE_SOFTCAP * (effective / PRESTIGE_SOFTCAP).pow(PRESTIGE_SOFTCAP_POWER)
        }

        val base = effective.sqrt()
        if (base.isZero()) return BigNumber.ZERO

        val inner = base.pow(0.3) - BigNumber.of(10.0)
        if (inner <= BigNumber.ZERO) return BigNumber.ZERO

        return inner * prestigeMultiplier()
    }

    /**
     * The chance (0..100) that a merge drops a gem.
     */
    fun gemDropChance(): Double {
        return GemUpgradesLogic.gemIncreaseChanceUpgrade.effectFormula(
            upgradeManager.getLevel(GemUpgradesLogic.gemIncreaseChanceUpgrade.id)
        )
    }
}