package com.boxmerger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boxmerger.GameViewModel
import com.boxmerger.model.BigNumber
import com.boxmerger.model.CurrencyManager
import com.boxmerger.model.CurrencyType
import com.boxmerger.model.UpgradeDefinition
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

// Upgrade definitions listed right where their UI element is listed
val spawnTierUpgrade = UpgradeDefinition(
    id = "spawn_tier",
    name = "Upgrade Spawn Tier",
    baseCost = mapOf(CurrencyType.BOXES to BigNumber(20e3)),
    scalingFunction = { level -> if (level <= 5) 2.25 else if (level <= 10) 3.0 else if (level <= 19) 4.0 else if (level <= 100) 5.0 else 6.5 },
    effectFormula = { level -> level.toDouble() },
    effectDescription = { level -> "Spawn Tier: $level" }
)

public fun getSpawnRate(level: Int): Double {
    val x = level.toDouble()

    return  6.0 * Math.pow(
        (x + 9.7786) / 10.7786,
        -1.3739
    )
}
val spawnRateUpgrade = UpgradeDefinition(
    id = "spawn_rate",
    name = "Upgrade Spawn Rate",
    baseCost = mapOf(CurrencyType.BOXES to BigNumber(1e3)),
    scalingFunction = { level -> if (level <= 10) 2.2 else if (level <= 20) 3.1 else  4.3},
    effectFormula = { level ->
        getSpawnRate(level)
    },
    effectDescription = { level ->
        val seconds = getSpawnRate(level)

        if (seconds >= 1.0) {
            String.format(Locale.US, "Spawn Rate: %.2fs", seconds)
        } else {
            String.format(Locale.US, "Spawn Rate: %.0fms", seconds * 1000.0)
        }
    }
)

