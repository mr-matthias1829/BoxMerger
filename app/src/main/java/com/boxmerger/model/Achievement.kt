// model/Achievement.kt
package com.boxmerger.model

import com.boxmerger.GameViewModel
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String,        // "ach_<id>" drawable lookup
    val fallbackEmoji: String,
    val condition: (GameState) -> Boolean,
    /** Optional: called once when unlocked. Use to set flags, unlock tabs, etc. */
    val onUnlock: ((GameViewModel) -> Unit)? = null
)

class AchievementManager {
    private val achievements = mutableMapOf<String, Achievement>()
    private val _unlocked = mutableStateMapOf<String, Boolean>()

    fun register(achievement: Achievement) {
        achievements[achievement.id] = achievement
        if (!_unlocked.containsKey(achievement.id)) {
            _unlocked[achievement.id] = false
        }
    }

    fun registerAll(list: List<Achievement>) {
        for (a in list) register(a)
    }

    fun isUnlocked(id: String): Boolean = _unlocked[id] ?: false

    fun getAll(): List<Achievement> = achievements.values.toList()

    fun getUnlocked(): List<Achievement> = achievements.values.filter { isUnlocked(it.id) }

    /** Call this every tick/frame. Fires onUnlock when condition first met. */
    fun checkAll(state: GameState, viewModel: GameViewModel) {
        for (a in achievements.values) {
            if (isUnlocked(a.id)) continue
            if (a.condition(state)) {
                _unlocked[a.id] = true
                a.onUnlock?.invoke(viewModel)
            }
        }
    }

    fun restore(unlockedIds: Set<String>) {
        for (id in unlockedIds) {
            _unlocked[id] = true
        }
    }

    fun getAllUnlockedIds(): Set<String> = _unlocked.filter { it.value }.keys

    fun reset() {
        for (key in _unlocked.keys) {
            _unlocked[key] = false
        }
    }

    /**
     * Fire onUnlock for every already-unlocked achievement.
     * Called after loading a save so flags and other side effects get reapplied.
     * Must be safe to call multiple times (onUnlock hooks should be idempotent).
     */
    fun replayUnlocks(viewModel: GameViewModel) {
        for (a in achievements.values) {
            if (isUnlocked(a.id)) {
                a.onUnlock?.invoke(viewModel)
            }
        }
    }
}