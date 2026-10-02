// ui/PageTab.kt
package com.boxmerger.ui

import androidx.compose.ui.graphics.vector.ImageVector
import com.boxmerger.model.Condition
import com.boxmerger.model.Currency

/**
 * A tab in the bottom nav. Rows are defined in order.
 * isVisible controls whether the tab shows at all.
 */
class PageTab(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val iconName: String? = null,
    val tintPng: Boolean = false,
    val pageId: String,
    val row: Int = 0,
    val isVisible: Condition = { true },
    /** Currencies to show in top bar on this page. Empty = use default. */
    val topBarCurrencies: List<Currency> = emptyList()
)

/**
 * Configuration for which currencies to show in the top bar for a given page.
 */
class TopBarConfig(
    val defaultCurrencies: List<Currency>,
    val perPageOverrides: Map<String, List<Currency>> = emptyMap()
) {
    fun currenciesForPage(pageId: String): List<Currency> {
        return perPageOverrides[pageId] ?: defaultCurrencies
    }
}