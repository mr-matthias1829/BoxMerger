package com.boxmerger.ui

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

enum class ColorFamily {
    RED, ORANGE, YELLOW, GREEN, CYAN, BLUE, PURPLE, PINK, BROWN, NEUTRAL
}

data class TierColor(
    val color: Color,
    val family: ColorFamily
)

private const val COLOR_SEED = 1928L

private val safeColorPalette = listOf(
    TierColor(Color(0xFFF44336), ColorFamily.RED),
    TierColor(Color(0xFFD32F2F), ColorFamily.RED),
    TierColor(Color(0xFFB71C1C), ColorFamily.RED),
    TierColor(Color(0xFFFF9800), ColorFamily.ORANGE),
    TierColor(Color(0xFFF57C00), ColorFamily.ORANGE),
    TierColor(Color(0xFFFF6D00), ColorFamily.ORANGE),
    TierColor(Color(0xFFC0B233), ColorFamily.YELLOW),
    TierColor(Color(0xFFCB9A09), ColorFamily.YELLOW),
    TierColor(Color(0xFFB79A2B), ColorFamily.YELLOW),
    TierColor(Color(0xFF4CAF50), ColorFamily.GREEN),
    TierColor(Color(0xFF388E3C), ColorFamily.GREEN),
    TierColor(Color(0xFF8BC34A), ColorFamily.GREEN),
    TierColor(Color(0xFF00C853), ColorFamily.GREEN),
    TierColor(Color(0xFF009688), ColorFamily.CYAN),
    TierColor(Color(0xFF00BCD4), ColorFamily.CYAN),
    TierColor(Color(0xFF00ACC1), ColorFamily.CYAN),
    TierColor(Color(0xFF13CECE), ColorFamily.CYAN),
    TierColor(Color(0xFF03A9F4), ColorFamily.BLUE),
    TierColor(Color(0xFF2196F3), ColorFamily.BLUE),
    TierColor(Color(0xFF3F51B5), ColorFamily.BLUE),
    TierColor(Color(0xFF1565C0), ColorFamily.BLUE),
    TierColor(Color(0xFF304FFE), ColorFamily.BLUE),
    TierColor(Color(0xFF673AB7), ColorFamily.PURPLE),
    TierColor(Color(0xFF9C27B0), ColorFamily.PURPLE),
    TierColor(Color(0xFF7B1FA2), ColorFamily.PURPLE),
    TierColor(Color(0xFFAA00FF), ColorFamily.PURPLE),
    TierColor(Color(0xFFE91E63), ColorFamily.PINK),
    TierColor(Color(0xFFFF4081), ColorFamily.PINK),
    TierColor(Color(0xFFD500F9), ColorFamily.PINK),
    TierColor(Color(0xFF795548), ColorFamily.BROWN),
    TierColor(Color(0xFF6D4C41), ColorFamily.BROWN),
    TierColor(Color(0xFF827717), ColorFamily.BROWN),
    TierColor(Color(0xFF607D8B), ColorFamily.NEUTRAL),
    TierColor(Color(0xFF455A64), ColorFamily.NEUTRAL),
    TierColor(Color(0xFF000000), ColorFamily.NEUTRAL),
    TierColor(Color(0xFF6B6868), ColorFamily.NEUTRAL)
)

private val generatedTierColors = mutableMapOf<Int, TierColor>()

private fun getGeneratedTierColor(tier: Int): TierColor {
    generatedTierColors[tier]?.let { return it }

    val random = Random(COLOR_SEED)

    var previousFamily: ColorFamily? = null
    var twoAgoFamily: ColorFamily? = null
    var threeAgoFamily: ColorFamily? = null
    var fourAgoFamily: ColorFamily? = null
    var fiveAgoFamily: ColorFamily? = null

    for (currentTier in 7..tier) {
        val validColors = safeColorPalette.filter {
            it.family != previousFamily &&
                    it.family != twoAgoFamily &&
                    it.family != threeAgoFamily &&
                    it.family != fourAgoFamily &&
                    it.family != fiveAgoFamily
        }

        val selected = validColors[random.nextInt(validColors.size)]
        generatedTierColors[currentTier] = selected

        fiveAgoFamily = fourAgoFamily
        fourAgoFamily = threeAgoFamily
        threeAgoFamily = twoAgoFamily
        twoAgoFamily = previousFamily
        previousFamily = selected.family
    }

    return generatedTierColors.getValue(tier)
}

fun getTierColor(tier: Int): Color {
    return when (tier) {
        1 -> Color(0xFF4CAF50)
        2 -> Color(0xFF2196F3)
        3 -> Color(0xFF9C27B0)
        4 -> Color(0xFFFF9800)
        5 -> Color(0xFF00BCD4)
        6 -> Color(0xFFE91E63)
        7 -> Color(0xFF827717)
        8 -> Color(0xFF607D8B)
        9 -> Color(0xFFB71C1C)
        10 -> Color(0xFF6B6868)
        else -> getGeneratedTierColor(tier).color
    }
}