@Composable
fun GameScreen(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.upgradeManager.registerDefinition(spawnTierUpgrade)
        viewModel.upgradeManager.registerDefinition(spawnRateUpgrade)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 5x4 Grid (20 cells)
        Box(
            modifier = Modifier
                .size(width = 340.dp, height = 272.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(20) { index ->
                    val item = viewModel.gridItems[index]
                    val isSelected = selectedIndex == index

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                                    item != null -> getTierColor(item.tier)
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                val currentSelected = selectedIndex
                                if (currentSelected == null) {
                                    if (item != null) {
                                        selectedIndex = index
                                    }
                                } else {
                                    viewModel.moveOrMerge(currentSelected, index)
                                    selectedIndex = null
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (item != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Box Tier ${item.tier}",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "T${item.tier}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Auto-Merger Toggle Button (if unlocked)
        if (viewModel.autoMergerUnlocked) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Auto-Merger: ", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = viewModel.autoMergerEnabled,
                    onCheckedChange = { viewModel.autoMergerEnabled = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Upgrades Section
        Text(
            text = "Upgrades",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tierLevel = viewModel.upgradeManager.getLevel(spawnTierUpgrade.id)
            UpgradeRecordItem(
                definition = spawnTierUpgrade,
                currentLevel = tierLevel,
                effectText = spawnTierUpgrade.effectDescription(tierLevel),
                cost = viewModel.upgradeManager.getCurrentCost(spawnTierUpgrade),
                currencyManager = viewModel.currencyManager,
                onBuy = { viewModel.buyUpgrade(spawnTierUpgrade) }
            )

            val rateLevel = viewModel.upgradeManager.getLevel(spawnRateUpgrade.id)
            UpgradeRecordItem(
                definition = spawnRateUpgrade,
                currentLevel = rateLevel,
                effectText = spawnRateUpgrade.effectDescription(rateLevel),
                cost = viewModel.upgradeManager.getCurrentCost(spawnRateUpgrade),
                currencyManager = viewModel.currencyManager,
                onBuy = { viewModel.buyUpgrade(spawnRateUpgrade) }
            )
        }
    }
}

@Composable
fun UpgradeRecordItem(
    definition: UpgradeDefinition,
    currentLevel: Int,
    effectText: String,
    cost: Map<CurrencyType, BigNumber>,
    currencyManager: CurrencyManager,
    onBuy: () -> Unit
) {
    val canAfford = currencyManager.hasEnough(cost)
    val name = "${definition.name} (Lvl $currentLevel)"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBuy,
                enabled = canAfford,
                modifier = Modifier.size(52.dp),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Buy Upgrade",
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = effectText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "| Cost:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    cost.forEach { (type, amount) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (type) {
                                    CurrencyType.BOXES -> Icons.Default.Star
                                    CurrencyType.GEM -> Icons.Default.Favorite
                                    CurrencyType.PRESTIGE -> Icons.Default.Bolt
                                },
                                contentDescription = type.name,
                                tint = when (type) {
                                    CurrencyType.BOXES -> Color(0xFFFFD700)
                                    CurrencyType.GEM -> Color(0xFFE91E63)
                                    CurrencyType.PRESTIGE -> Color(0xFF00BCD4)
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = amount.toEngineeringString(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class ColorFamily {
    RED,
    ORANGE,
    YELLOW,
    GREEN,
    CYAN,
    BLUE,
    PURPLE,
    PINK,
    BROWN,
    NEUTRAL
}

data class TierColor(
    val color: Color,
    val family: ColorFamily
)

private const val COLOR_SEED = 1928L

private val safeColorPalette = listOf(
    // Reds
    TierColor(Color(0xFFF44336), ColorFamily.RED),
    TierColor(Color(0xFFD32F2F), ColorFamily.RED),
    TierColor(Color(0xFFB71C1C), ColorFamily.RED),

    // Oranges
    TierColor(Color(0xFFFF9800), ColorFamily.ORANGE),
    TierColor(Color(0xFFF57C00), ColorFamily.ORANGE),
    TierColor(Color(0xFFFF6D00), ColorFamily.ORANGE),

    // Yellows / Golds
    TierColor(Color(0xFFC0B233), ColorFamily.YELLOW),
    TierColor(Color(0xFFCB9A09), ColorFamily.YELLOW),
    TierColor(Color(0xFFB79A2B), ColorFamily.YELLOW),

    // Greens
    TierColor(Color(0xFF4CAF50), ColorFamily.GREEN),
    TierColor(Color(0xFF388E3C), ColorFamily.GREEN),
    TierColor(Color(0xFF8BC34A), ColorFamily.GREEN),
    TierColor(Color(0xFF00C853), ColorFamily.GREEN),

    // Teals / Cyans
    TierColor(Color(0xFF009688), ColorFamily.CYAN),
    TierColor(Color(0xFF00BCD4), ColorFamily.CYAN),
    TierColor(Color(0xFF00ACC1), ColorFamily.CYAN),
    TierColor(Color(0xFF13CECE), ColorFamily.CYAN),

    // Blues
    TierColor(Color(0xFF03A9F4), ColorFamily.BLUE),
    TierColor(Color(0xFF2196F3), ColorFamily.BLUE),
    TierColor(Color(0xFF3F51B5), ColorFamily.BLUE),
    TierColor(Color(0xFF1565C0), ColorFamily.BLUE),
    TierColor(Color(0xFF304FFE), ColorFamily.BLUE),

    // Purples
    TierColor(Color(0xFF673AB7), ColorFamily.PURPLE),
    TierColor(Color(0xFF9C27B0), ColorFamily.PURPLE),
    TierColor(Color(0xFF7B1FA2), ColorFamily.PURPLE),
    TierColor(Color(0xFFAA00FF), ColorFamily.PURPLE),

    // Pinks / Magentas
    TierColor(Color(0xFFE91E63), ColorFamily.PINK),
    TierColor(Color(0xFFFF4081), ColorFamily.PINK),
    TierColor(Color(0xFFD500F9), ColorFamily.PINK),

    // Earthy
    TierColor(Color(0xFF795548), ColorFamily.BROWN),
    TierColor(Color(0xFF6D4C41), ColorFamily.BROWN),
    TierColor(Color(0xFF827717), ColorFamily.BROWN),

    // Neutral / special
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
    var ThreeAgoFamily: ColorFamily? = null
    var FourAgoFamily: ColorFamily? = null
    var FiveAgoFamily: ColorFamily? = null

    for (currentTier in 7..tier) {
        val validColors = safeColorPalette.filter {
            it.family != previousFamily &&
                    it.family != twoAgoFamily &&
                    it.family != ThreeAgoFamily &&
                    it.family != FourAgoFamily &&
                    it.family != FiveAgoFamily
        }

        val selected = validColors[random.nextInt(validColors.size)]

        generatedTierColors[currentTier] = selected

        FiveAgoFamily = FourAgoFamily
        FourAgoFamily = ThreeAgoFamily
        ThreeAgoFamily = twoAgoFamily
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
