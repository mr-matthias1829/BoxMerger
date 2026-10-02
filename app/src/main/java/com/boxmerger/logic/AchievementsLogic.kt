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
            visibleAfter = "first_merge",
            id = "3000_merges",
            name = "Merger 3000",
            description = "Have 3000 merges in total",
            iconName = "ach_stack",
            fallbackEmoji = "🔀",
            condition = { state ->
                state.stat("total_merges") >= 3000
            }
        ),
        Achievement(
            visibleAfter = "first_merge",
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
            visibleAfter = "tier_5",
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
            visibleAfter = "tier_20",
            id = "tier_50",
            name = "Barrel von tonne",
            description = "Reach Tier 50 and unlock levels",
            iconName = "ach_tier_50",
            fallbackEmoji = "50",
            condition = { state ->
                state.stat("highest_tier") >= 50
            },
            onUnlock = { vm ->
                vm.setFlag("leveling_unlocked")
            }
        ),
        Achievement(
            visibleAfter = "tier_50",
            id = "tier_100",
            name = "Triple digit tier",
            description = "Reach Tier 100",
            iconName = "ach_temp",
            fallbackEmoji = "100",
            condition = { state ->
                state.stat("highest_tier") >= 100
            }
        ),
        Achievement(
            visibleAfter = "tier_100",
            id = "tier_200",
            name = "A whole lotta tiers",
            description = "Reach tier 200",
            iconName = "ach_temp",
            fallbackEmoji = "200",
            condition = { state ->
                state.stat("highest_tier") >= 200
            }
        ),
        Achievement(
            visibleAfter = "tier_200",
            id = "tier_300",
            name = "There's still more to go",
            description = "Reach tier 300",
            iconName = "ach_temp",
            fallbackEmoji = "300",
            condition = { state ->
                state.stat("highest_tier") >= 300
            }
        ),
        Achievement(
            visibleAfter = "tier_300",
            id = "tier_400",
            name = "Are we there yet?",
            description = "Reach tier 400",
            iconName = "ach_temp",
            fallbackEmoji = "400",
            condition = { state ->
                state.stat("highest_tier") >= 400
            }
        ),
        Achievement(
            visibleAfter = "tier_400",
            id = "tier_500",
            name = "That's all folks!",
            description = "Reach tier 500",
            iconName = "ach_temp",
            fallbackEmoji = "500",
            condition = { state ->
                state.stat("highest_tier") >= 500
            }
        ),
        Achievement(
            visibleAfter = "tier_501", // shows when its unlocked
            id = "tier_501",
            name = "But wait, there's more!",
            description = "Witness the tiers looping and achieve star 1",
            iconName = "ach_temp",
            fallbackEmoji = "501",
            condition = { state ->
                state.stat("highest_tier") >= 501
            }
        ),
        Achievement(
            visibleAfter = "tier_501",
            id = "tier_1000",
            name = "It'll never end",
            description = "Reach tier star 2",
            iconName = "ach_temp",
            fallbackEmoji = "1k",
            condition = { state ->
                state.stat("highest_tier") >= 1000
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
            visibleAfter = "first_gem",
            id = "gem_50",
            name = "Very shiny!",
            description = "Hold onto 50 gems",
            iconName = "ach_gem",
            fallbackEmoji = "G",
            condition = { state ->
                state.currency("gem").compareTo(BigNumber.of(50.0)) >= 0
            }
        ),
        Achievement(
            visibleAfter = "gem_50",
            id = "gem_1e3",
            name = "Super shiny!",
            description = "Hold onto 1K (1e3) gems",
            iconName = "ach_gem",
            fallbackEmoji = "G",
            condition = { state ->
                state.currency("gem").compareTo(BigNumber.of(1e3)) >= 0
            }
        ),
        Achievement(
            visibleAfter = "gem_1e3",
            id = "gem_50e6",
            name = "Ultra shiny!",
            description = "Hold onto 50M (50e6) gems",
            iconName = "ach_gem",
            fallbackEmoji = "G",
            condition = { state ->
                state.currency("gem").compareTo(BigNumber.of(50e6)) >= 0
            }
        ),
        Achievement(
            visibleAfter = "gem_50e6",
            id = "gem_1e12",
            name = "Blinding shining light",
            description = "Hold onto 1T (1e12) gems",
            iconName = "ach_gem",
            fallbackEmoji = "G",
            condition = { state ->
                state.currency("gem").compareTo(BigNumber.of(1e12)) >= 0
            }
        ),
        Achievement(
            visibleAfter = "tier_20",
            id = "first_prestige",
            name = "And back we go",
            description = "Perform your first prestige and unlock the auto merger",
            iconName = "ach_first_prestige",
            fallbackEmoji = "⚡",
            condition = { state -> state.stat("total_prestiges") >= 1 },
            onUnlock = { vm -> vm.setFlag("auto_merger_unlocked") }
        ),
        Achievement(
            visibleAfter = "first_prestige",
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
            visibleAfter = "prestige_900",
            id = "prestige_1e6",
            name = "Millionaire",
            description = "Hold onto over 1M (1e6) prestige",
            iconName = "ach_prestige_1e6",
            fallbackEmoji = "P",
            condition = { state ->
                state.currency("prestige").compareTo(BigNumber.of(1e6)) >= 0
            }
        ),
        Achievement(
            visibleAfter = "first_prestige",
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
            visibleAfter = "prestige_1e6",
            id = "level_50",
            name = "Gem farming",
            description = "Reach level 50 and unlock Level Reset",
            iconName = "ach_level_50",
            fallbackEmoji = "50",
            condition = { state -> state.stat("player_level") >= 50 },
            onUnlock = { vm -> vm.setFlag("level_reset_unlocked") }
        ),
    )
}