// logic/AchievementsLogic.kt
package com.boxmerger.logic

import com.boxmerger.model.*
import com.boxmerger.GameViewModel

object AchievementsLogic {
    val ALL: List<Achievement> = listOf(
        Achievement(
            id = "first_merge",
            name = "So it begins",
            description = "Perform your first merge.",
            iconName = "ach_first_merge",
            fallbackEmoji = "🔀",
            condition = { state -> state.stat("total_merges") >= 1 }
        ),
        Achievement(
            id = "tier_5",
            name = "Stepping stone",
            description = "Reach Tier 5",
            iconName = "ach_tier_5",
            fallbackEmoji = "5️⃣",
            condition = { state ->
                state.stat("highest_tier") >= 5
            }
        ),
        Achievement(
            id = "first_gem",
            name = "Shiny!",
            description = "Get your first gem",
            iconName = "ach_gem",
            fallbackEmoji = "G",
            condition = { state ->
                state.currency("gem").compareTo(BigNumber.ZERO) > 0
            }
        ),
        Achievement(
            id = "tier_20",
            name = "Milestone",
            description = "Reach Tier 20 and unlock prestige",
            iconName = "ach_tier_20",
            fallbackEmoji = "20",
            condition = { state ->
                state.stat("highest_tier") >= 20
            },
            onUnlock = { vm ->
                vm.setFlag("prestige_unlocked")
            }
        ),
        Achievement(
            id = "first_prestige",
            name = "And back we go",
            description = "Perform your first prestige and unlock the auto merger",
            iconName = "ach_first_prestige",
            fallbackEmoji = "⚡",
            condition = { state -> state.stat("total_prestiges") >= 1 },
            onUnlock = { vm -> vm.setFlag("auto_merger_unlocked") }
        ),
        Achievement(
            id = "prestige_900",
            name = "It's over x9 blocks!",
            description = "Hold onto over 900 prestige",
            iconName = "ach_prestige_900",
            fallbackEmoji = "P",
            condition = { state ->
                state.currency("prestige").compareTo(BigNumber.of(900.0)) >= 0
            }
        ),
        Achievement(
            id = "3000_merges",
            name = "Merger 3000",
            description = "Have 3000 merges in total",
            iconName = "ach_stack",
            fallbackEmoji = "🔀",
            condition = { state ->
                state.stat("manual_merges") >= 3000
            }
        ),
        Achievement(
            id = "gem_25",
            name = "Very shiny!",
            description = "Hold onto 25 gems",
            iconName = "ach_gem",
            fallbackEmoji = "G",
            condition = { state ->
                state.currency("gem").compareTo(BigNumber.of(25.0)) >= 0
            }
        ),
        Achievement(
            id = "age_of_machine",
            name = "Age of the machine",
            description = "Have more automatic merges than manual merges",
            iconName = "ach_age_of_machine",
            fallbackEmoji = "🤖",
            condition = { state ->
                state.stat("manual_merges") < state.stat("auto_merges")
            }
        ),
        Achievement(
            id = "tier_50",
            name = "Barrel von tonne",
            description = "Reach Tier 50",
            iconName = "ach_tier_50",
            fallbackEmoji = "50",
            condition = { state ->
                state.stat("highest_tier") >= 50
            }
        ),
        Achievement(
            id = "tier_100",
            name = "How many tiers are there?",
            description = "Reach Tier 100",
            iconName = "ach_tier_100",
            fallbackEmoji = "100",
            condition = { state ->
                state.stat("highest_tier") >= 100
            }
        ),
        Achievement(
            id = "tier_250",
            name = "A whole lotta tiers",
            description = "Reach tier 250",
            iconName = "ach_tier_250",
            fallbackEmoji = "250",
            condition = { state ->
                state.stat("highest_tier") >= 250
            }
        ),
    )
}