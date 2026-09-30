// ui/MainLayout.kt
package com.boxmerger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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
            pageId = "game",
            topBarCurrencies = listOf(Currencies.BOXES, Currencies.GEM)
        ),
        PageTab(
            id = "prestige",
            label = "Prestige",
            icon = Icons.Default.Bolt,
            pageId = "prestige",
            isVisible = { state -> state.hasFlag("prestige_unlocked") || state.stat("total_prestiges") > 0 },
            topBarCurrencies = listOf(Currencies.PRESTIGE)
        ),
        PageTab(
            id = "gem_upgrades",
            label = "Gem Upgrades",
            icon = Icons.Default.Diamond,
            pageId = "gem_upgrades",
            topBarCurrencies = listOf(Currencies.BOXES, Currencies.GEM )
        ),
        PageTab(
            id = "achievements",
            label = "Achievements",
            icon = Icons.Default.EmojiEvents,
            pageId = "achievements"
        ),
        PageTab(
            id = "stats",
            label = "Stats",
            icon = Icons.Default.BarChart,
            pageId = "stats"
        ),
        PageTab(
            id = "settings",
            label = "Settings",
            icon = Icons.Default.Settings,
            pageId = "settings"
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

    Column(
        modifier = modifier
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
                            .toEngineeringString(),
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
    // Split into rows of 3
    val rows = tabs.chunked(3)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (row in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for ((i, tab) in row.withIndex()) {
                    val actualIndex = tabs.indexOf(tab)
                    IconButtonWithTooltip(
                        icon = tab.icon,
                        label = tab.label,
                        isSelected = selectedIndex == actualIndex,
                        onClick = { onSelect(actualIndex) }
                    )
                }
            }
        }
    }
}

@Composable
fun IconButtonWithTooltip(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier
            .size(width = 110.dp, height = 40.dp)
            .padding(horizontal = 2.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, style = MaterialTheme.typography.bodySmall)
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