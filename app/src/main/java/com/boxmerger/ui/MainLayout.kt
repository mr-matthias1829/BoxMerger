// ui/MainLayout.kt
package com.boxmerger.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
            id = "hubscreen",
            label = "Hub",
            icon = Icons.Default.AutoAwesome,
            iconName = "tab_speciality",          // unchanged so the icon asset still resolves
            pageId = "hubscreen",
            row = 0,
            // the hub also opens for Energy (2h playtime), in case that comes before 3k merges
            isVisible = { state -> state.hasFlag("speciality_unlocked") || state.hasFlag("energy_unlocked") },
            topBarCurrencies = listOf(Currencies.BOXES, Currencies.GEM)
        ),
        PageTab(
            id = "speciality_upgrades",
            label = "Speciality Upgrades",
            icon = Icons.Default.AutoAwesome,
            iconName = "tab_speciality",
            pageId = "speciality_upgrades",
            row = 0,                              // ignored, it has a parent
            parentId = "hubscreen",
            description = "Unique upgrades that'll prove quite useful",
            isVisible = { state -> state.hasFlag("speciality_unlocked") },
            topBarCurrencies = listOf(Currencies.BOXES, Currencies.GEM)
        ),
        PageTab(
            id = "energy",
            label = "Energy",
            icon = Icons.Default.BatteryChargingFull,
            iconName = "tab_energy",
            pageId = "energy",
            row = 0,                              // ignored, it has a parent
            parentId = "hubscreen",
            description = "Generate energy over time",
            isVisible = { state -> state.hasFlag("energy_unlocked") },
            topBarCurrencies = listOf(Currencies.ENERGY, Currencies.PRESTIGE)
        ),
        PageTab(
            id = "steel",
            label = "Steel",
            icon = Icons.Default.Build,
            iconName = "tab_steel",
            pageId = "steel",
            row = 0,                              // ignored, it has a parent
            parentId = "hubscreen",
            description = "Forge steel from manual merges",
            isVisible = { state -> state.hasFlag("steel_unlocked") },
            topBarCurrencies = listOf(Currencies.STEEL)
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
    var selectedTabId by rememberSaveable { mutableStateOf("game") }
    val state = viewModel.state

    val allTabs = TabConfig.TABS.filter { it.isVisible(state) }
    val navTabs = allTabs.filter { it.parentId == null }          // only these get a button
    val currentTab = allTabs.firstOrNull { it.id == selectedTabId } ?: navTabs.firstOrNull()

    // Optional: system back button returns from a sub-page to its hub (no on-screen arrow)
    BackHandler(enabled = currentTab?.parentId != null) {
        selectedTabId = currentTab!!.parentId!!
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Steel / Energy stay out of the top bar until they are unlocked
            val topBarCurrencies = (currentTab?.topBarCurrencies
                ?.takeIf { it.isNotEmpty() }
                ?: TabConfig.DEFAULT_TOP_BAR_CURRENCIES)
                .filter {
                    (it != Currencies.STEEL || state.hasFlag("steel_unlocked")) &&
                            (it != Currencies.ENERGY || state.hasFlag("energy_unlocked"))
                }

            TopCurrencyBar(viewModel = viewModel, currencies = topBarCurrencies)

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (currentTab?.pageId) {
                    "game" -> GameScreen(viewModel = viewModel)
                    "prestige" -> PrestigeScreen(viewModel = viewModel)
                    "gem_upgrades" -> GemUpgradesScreen(viewModel = viewModel)
                    "leveling" -> LevelingScreen(viewModel = viewModel)
                    "hubscreen" -> HubScreen(
                        pages = allTabs.filter { it.parentId == currentTab.id },
                        onOpen = { selectedTabId = it }
                    )
                    "speciality_upgrades" -> SpecialityUpgradesScreen(viewModel = viewModel)
                    "energy" -> EnergyScreen(viewModel = viewModel)
                    "steel" -> SteelScreen(viewModel = viewModel)
                    "achievements" -> AchievementsScreen(viewModel = viewModel)
                    "stats" -> StatsScreen(viewModel = viewModel)
                    "settings" -> SettingsScreen(viewModel = viewModel)
                    else -> PlaceholderScreen(title = "Unknown Page")
                }
            }

            Surface(tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                BottomNav(
                    tabs = navTabs,
                    selectedId = currentTab?.id,        // a sub-page matches no button, so none is lit
                    onSelect = { selectedTabId = it }
                )
            }
        }

        PopupOverlay(
            queue = viewModel.popupQueue,
            modifier = Modifier.align(Alignment.TopCenter).zIndex(10f)
        )
    }
}

@Composable
fun BottomNav(
    tabs: List<PageTab>,
    selectedId: String?,
    onSelect: (String) -> Unit
) {
    val rows = tabs.groupBy { it.row }.entries.sortedBy { it.key }.map { it.value }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (rowTabs in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (tab in rowTabs) {
                    TabButton(
                        tab = tab,
                        isSelected = selectedId == tab.id,
                        onClick = { onSelect(tab.id) },
                        modifier = Modifier.weight(1f).height(44.dp)
                    )
                }
            }
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
                            .toPrettyString(),
                        style = MaterialTheme.typography.titleMedium
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