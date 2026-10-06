// ui/GameScreen.kt
package com.boxmerger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boxmerger.GameViewModel
import com.boxmerger.logic.GameLogic

@Composable
fun GameScreen(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GridView(
            viewModel = viewModel,
            selectedIndex = selectedIndex,
            onSelect = { selectedIndex = it }
        )

        // Auto-Merger toggle
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Upgrades",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (viewModel.maxButtonUnlocked) {
                Button(
                    onClick = { viewModel.buyMaxMainUpgrades() },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Buy Max", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        UpgradeList(
            viewModel = viewModel,
            pageId = GameLogic.PAGE_ID
        )
    }
}

@Composable
fun GridView(
    viewModel: GameViewModel,
    selectedIndex: Int?,
    onSelect: (Int?) -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .size(width = 340.dp, height = 274.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(20) { index ->
                val item = viewModel.gridItems[index]
                val isSelected = selectedIndex == index

                // Does a PNG exist for this tier? (Disabled if Simple Boxes is enabled)
                val hasPng = !viewModel.simpleBoxesEnabled && item != null && IconResolver.assetExists(context, "tiers/t${item.tier}.png")

                // Background: tint only when no PNG is available
                val bgColor = when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    item == null -> MaterialTheme.colorScheme.surface
                    hasPng -> Color.Transparent
                    else -> getTierColor(item.tier)
                }

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgColor)
                        .clickable {
                            val currentSelected = selectedIndex
                            if (currentSelected == null) {
                                if (item != null) onSelect(index)
                            } else {
                                viewModel.moveOrMerge(currentSelected, index)
                                onSelect(null)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (item != null) {
                        GridItemContent(
                            tier = item.tier,
                            fillsCell = hasPng,
                            simpleBoxesEnabled = viewModel.simpleBoxesEnabled
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GridItemContent(
    tier: Int,
    fillsCell: Boolean,
    simpleBoxesEnabled: Boolean
) {
    if (simpleBoxesEnabled) {
        Text(
            text = "T$tier",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        return
    }

    val iconSize = if (fillsCell) 52.dp else 36.dp

    IconResolver.GridItemIcon(
        tier = tier,
        size = iconSize,
        contentScale = ContentScale.Fit,
        fallback = {
            Text(
                text = "T$tier",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    )
}