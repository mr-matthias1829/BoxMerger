// GameViewModel.kt
package com.boxmerger

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.boxmerger.logic.AchievementsLogic
import com.boxmerger.logic.Gains
import com.boxmerger.logic.GameLogic
import com.boxmerger.logic.GemUpgradesLogic
import com.boxmerger.logic.LevelingLogic
import com.boxmerger.logic.PrestigeLogic
import com.boxmerger.logic.SpecialityLogic
import com.boxmerger.model.*
import com.boxmerger.ui.PopupEvent
import com.boxmerger.ui.achievementPopup
import com.boxmerger.ui.levelUpPopup
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val currencyManager = CurrencyManager()
    val upgradeManager = UpgradeManager()
    val achievementManager = AchievementManager()
    val stats = GameStats()

    // Single popup queue for all popup types
    val popupQueue = mutableStateListOf<PopupEvent>()
    private var nextPopupId = 1L

    // 5x4 grid = 20 cells
    private val _gridItems = mutableStateListOf<GridItem?>().apply {
        repeat(20) { add(null) }
    }
    val gridItems: List<GridItem?> get() = _gridItems

    // Flags (for achievements/tab visibility)
    private val _flags = mutableStateListOf<String>()
    val flags: Set<String> get() = _flags.toSet()

    // Stats exposed for UI
    var playtimeSeconds by mutableStateOf(0L)
        private set
    var autoMergerEnabled by mutableStateOf(false)
    var simpleBoxesEnabled by mutableStateOf(false)

    // Leveling state
    var playerLevel by mutableStateOf(0)
        private set
    var levelingMerges by mutableStateOf(0L)
        private set
    var levelResetPrestigeBoost by mutableStateOf(0.0)
        private set
    var levelResets by mutableStateOf(0)
        private set

    var totalPrestiges: Int
        get() = stats.get("total_prestiges").toInt()
        private set(value) { stats.set("total_prestiges", value.toLong()) }

    val autoMergerUnlocked: Boolean get() = hasFlag("auto_merger_unlocked")

    private var nextItemId = 1L
    private val prefs = application.getSharedPreferences("box_merger_prefs", Context.MODE_PRIVATE)

    // Snapshot for conditions
    val state: GameState
        get() = GameState(currencyManager, upgradeManager, stats, flags)

    init {
        registerAllDefinitions()
        loadGame()

        // Wire up the global Gains object so LevelingLogic and any other
        // logic file can call Gains.xxx() without passing anything.
        Gains.init(
            currencyManager = currencyManager,
            upgradeManager = upgradeManager,
            playerLevelProvider = { playerLevel },
            levelResetPrestigeBoostProvider = { levelResetPrestigeBoost }
        )

        startLoops()
    }

    private fun registerAllDefinitions() {
        upgradeManager.registerAll(GameLogic.ALL)
        upgradeManager.registerAll(GemUpgradesLogic.ALL)
        upgradeManager.registerAll(PrestigeLogic.ALL)
        upgradeManager.registerAll(SpecialityLogic.ALL)
        achievementManager.registerAll(AchievementsLogic.ALL)
    }

    private fun startLoops() {
        startPlaytimeTracker()
        startPassiveIncomeLoop()
        startSpawnerLoop()
        startAutoMergerLoop()
        startAutoSaveLoop()
        startConditionCheckLoop()
    }

    // --- Flags ---

    fun setFlag(flag: String) {
        if (!_flags.contains(flag)) {
            _flags.add(flag)
        }
    }

    fun hasFlag(flag: String): Boolean = _flags.contains(flag)

    // --- Playtime ---

    private fun startPlaytimeTracker() {
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                playtimeSeconds++
                stats.set("playtime_seconds", playtimeSeconds)
            }
        }
    }

    // --- Passive income ---

    private var passiveJob: Job? = null
    private fun startPassiveIncomeLoop() {
        passiveJob?.cancel()
        passiveJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val income = Gains.passiveBoxPerSecond(_gridItems)
                if (income.compareTo(BigNumber.ZERO) > 0) {
                    currencyManager.add(Currencies.BOXES, income)
                }
            }
        }
    }

    // --- Spawner ---

    private var spawnerJob: Job? = null
    private fun startSpawnerLoop() {
        spawnerJob?.cancel()
        spawnerJob = viewModelScope.launch {
            while (true) {
                val interval = getSpawnIntervalMs()
                delay(interval)
                spawnSequentialItem()
            }
        }
    }

    fun restartSpawner() {
        startSpawnerLoop()
    }

    private fun getSpawnIntervalMs(): Long {
        val rateLevel = upgradeManager.getLevel(GameLogic.spawnRateUpgrade.id)
        val seconds = GameLogic.spawnRateUpgrade.effectFormula(rateLevel)
        return maxOf(100L, (seconds * 1000.0).toLong())
    }

    fun spawnSequentialItem() {
        val firstEmptyIndex = _gridItems.indexOfFirst { it == null }
        if (firstEmptyIndex != -1) {
            val tier = upgradeManager.spawnTierLevel
            _gridItems[firstEmptyIndex] = GridItem(nextItemId++, tier)

            // Double box spawn check
            val doubleLevel = upgradeManager.getLevel(SpecialityLogic.doubleSpawnUpgrade.id)
            if (doubleLevel > 0) {
                val chance = SpecialityLogic.doubleSpawnUpgrade.effectFormula(doubleLevel)
                if (Random.nextDouble() * 100.0 < chance) {
                    val secondEmptyIndex = _gridItems.indexOfFirst { it == null }
                    if (secondEmptyIndex != -1) {
                        _gridItems[secondEmptyIndex] = GridItem(nextItemId++, tier)
                    }
                }
            }
        }
    }

    // --- Leveling ---

    fun mergesNeededForNextLevel(level: Int = playerLevel): Long =
        LevelingLogic.mergesNeededForNextLevel(level)

    private fun processLevelingMerge() {
        levelingMerges++
        var needed = mergesNeededForNextLevel(playerLevel)
        while (levelingMerges >= needed) {
            levelingMerges -= needed
            playerLevel++
            stats.set("player_level", playerLevel.toLong())

            val reward = Gains.levelUpGems(playerLevel)
            currencyManager.add(Currencies.GEM, reward)
            popupQueue.add(levelUpPopup(playerLevel, reward, nextPopupId++))

            needed = mergesNeededForNextLevel(playerLevel)
        }
    }

    val canLevelReset: Boolean
        get() = hasFlag("level_reset_unlocked") &&
                playerLevel >= LevelingLogic.levelResetRequirement()

    fun performLevelReset() {
        if (!canLevelReset) return
        val boostGained = LevelingLogic.levelResetPrestigeBoostGain(playerLevel)
        levelResetPrestigeBoost += boostGained
        levelResets++
        playerLevel = 0
        levelingMerges = 0L
        stats.set("player_level", 0L)
        saveGame()
    }

    // --- Merging ---

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

            currencyManager.add(Currencies.BOXES, Gains.boxMergeValue(newTier))

            if (isAuto) {
                stats.increment("auto_merges")
            } else {
                stats.increment("manual_merges")
            }
            stats.increment("total_merges")

            if (newTier > stats.get("highest_tier")) {
                stats.set("highest_tier", newTier.toLong())
            }

            when {
                state.stat("highest_tier") >= 5 -> {
                    checkGemDrop()
                }}
            when {
                hasFlag("leveling_unlocked") -> {
                    processLevelingMerge()
                }}

        } else {
            _gridItems[toIndex] = source
            _gridItems[fromIndex] = target
        }
    }

    private fun checkGemDrop() {
        if (Random.nextDouble() * 100.0 < Gains.gemDropChance()) {
            currencyManager.add(Currencies.GEM, Gains.gemDropValue())
        }
    }

    // --- Auto merger ---

    private var autoMergerJob: Job? = null
    private fun startAutoMergerLoop() {
        autoMergerJob?.cancel()
        autoMergerJob = viewModelScope.launch {
            while (true) {
                val speedSeconds = GemUpgradesLogic.gemAutoSpeedUpgrade.effectFormula(
                    upgradeManager.getLevel(GemUpgradesLogic.gemAutoSpeedUpgrade.id)
                )
                delay((speedSeconds * 1000.0).toLong())

                if (autoMergerUnlocked && autoMergerEnabled) {
                    performAutoMerge()
                }
            }
        }
    }

    private fun performAutoMerge() {
        for (i in 0 until 20) {
            val itemI = _gridItems[i] ?: continue
            for (j in (i + 1) until 20) {
                val itemJ = _gridItems[j]
                if (itemJ != null && itemJ.tier == itemI.tier) {
                    moveOrMerge(j, i, isAuto = true)
                    return
                }
            }
        }
    }

    // --- Condition check loop (achievements, unlocks) ---

    private fun startConditionCheckLoop() {
        viewModelScope.launch {
            while (true) {
                delay(500L)
                achievementManager.checkAll(state, this@GameViewModel) { unlockedAchievement ->
                    popupQueue.add(achievementPopup(unlockedAchievement, nextPopupId++))
                }
                upgradeManager.checkUnlocks(this@GameViewModel)
            }
        }
    }

    // --- Upgrades ---

    val maxButtonUnlocked: Boolean
        get() = upgradeManager.getLevel(SpecialityLogic.maxButtonUpgrade.id) >= 1

    fun buyMaxMainUpgrades() {
        if (!maxButtonUnlocked) return
        var boughtAny: Boolean
        do {
            boughtAny = false
            if (upgradeManager.canUpgrade(GameLogic.spawnTierUpgrade, currencyManager)) {
                buyUpgrade(GameLogic.spawnTierUpgrade)
                boughtAny = true
            }
            if (upgradeManager.canUpgrade(GameLogic.spawnRateUpgrade, currencyManager)) {
                buyUpgrade(GameLogic.spawnRateUpgrade)
                boughtAny = true
            }
        } while (boughtAny)
    }

    fun buyUpgrade(definition: UpgradeDefinition) {
        val success = upgradeManager.purchaseUpgrade(definition, currencyManager)
        if (success) {
            definition.onPurchase?.invoke(this)
            saveGame()
        }
    }

    fun upgradeGridItemsToTier(newTier: Int) {
        for (i in _gridItems.indices) {
            val item = _gridItems[i]
            if (item != null && item.tier < newTier) {
                _gridItems[i] = item.copy(tier = newTier)
            }
        }
    }

    // --- Prestige ---

    fun calculatePrestigeGain(): BigNumber = Gains.prestigeGain()

    val canPrestige: Boolean
        get() {
            if (totalPrestiges > 0) return true
            return _gridItems.any { it != null && it.tier >= 20 }
        }

    fun performPrestige() {
        if (!canPrestige) return
        val gain = calculatePrestigeGain()
        if (gain.compareTo(BigNumber.ZERO) > 0) {
            currencyManager.add(Currencies.PRESTIGE, gain)
            totalPrestiges = totalPrestiges + 1
            currencyManager.resetForPrestige()
            upgradeManager.setLevel(GameLogic.spawnTierUpgrade.id, 0)
            upgradeManager.setLevel(GameLogic.spawnRateUpgrade.id, 0)
            for (i in _gridItems.indices) {
                _gridItems[i] = null
            }
            _gridItems[7] = GridItem(nextItemId++, 1)
            _gridItems[12] = GridItem(nextItemId++, 1)
            setFlag("has_prestiged")
            saveGame()
        }
    }

    // --- Save / Load ---

    private fun startAutoSaveLoop() {
        viewModelScope.launch {
            while (true) {
                delay(10000L)
                saveGame()
            }
        }
    }

    fun saveGame() {
        val editor = prefs.edit()

        for (c in Currencies.ALL) {
            val b = currencyManager.getBalance(c.id)
            editor.putString("currency_${c.id}_m", b.mantissa.toString())
            editor.putInt("currency_${c.id}_e", b.exponent)
        }

        for ((key, value) in stats.all()) {
            editor.putLong("stat_$key", value)
        }
        editor.putLong("playtime_seconds", playtimeSeconds)
        editor.putBoolean("auto_merger_enabled", autoMergerEnabled)
        editor.putBoolean("simple_boxes_enabled", simpleBoxesEnabled)

        editor.putInt("player_level", playerLevel)
        editor.putLong("leveling_merges", levelingMerges)
        editor.putFloat("level_reset_prestige_boost", levelResetPrestigeBoost.toFloat())
        editor.putInt("level_resets", levelResets)

        for ((id, level) in upgradeManager.getAllLevels()) {
            editor.putInt("upgrade_level_$id", level)
        }

        editor.putStringSet("flags", flags)
        editor.putStringSet("achievements", achievementManager.getAllUnlockedIds())

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
        for (c in Currencies.ALL) {
            val m = prefs.getString("currency_${c.id}_m", "0.0")!!.toDouble()
            val e = prefs.getInt("currency_${c.id}_e", 0)
            currencyManager.setBalance(c.id, BigNumber.of(m, e))
        }

        val allPrefs: Map<String, *> = prefs.getAll()
        val statsMap = mutableMapOf<String, Long>()
        for (key in allPrefs.keys) {
            if (key.startsWith("stat_")) {
                statsMap[key.removePrefix("stat_")] = prefs.getLong(key, 0L)
            }
        }
        stats.restore(statsMap)

        playtimeSeconds = prefs.getLong("playtime_seconds", 0L)
        stats.set("playtime_seconds", playtimeSeconds)
        autoMergerEnabled = prefs.getBoolean("auto_merger_enabled", false)
        simpleBoxesEnabled = prefs.getBoolean("simple_boxes_enabled", false)

        playerLevel = prefs.getInt("player_level", 0)
        levelingMerges = prefs.getLong("leveling_merges", 0L)
        levelResetPrestigeBoost = prefs.getFloat("level_reset_prestige_boost", 0f).toDouble()
        levelResets = prefs.getInt("level_resets", 0)
        stats.set("player_level", playerLevel.toLong())

        val levels = mutableMapOf<String, Int>()
        for (key in allPrefs.keys) {
            if (key.startsWith("upgrade_level_")) {
                val id = key.removePrefix("upgrade_level_")
                val lvl = prefs.getInt(key, 0)
                levels[id] = lvl
            }
        }
        upgradeManager.restore(levels)

        val savedFlags = prefs.getStringSet("flags", emptySet()) ?: emptySet()
        _flags.clear()
        _flags.addAll(savedFlags)

        val unlocked = prefs.getStringSet("achievements", emptySet()) ?: emptySet()
        achievementManager.restore(unlocked)
        achievementManager.replayUnlocks(this)

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

        val anyItemPresent = (0 until 20).any { _gridItems[it] != null }
        if (!anyItemPresent) {
            _gridItems[7] = GridItem(nextItemId++, upgradeManager.spawnTierLevel)
            _gridItems[12] = GridItem(nextItemId++, upgradeManager.spawnTierLevel)
        }
    }

    fun hardReset() {
        prefs.edit().clear().apply()
        currencyManager.resetAll()
        upgradeManager.reset()
        achievementManager.reset()
        popupQueue.clear()
        stats.reset()
        _flags.clear()
        playtimeSeconds = 0L
        autoMergerEnabled = false
        simpleBoxesEnabled = false
        playerLevel = 0
        levelingMerges = 0L
        levelResetPrestigeBoost = 0.0
        levelResets = 0
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