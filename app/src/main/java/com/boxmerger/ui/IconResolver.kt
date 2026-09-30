package com.boxmerger.ui

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.boxmerger.model.Achievement
import com.boxmerger.model.Currency

object IconResolver {

    /**
     * Cache: asset path -> Bitmap or null.
     * Once we know an asset is missing, we remember that too, so we don't
     * repeatedly hit the filesystem on every recomposition.
     */
    private val bitmapCache = mutableMapOf<String, androidx.compose.ui.graphics.ImageBitmap?>()

    private fun loadAssetBitmap(context: Context, path: String): androidx.compose.ui.graphics.ImageBitmap? {
        // Cache hit?
        if (bitmapCache.containsKey(path)) return bitmapCache[path]

        val result = try {
            context.assets.open(path).use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        } catch (e: Exception) {
            null
        }

        bitmapCache[path] = result
        return result
    }

    @Composable
    fun CurrencyIcon(
        currency: Currency,
        size: Dp,
        tint: Color? = null,
        modifier: Modifier = Modifier
    ) {
        val context = LocalContext.current
        val bitmap = remember(currency.iconName) {
            loadAssetBitmap(context, "currencies/${currency.iconName}.png")
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = currency.displayName,
                modifier = modifier.size(size),
                contentScale = ContentScale.Fit,
                colorFilter = tint?.let { ColorFilter.tint(it) }
            )
        } else {
            Text(
                text = currency.fallbackEmoji,
                fontSize = (size.value * 0.8f).sp,
                modifier = modifier
            )
        }
    }

    @Composable
    fun GridItemIcon(
        tier: Int,
        size: Dp,
        contentScale: ContentScale = ContentScale.Fit,
        fallback: @Composable () -> Unit
    ) {
        val context = LocalContext.current
        val bitmap = remember(tier) {
            loadAssetBitmap(context, "tiers/t$tier.png")
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Tier $tier",
                modifier = Modifier.size(size),
                contentScale = contentScale
            )
        } else {
            fallback()
        }
    }

    @Composable
    fun AchievementIcon(
        achievement: Achievement,
        isUnlocked: Boolean,
        size: Dp,
        fallback: @Composable () -> Unit
    ) {
        val context = LocalContext.current
        val bitmap = remember(achievement.iconName) {
            loadAssetBitmap(context, "achievements/${achievement.iconName}.png")
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = achievement.name,
                modifier = Modifier.size(size),
                contentScale = ContentScale.Fit,
                alpha = if (isUnlocked) 1f else 0.4f
            )
        } else {
            fallback()
        }
    }

    /**
     * Check whether an asset exists without loading it, for UI decisions like
     * "should this cell have a transparent background because a PNG will cover it?"
     */
    private val assetExistsCache = mutableMapOf<String, Boolean>()

    fun assetExists(context: Context, path: String): Boolean {
        assetExistsCache[path]?.let { return it }
        val result = try {
            context.assets.open(path).use { true }
        } catch (e: Exception) {
            false
        }
        assetExistsCache[path] = result
        return result
    }
}