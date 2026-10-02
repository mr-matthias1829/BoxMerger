package com.boxmerger.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boxmerger.logic.LevelingLogic
import com.boxmerger.model.Achievement
import com.boxmerger.model.BigNumber
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/**
 * Data class representing one popup to show. Create instances for any event
 * you want to notify the user about: level up, achievement unlocked, etc.
 */
data class PopupEvent(
    val id: Long,
    val title: String,
    val headline: String,
    val subtitle: String,
    val durationMs: Long,
    val icon: @Composable (size: Dp) -> Unit,
    val accentColor: Color? = null
)

/**
 * Generic popup queue overlay. Add PopupEvents to the queue, they'll be shown
 * one at a time with a slide-in-from-top animation, tap to dismiss, auto-dismiss
 * after `durationMs`.
 */
@Composable
fun PopupOverlay(
    queue: SnapshotStateList<PopupEvent>,
    modifier: Modifier = Modifier
) {
    var currentPopup by remember { mutableStateOf<PopupEvent?>(null) }
    var isVisible by remember { mutableStateOf(false) }
    var isDismissed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            if (queue.isNotEmpty()) {
                val popup = queue.first()
                currentPopup = popup
                isDismissed = false
                isVisible = true

                val startTime = System.currentTimeMillis()
                while (System.currentTimeMillis() - startTime < popup.durationMs && !isDismissed) {
                    delay(50L)
                }

                isVisible = false
                delay(400L)

                if (queue.isNotEmpty() && queue.first() == popup) {
                    queue.removeAt(0)
                }
                currentPopup = null
                delay(150L)
            } else {
                snapshotFlow { queue.size }.first { it > 0 }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = isVisible && currentPopup != null,
            enter = slideInVertically(
                initialOffsetY = { -it - 200 },
                animationSpec = tween(durationMillis = 400)
            ) + fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = slideOutVertically(
                targetOffsetY = { -it - 200 },
                animationSpec = tween(durationMillis = 400)
            ) + fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            currentPopup?.let { popup ->
                val borderColor = popup.accentColor ?: MaterialTheme.colorScheme.primary

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    border = BorderStroke(1.5.dp, borderColor.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clickable { isDismissed = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        popup.icon(44.dp)

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = popup.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = borderColor,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = popup.headline,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (popup.subtitle.isNotEmpty()) {
                                Text(
                                    text = popup.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}









/**
 * Builds a PopupEvent for an achievement unlock.
 */
fun achievementPopup(achievement: Achievement, id: Long): PopupEvent {
    return PopupEvent(
        id = id,
        title = "ACHIEVEMENT UNLOCKED!",
        headline = achievement.name,
        subtitle = achievement.description,
        durationMs = 6000L,
        accentColor = null,
        icon = { size ->
            IconResolver.AchievementIcon(
                achievement = achievement,
                isUnlocked = true,
                size = size,
                fallback = {
                    androidx.compose.material3.Text(
                        text = achievement.fallbackEmoji,
                        fontSize = (size.value * 0.7f).sp
                    )
                }
            )
        }
    )
}

/**
 * Builds a PopupEvent for a level-up.
 */
fun levelUpPopup(level: Int, gemsAwarded: BigNumber, id: Long): PopupEvent {
    val blockBoostPct = ((LevelingLogic.blockEarningsMultiplier(level) - 1.0) * 100).toInt()
    val gemText = formatRewardAmount(gemsAwarded)

    return PopupEvent(
        id = id,
        title = "LEVEL UP!",
        headline = "You are now level $level!",
        subtitle = "+$gemText Gems | +$blockBoostPct% Block Boost",
        durationMs = 3000L,
        accentColor = null,
        icon = { size ->
            IconResolver.NamedIcon(
                name = "icon_level",
                fallbackEmoji = "📈",
                size = size
            )
        }
    )
}

/**
 * Formats a reward BigNumber for display. Small values show up to 1 decimal;
 * big values go to engineering notation.
 */
private fun formatRewardAmount(amount: BigNumber): String {
    val asDouble = amount.toDouble()
    return when {
        asDouble <= 0.0 -> "0"
        asDouble < 1000.0 && asDouble == asDouble.toLong().toDouble() -> asDouble.toLong().toString()
        asDouble < 1000.0 -> String.format(java.util.Locale.US, "%.1f", asDouble)
        else -> amount.toPrettyString()
    }
}