package com.boxmerger

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import com.boxmerger.model.BigNumber
import com.boxmerger.model.CurrencyType
import com.boxmerger.model.GridItem
import com.boxmerger.model.UpgradeManager
import com.boxmerger.model.CurrencyManager
import com.boxmerger.model.UpgradeDefinition
import com.boxmerger.model.gemAutoSpeedUpgrade
import com.boxmerger.model.gemIncreaseChanceUpgrade
import com.boxmerger.model.gemMoreBoxesUpgrade
import com.boxmerger.model.gemMorePrestigeUpgrade
import com.boxmerger.model.presMoreBoxesUpgrade
import com.boxmerger.model.presMoreGemsUpgrade
import com.boxmerger.model.presMorePrestigeUpgrade
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {
    val currencyManager = CurrencyManager()
    val upgradeManager = UpgradeManager()

    // 5x4 grid = 20 cells
    private val _gridItems = mutableStateListOf<GridItem?>().apply {
        repeat(20) { add(null) }
    }
    val gridItems: List<GridItem?> get() = _gridItems

    // Stats
    var manualMerges by mutableStateOf(0)
        private set
    var autoMerges by mutableStateOf(0)
        private set
    val totalMerges: Int get() = manualMerges + autoMerges

    var playtimeSeconds by mutableStateOf(0L)
        private set

    var totalPrestiges by mutableStateOf(0)
        private set

    val autoMergerUnlocked: Boolean get() = totalPrestiges > 0
    var autoMergerEnabled by mutableStateOf(false)

    private var nextItemId = 1L
    private val prefs = application.getSharedPreferences("box_merger_prefs", Context.MODE_PRIVATE)

    init {
        loadGame()
        startPlaytimeTracker()
        startPassiveIncomeLoop()
        startSpawnerLoop()
        startAutoMergerLoop()
        startAutoSaveLoop()
    }

    private fun startPlaytimeTracker() {
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                playtimeSeconds++
            }
        }
    }

    private var passiveJob: Job? = null
    private fun startPassiveIncomeLoop() {
        passiveJob?.cancel()
        passiveJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                var totalEarnings = 0.0
                for (item in _gridItems) {
                    if (item != null) {
                        totalEarnings += 3.0.pow((item.tier - 1).toDouble())
                    }
                }
                if (totalEarnings > 0.0) {
                    // Apply Multipliers: Box upgrades + Prestige passive bonus (+1% per prestige)
                    val boxMultiplier = gemMoreBoxesUpgrade.effectFormula(upgradeManager.getLevel(gemMoreBoxesUpgrade.id)) *
                            presMoreBoxesUpgrade.effectFormula(upgradeManager.getLevel(presMoreBoxesUpgrade.id)) *
                            (1.0 + 0.01 * currencyManager.prestige.mantissa * 10.0.pow(currencyManager.prestige.exponent.toDouble())) // simplified prestige count

                    val finalEarnings = totalEarnings * boxMultiplier
                    currencyManager.add(CurrencyType.BOXES, BigNumber(finalEarnings))
                }
            }
        }
    }

    private var spawnerJob: Job? = null
    private fun startSpawnerLoop() {
        spawnerJob?.cancel()
        spawnerJob = viewModelScope.launch {
            while (true) {
                val interval = upgradeManager.getSpawnIntervalMs()
                delay(interval)
                spawnSequentialItem()
            }
        }
    }

    fun spawnSequentialItem() {
        val firstEmptyIndex = _gridItems.indexOfFirst { it == null }
        if (firstEmptyIndex != -1) {
            val tier = upgradeManager.spawnTierLevel
            _gridItems[firstEmptyIndex] = GridItem(nextItemId++, tier)
        }
    }

    private fun checkGemDrop() {
        val chance = gemIncreaseChanceUpgrade.effectFormula(upgradeManager.getLevel(gemIncreaseChanceUpgrade.id))
        if (Random.nextDouble() * 100.0 < chance) {
            val gemMultiplier = presMoreGemsUpgrade.effectFormula(upgradeManager.getLevel(presMoreGemsUpgrade.id))
            currencyManager.add(CurrencyType.GEM, 1.0 * gemMultiplier)
        }
    }

    fun moveOrMerge(fromIndex: Int, toIndex: Int, isAuto: Boolean = false) {
        if (fromIndex !in 0..19 || toIndex !in 0..19 || fromIndex == toIndex) return
        val source = _gridItems[fromIndex] ?: return
        val target = _gridItems[toIndex]

        if (target == null) {
            _gridItems[toIndex] = source
            _gridItems[fromIndex] = null
        } else if (target.tier == source.tier) {
            val newTier = target.tier + 1
            _gridItems[toIndex] = GridItem(nextItemId++, newTier)
            _gridItems[fromIndex] = null
            currencyManager.add(CurrencyType.BOXES, BigNumber(10.0 * newTier))

            if (isAuto) {
                autoMerges++
            } else {
                manualMerges++
            }
            checkGemDrop()
        } else {
            _gridItems[toIndex] = source
            _gridItems[fromIndex] = target
        }
    }

    private var autoMergerJob: Job? = null
    private fun startAutoMergerLoop() {
        autoMergerJob?.cancel()
        autoMergerJob = viewModelScope.launch {
            while (true) {
                val speedSeconds = gemAutoSpeedUpgrade.effectFormula(upgradeManager.getLevel(gemAutoSpeedUpgrade.id))
                delay((speedSeconds * 1000.0).toLong())

                if (autoMergerUnlocked && autoMergerEnabled) {
                    performAutoMerge()
                }
            }
        }
    }

    private fun performAutoMerge() {
        // Scan from top-left to bottom-right (0 to 19)
        for (i in 0 until 20) {
            val itemI = _gridItems[i] ?: continue
            // Find any subsequent slot j > i with the same tier
            for (j in (i + 1) until 20) {
                val itemJ = _gridItems[j]
                if (itemJ != null && itemJ.tier == itemI.tier) {
                    // Merge j into i (most top-left slot)
                    moveOrMerge(j, i, isAuto = true)
                    return
                }
            }
        }
    }

    fun buyUpgrade(definition: UpgradeDefinition) {
        val success = upgradeManager.purchaseUpgrade(definition, currencyManager)
        if (success) {
            if (definition.id == "spawn_tier") {
                val newTier = upgradeManager.spawnTierLevel
                for (i in _gridItems.indices) {
                    val item = _gridItems[i]
                    if (item != null && item.tier < newTier) {
                        _gridItems[i] = item.copy(tier = newTier)
                    }
                }
            } else if (definition.id == "spawn_rate") {
                startSpawnerLoop()
            }
            saveGame()
        }
    }

    fun calculatePrestigeGain(): BigNumber {
        val boxesDouble = currencyManager.boxes.mantissa * 10.0.pow(currencyManager.boxes.exponent.toDouble())
        val base = Math.pow(sqrt(boxesDouble-1e6), 0.3) - 10
        if (base < 0 || base.isNaN()) return BigNumber.ZERO
        val multiplier = gemMorePrestigeUpgrade.effectFormula(upgradeManager.getLevel(gemMorePrestigeUpgrade.id)) *
                presMorePrestigeUpgrade.effectFormula(upgradeManager.getLevel(presMorePrestigeUpgrade.id))
        val gain = maxOf(0.0, base * multiplier)
        return BigNumber(gain)
    }

    val canPrestige: Boolean
        get() {
            if (totalPrestiges > 0) return true
            return _gridItems.any { it != null && it.tier >= 20 }
        }

    fun performPrestige() {
        if (!canPrestige) return
        val gain = calculatePrestigeGain()
        if (gain.compareTo(BigNumber.ZERO) > 0) {
            currencyManager.add(CurrencyType.PRESTIGE, gain)
            totalPrestiges++
            currencyManager.resetForPrestige()
            upgradeManager.setLevel("spawn_tier", 1)
            upgradeManager.setLevel("spawn_rate", 1)
            for (i in _gridItems.indices) {
                _gridItems[i] = null
            }
            _gridItems[7] = GridItem(nextItemId++, 1)
            _gridItems[12] = GridItem(nextItemId++, 1)
            saveGame()
        }
    }

    private var autoSaveJob: Job? = null
    private fun startAutoSaveLoop() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            while (true) {
                delay(10000L)
                saveGame()
            }
        }
    }

    fun saveGame() {
        val editor = prefs.edit()
        editor.putString("boxes_mantissa", currencyManager.boxes.mantissa.toString())
        editor.putInt("boxes_exponent", currencyManager.boxes.exponent)
        editor.putString("gems_mantissa", currencyManager.gems.mantissa.toString())
        editor.putInt("gems_exponent", currencyManager.gems.exponent)
        editor.putString("prestige_mantissa", currencyManager.prestige.mantissa.toString())
        editor.putInt("prestige_exponent", currencyManager.prestige.exponent)

        editor.putInt("manual_merges", manualMerges)
        editor.putInt("auto_merges", autoMerges)
        editor.putLong("playtime_seconds", playtimeSeconds)
        editor.putInt("total_prestiges", totalPrestiges)
        editor.putBoolean("auto_merger_enabled", autoMergerEnabled)
        editor.putLong("last_save_time", System.currentTimeMillis())

        for ((id, level) in upgradeManager.getAllLevels()) {
            editor.putInt("upgrade_level_$id", level)
        }

        for (i in 0 until 20) {
            val item = _gridItems[i]
            if (item != null) {
                editor.putLong("grid_${i}_id", item.id)
                editor.putInt("grid_${i}_tier", item.tier)
            } else {
                editor.remove("grid_${i}_id")
                editor.remove("grid_${i}_tier")
            }
        }
        editor.apply()
    }

    private fun loadGame() {
        val boxesMantissa = prefs.getString("boxes_mantissa", "0.0")!!.toDouble()
        val boxesExponent = prefs.getInt("boxes_exponent", 0)
        val gemsMantissa = prefs.getString("gems_mantissa", "0.0")!!.toDouble()
        val gemsExponent = prefs.getInt("gems_exponent", 0)
        val presMantissa = prefs.getString("prestige_mantissa", "0.0")!!.toDouble()
        val presExponent = prefs.getInt("prestige_exponent", 0)

        currencyManager.restore(
            BigNumber(boxesMantissa, boxesExponent),
            BigNumber(gemsMantissa, gemsExponent),
            BigNumber(presMantissa, presExponent)
        )

        manualMerges = prefs.getInt("manual_merges", 0)
        autoMerges = prefs.getInt("auto_merges", 0)
        playtimeSeconds = prefs.getLong("playtime_seconds", 0L)
        totalPrestiges = prefs.getInt("total_prestiges", 0)
        autoMergerEnabled = prefs.getBoolean("auto_merger_enabled", false)

        val levels = mutableMapOf<String, Int>()
        val allUpgradeIds = listOf(
            "spawn_tier", "spawn_rate",
            "gem_more_boxes", "gem_more_prestige", "gem_increase_chance", "gem_auto_speed",
            "pres_more_boxes", "pres_more_gems", "pres_more_prestige"
        )
        for (id in allUpgradeIds) {
            val lvl = prefs.getInt("upgrade_level_$id", 1)
            if (lvl > 1) {
                levels[id] = lvl
            }
        }
        upgradeManager.restore(levels)

        var maxId = 0L
        for (i in 0 until 20) {
            val id = prefs.getLong("grid_${i}_id", -1L)
            val tier = prefs.getInt("grid_${i}_tier", -1)
            if (id != -1L && tier != -1) {
                _gridItems[i] = GridItem(id, tier)
                if (id > maxId) maxId = id
            } else {
                _gridItems[i] = null
            }
        }
        nextItemId = maxId + 1

        if (_gridItems.all { it == null }) {
            _gridItems[7] = GridItem(nextItemId++, upgradeManager.spawnTierLevel)
            _gridItems[12] = GridItem(nextItemId++, upgradeManager.spawnTierLevel)
        }
    }

    fun hardReset() {
        prefs.edit().clear().apply()
        currencyManager.resetAll()
        upgradeManager.reset()
        manualMerges = 0
        autoMerges = 0
        playtimeSeconds = 0L
        totalPrestiges = 0
        autoMergerEnabled = false
        for (i in _gridItems.indices) {
            _gridItems[i] = null
        }
        nextItemId = 1L
        _gridItems[7] = GridItem(nextItemId++, 1)
        _gridItems[12] = GridItem(nextItemId++, 1)
        saveGame()
    }

    override fun onCleared() {
        super.onCleared()
        saveGame()
    }
}
