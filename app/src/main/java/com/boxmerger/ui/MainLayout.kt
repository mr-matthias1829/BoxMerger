// ui/MainLayout.kt
package com.boxmerger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.boxmerger.GameViewModel
import com.boxmerger.model.Currency
import com.boxmerger.model.Currencies

/**
 * Define your tabs here. isVisible controls show/hide.
 * topBarCurrencies controls which currencies show on this page.
 */
object TabConfig {
    val TABS = listOf(
        PageTab(
            id = "game",
            label = "Game",
            icon = Icons.Default.Home,
            iconName = "currencies/icon_boxes.png",
            pageId = "game",
            row = 0,
            topBarCurrencies = listOf(Currencies.BOXES, Currencies.GEM)
        ),
        PageTab(
            id = "gem_upgrades",
            label = "Gem Upgrades",
            icon = Icons.Default.Diamond,
            iconName = "currencies/icon_gem.png",
            pageId = "gem_upgrades",
            row = 0,
            topBarCurrencies = listOf(Currencies.BOXES, Currencies.GEM)
        ),
        PageTab(
            id = "leveling",
            label = "Leveling",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            iconName = "icons/icon_level.png",
            pageId = "leveling",
            row = 0,
            isVisible = { state -> state.hasFlag("leveling_unlocked") },
            topBarCurrencies = listOf(Currencies.BOXES, Currencies.GEM)
        ),
        PageTab(
            id = "prestige",
            label = "Prestige",
            icon = Icons.Default.Bolt,
            iconName = "currencies/icon_prestige.png",
            pageId = "prestige",
            row = 0,
            isVisible = { state -> state.hasFlag("prestige_unlocked") || state.stat("total_prestiges") > 0 },
            topBarCurrencies = listOf(Currencies.PRESTIGE)
        ),
        PageTab(
            id = "achievements",
            label = "Achievements",
            icon = Icons.Default.EmojiEvents,
            iconName = "tab_achievements",
            pageId = "achievements",
            row = 1
        ),
        PageTab(
            id = "stats",
            label = "Stats",
            icon = Icons.Default.BarChart,
            iconName = "tab_stats",
            pageId = "stats",
            row = 1
        ),
        PageTab(
            id = "settings",
            label = "Settings",
            icon = Icons.Default.Settings,
            iconName = "tab_settings",
            pageId = "settings",
            row = 1
        )
    )

    val DEFAULT_TOP_BAR_CURRENCIES = listOf(
        Currencies.BOXES, Currencies.GEM, Currencies.PRESTIGE
    )
}

@Composable
fun MainLayout(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val state = viewModel.state

    // Filter visible tabs
    val visibleTabs = TabConfig.TABS.filter { it.isVisible(state) }

    // Clamp selected index
    val safeIndex = selectedTabIndex.coerceIn(0, maxOf(0, visibleTabs.size - 1))
    val currentTab = visibleTabs.getOrNull(safeIndex)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Dynamic top bar
            val topBarCurrencies = currentTab?.topBarCurrencies
                ?.takeIf { it.isNotEmpty() }
                ?: TabConfig.DEFAULT_TOP_BAR_CURRENCIES

            TopCurrencyBar(
                viewModel = viewModel,
                currencies = topBarCurrencies
            )

            // Main content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentTab?.pageId) {
                    "game" -> GameScreen(viewModel = viewModel)
                    "prestige" -> PrestigeScreen(viewModel = viewModel)
                    "gem_upgrades" -> GemUpgradesScreen(viewModel = viewModel)
                    "leveling" -> LevelingScreen(viewModel = viewModel)
                    "achievements" -> AchievementsScreen(viewModel = viewModel)
                    "stats" -> StatsScreen(viewModel = viewModel)
                    "settings" -> SettingsScreen(viewModel = viewModel)
                    else -> PlaceholderScreen(title = "Unknown Page")
                }
            }

            // Bottom nav
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                BottomNav(
                    tabs = visibleTabs,
                    selectedIndex = safeIndex,
                    onSelect = { selectedTabIndex = it }
                )
            }
        }

        // Unified popup overlay — shows achievements, level ups, and anything
        // else you add to viewModel.popupQueue
        PopupOverlay(
            queue = viewModel.popupQueue,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(10f)
        )
    }
}

@Composable
fun TopCurrencyBar(
    viewModel: GameViewModel,
    currencies: List<Currency>
) {
    Surface(
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (currency in currencies) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconResolver.CurrencyIcon(
                        currency = currency,
                        size = 24.dp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = viewModel.currencyManager.getBalance(currency.id)
                            .toPrettyString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNav(
    tabs: List<PageTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    // Group visible tabs by explicit row index, sorted by row
    val rows = tabs.groupBy { it.row }.entries.sortedBy { it.key }.map { it.value }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (rowTabs in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (tab in rowTabs) {
                    val actualIndex = tabs.indexOf(tab)
                    TabButton(
                        tab = tab,
                        isSelected = selectedIndex == actualIndex,
                        onClick = { onSelect(actualIndex) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TabButton(
    tab: PageTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier,
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            IconResolver.TabIcon(
                tab = tab,
                isSelected = isSelected,
                size = 26.dp,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
    }
}