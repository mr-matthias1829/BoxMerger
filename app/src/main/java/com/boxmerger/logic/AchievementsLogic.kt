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
            description = "Have 3000 merges in total and unlock speciality upgrades",
            iconName = "ach_stack",
            fallbackEmoji = "🔀",
            condition = { state ->
                state.stat("total_merges") >= 3000
            },
            onUnlock = { vm -> vm.setFlag("speciality_unlocked") }
        ),
        Achievement(
            visibleAfter = "3000_merges",
            id = "10e3_merges",
            name = "Can't stop, won't stop!",
            description = "Have 10K (1e4) merges in total",
            iconName = "ach_stack",
            fallbackEmoji = "🔀",
            condition = { state ->
                state.stat("total_merges") >= 10e3
            }
        ),
        Achievement(
            visibleAfter = "10e3_merges",
            id = "50e3_merges",
            name = "Nowhere near done",
            description = "Have 50K (5e4) merges in total, you'll probably let the auto-merger do all the hard work from here on out",
            iconName = "ach_stack",
            fallbackEmoji = "🔀",
            condition = { state ->
                state.stat("total_merges") >= 50e3
            }
        ),
        Achievement(
            visibleAfter = "50e3_merges",
            id = "250e3_merges",
            name = "Certainly not stopping now",
            description = "Have 250K (2.5e5) merges in total",
            iconName = "ach_stack",
            fallbackEmoji = "🔀",
            condition = { state ->
                state.stat("total_merges") >= 250e3
            }
        ),
        Achievement(
            visibleAfter = "250e3_merges",
            id = "1e6_merges",
            name = "Still not done",
            description = "Have 1M (1e6) merges in total",
            iconName = "ach_stack",
            fallbackEmoji = "🔀",
            condition = { state ->
                state.stat("total_merges") >= 1e6
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
            name = "You're a star!",
            description = "Reach Tier 100",
            iconName = "t100",
            fallbackEmoji = "100",
            condition = { state ->
                state.stat("highest_tier") >= 100
            }
        ),
        Achievement(
            visibleAfter = "tier_100",
            id = "tier_200",
            name = "We support you all the way",
            description = "Reach tier 200",
            iconName = "t200",
            fallbackEmoji = "200",
            condition = { state ->
                state.stat("highest_tier") >= 200
            }
        ),
        Achievement(
            visibleAfter = "tier_200",
            id = "tier_300",
            name = "The whole spectrum",
            description = "Reach tier 300",
            iconName = "t300",
            fallbackEmoji = "300",
            condition = { state ->
                state.stat("highest_tier") >= 300
            }
        ),
        Achievement(
            visibleAfter = "tier_300",
            id = "tier_400",
            name = "Right on target",
            description = "Reach tier 400",
            iconName = "t400",
            fallbackEmoji = "400",
            condition = { state ->
                state.stat("highest_tier") >= 400
            }
        ),
        Achievement(
            visibleAfter = "tier_400",
            id = "tier_500",
            name = "Pure balance",
            description = "Reach tier 500, you're halfway there!",
            iconName = "t500",
            fallbackEmoji = "500",
            condition = { state ->
                state.stat("highest_tier") >= 500
            }
        ),
        Achievement(
            visibleAfter = "tier_500",
            id = "tier_600",
            name = "Swirling colors",
            description = "Reach tier 600",
            iconName = "t600",
            fallbackEmoji = "600",
            condition = { state ->
                state.stat("highest_tier") >= 600
            }
        ),
        Achievement(
            visibleAfter = "tier_600",
            id = "tier_700",
            name = "Lucky number 7's",
            description = "Reach tier 700",
            iconName = "t700",
            fallbackEmoji = "700",
            condition = { state ->
                state.stat("highest_tier") >= 700
            }
        ),
        Achievement(
            visibleAfter = "tier_700",
            id = "tier_800",
            name = "Smiling through the pain",
            description = "Reach tier 800",
            iconName = "t800",
            fallbackEmoji = "800",
            condition = { state ->
                state.stat("highest_tier") >= 800
            }
        ),
        Achievement(
            visibleAfter = "tier_800",
            id = "tier_900",
            name = "Crowned champion",
            description = "Reach tier 900, almost there!",
            iconName = "t900",
            fallbackEmoji = "900",
            condition = { state ->
                state.stat("highest_tier") >= 900
            }
        ),
        Achievement(
            visibleAfter = "tier_900",
            id = "tier_1000",
            name = "Star of the spectrum",
            description = "Reach tier 1000, congratulations!",
            iconName = "t1000",
            fallbackEmoji = "1000",
            condition = { state ->
                state.stat("highest_tier") >= 1000
            }
        ),
        Achievement(
            visibleAfter = "tier_1001", // shows when its unlocked
            id = "tier_1001",
            name = "There's more?!",
            description = "Witness the tiers looping and achieve star 1 (functionally doesnt work yet)",
            iconName = "ach_temp",
            fallbackEmoji = "1k+1",
            condition = { state ->
                state.stat("highest_tier") >= 1001
            }
        ),
        Achievement(
            visibleAfter = "tier_1001",
            id = "tier_1000",
            name = "It'll never end",
            description = "Reach tier star 2",
            iconName = "ach_temp",
            fallbackEmoji = "1k",
            condition = { state ->
                state.stat("highest_tier") >= 2001
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
            name = "It's over x9 boxes!",
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
            visibleAfter = "prestige_1e6",
            id = "prestige_100e9",
            name = "Billionaire",
            description = "Hold onto over 100B (1e11) prestige",
            iconName = "ach_prestige_1e6",
            fallbackEmoji = "P",
            condition = { state ->
                state.currency("prestige").compareTo(BigNumber.of(100e9)) >= 0
            }
        ),
        Achievement(
            visibleAfter = "prestige_100e9",
            id = "prestige_10e15",
            name = "Prestige to last a lifetime",
            description = "Hold onto over 10Qa (1e16) prestige",
            iconName = "ach_prestige_1e6",
            fallbackEmoji = "P",
            condition = { state ->
                state.currency("prestige").compareTo(BigNumber.of(10e15)) >= 0
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
        Achievement(
            visibleAfter = "3000_merges",
            id = "buy_buy_max",
            name = "My fingers no longer hurt!",
            description = "Unlock the 'Buy Max' button",
            iconName = "ach_temp",
            fallbackEmoji = "🤖",
            condition = { state -> state.upgradeLevel("spec_max_button") >= 1 }
        ),
    )
}