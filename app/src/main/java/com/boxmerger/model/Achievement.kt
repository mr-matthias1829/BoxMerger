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
    val iconName: String,
    val fallbackEmoji: String,
    val condition: (GameState) -> Boolean,
    /** Optional: hidden until this achievement id is unlocked. */
    val visibleAfter: String? = null,
    /** Optional: hidden until this returns true. Combined with visibleAfter (both must pass). */
    val visibleWhen: ((GameState) -> Boolean)? = null,
    val onUnlock: ((GameViewModel) -> Unit)? = null
) {
    val hasVisibilityRule: Boolean get() = visibleAfter != null || visibleWhen != null
}

class AchievementManager {
    private val achievements = mutableMapOf<String, Achievement>()
    private val _unlocked = mutableStateMapOf<String, Boolean>()
    private val _visible = mutableStateMapOf<String, Boolean>()

    fun register(achievement: Achievement) {
        achievements[achievement.id] = achievement
        if (!_unlocked.containsKey(achievement.id)) {
            _unlocked[achievement.id] = false
        }
        if (!_visible.containsKey(achievement.id)) {
            _visible[achievement.id] = !achievement.hasVisibilityRule
        }
    }

    fun registerAll(list: List<Achievement>) {
        for (a in list) register(a)
    }

    fun isUnlocked(id: String): Boolean = _unlocked[id] ?: false

    /** Unlocked achievements are always visible, regardless of their visibility rule. */
    fun isVisible(id: String): Boolean = isUnlocked(id) || (_visible[id] ?: false)

    fun getAll(): List<Achievement> = achievements.values.toList()

    fun getVisible(): List<Achievement> = achievements.values.filter { isVisible(it.id) }

    fun getUnlocked(): List<Achievement> = achievements.values.filter { isUnlocked(it.id) }

    fun checkAll(
        state: GameState,
        viewModel: GameViewModel,
        onNewUnlock: ((Achievement) -> Unit)? = null
    ) {
        for (a in achievements.values) {
            if (isUnlocked(a.id)) continue
            if (a.condition(state)) {
                _unlocked[a.id] = true
                a.onUnlock?.invoke(viewModel)
                onNewUnlock?.invoke(a)
            }
        }
        // After the unlock pass, so achievements gated on one that just unlocked appear the same tick
        updateVisibility(state)
    }

    private fun updateVisibility(state: GameState) {
        for (a in achievements.values) {
            if (!a.hasVisibilityRule) continue
            val visible = (a.visibleAfter == null || isUnlocked(a.visibleAfter)) &&
                    (a.visibleWhen?.invoke(state) ?: true)
            if (_visible[a.id] != visible) _visible[a.id] = visible
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
        for (a in achievements.values) {
            _visible[a.id] = !a.hasVisibilityRule
        }
    }

    fun replayUnlocks(viewModel: GameViewModel) {
        for ((id, isUnl) in _unlocked) {
            if (isUnl) {
                achievements[id]?.onUnlock?.invoke(viewModel)
            }
        }
    }
}