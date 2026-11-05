package com.etrisad.zenith.ui.screens.profile

import androidx.annotation.StringRes
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.material.icons.outlined.Adjust
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.AlarmOn
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.BedtimeOff
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CalendarViewMonth
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.DataArray
import kotlin.math.pow
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.Cyclone
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.FilterVintage
import androidx.compose.material.icons.outlined.Dock
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Hive
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.HourglassFull
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PictureInPicture
import androidx.compose.material.icons.outlined.Nfc
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material.icons.outlined.Token
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.etrisad.zenith.R
const val PROFILE_XP_BASE = 500
const val PROFILE_XP_CURVE = 1.35
enum class ProfileTier(
    val value: Int,
    val icon: ImageVector,
    val title: String,
    @StringRes val titleRes: Int
) {
    I(1, Icons.Outlined.StarOutline, "Spark", R.string.profile_tier_spark),
    V(5, Icons.Outlined.Hive, "Nest", R.string.profile_tier_nest),
    X(10, Icons.Outlined.Token, "Mint", R.string.profile_tier_mint),
    L(50, Icons.Outlined.Cyclone, "Vortex", R.string.profile_tier_vortex),
    C(100, Icons.Outlined.FilterVintage, "Bloom", R.string.profile_tier_bloom),
    D(500, Icons.Outlined.MilitaryTech, "Valor", R.string.profile_tier_valor),
    M(1000, Icons.Outlined.EmojiEvents, "Legend", R.string.profile_tier_legend);

    companion object {
        fun highestAtOrBelow(value: Long): ProfileTier? =
            entries.filter { value >= it.value }.maxByOrNull { it.value }
    }
}
fun toCustomRoman(value: Int): String {
    if (value <= 0) return "-"
    var rest = value
    val parts = mutableListOf<String>()
    for (tier in ProfileTier.entries.sortedByDescending { it.value }) {
        while (rest >= tier.value) {
            parts.add(tier.name)
            rest -= tier.value
        }
    }
    return parts.joinToString(" ")
}
fun tierIconsForLevel(level: Int): List<ImageVector> {
    if (level <= 0) return emptyList()
    var rest = level
    return buildList {
        for (tier in ProfileTier.entries.sortedByDescending { it.value }) {
            while (rest >= tier.value) {
                add(tier.icon)
                rest -= tier.value
            }
        }
    }
}
fun calcShieldXp(limitMillis: Long, usageMillis: Long): Int {
    if (limitMillis <= 0L) return 0
    if (usageMillis > limitMillis) return 0
    val savingsRatio = (limitMillis - usageMillis).toFloat() / limitMillis
    return 10 + (savingsRatio * 90).toInt().coerceIn(0, 90)
}
fun calcGoalXp(targetMillis: Long, usageMillis: Long): Int {
    if (targetMillis <= 0L) return 0
    val ratio = usageMillis.toFloat() / targetMillis
    return if (ratio >= 1f) {
        100 + ((ratio - 1f) * 100).toInt().coerceIn(0, 50)
    } else {
        (ratio * 100).toInt().coerceIn(0, 99)
    }
}
fun xpForLevel(level: Int): Long {
    if (level <= 1) return 0L
    return (PROFILE_XP_BASE * (level - 1).toDouble().pow(PROFILE_XP_CURVE)).toLong()
}

fun levelForXp(xp: Long): Int {
    if (xp < 0L) return 1
    var level = 1
    while (level < 10000 && xp >= xpForLevel(level + 1)) level++
    return level
}

fun levelProgressForXp(xp: Long): Float {
    val level = levelForXp(xp)
    val current = xpForLevel(level)
    val next = xpForLevel(level + 1)
    if (next <= current) return 1f
    return ((xp - current).toFloat() / (next - current)).coerceIn(0f, 1f)
}
fun xpToNextLevel(xpTotal: Long): Long =
    (xpForLevel(levelForXp(xpTotal) + 1) - xpTotal).coerceAtLeast(0L)
data class LevelTitle(
    val id: String,
    val name: String,
    @StringRes val nameRes: Int,
    val requiredLevel: Int
)
enum class BorderEffect { NONE, SPIN, SHINE }

data class AvatarBorder(
    val id: String,
    val name: String,
    @StringRes val nameRes: Int,
    val requiredLevel: Int,
    val colors: List<Color>,
    val effect: BorderEffect = BorderEffect.NONE
)

val LEVEL_TITLES = listOf(
    LevelTitle("initiate", "Initiate", R.string.profile_level_title_initiate, 2),
    LevelTitle("keeper", "Keeper", R.string.profile_level_title_keeper, 5),
    LevelTitle("warden", "Warden", R.string.profile_level_title_warden, 10),
    LevelTitle("sentinel", "Sentinel", R.string.profile_level_title_sentinel, 15),
    LevelTitle("paragon", "Paragon", R.string.profile_level_title_paragon, 20),
    LevelTitle("luminary", "Luminary", R.string.profile_level_title_luminary, 25),
    LevelTitle("zenith", "Zenith", R.string.profile_level_title_zenith, 30),
    LevelTitle("celestial", "Celestial", R.string.profile_level_title_celestial, 35),
    LevelTitle("eternal", "Eternal", R.string.profile_level_title_eternal, 40),
    LevelTitle("mythic", "Mythic", R.string.profile_level_title_mythic, 45),
    LevelTitle("transcendent", "Transcendent", R.string.profile_level_title_transcendent, 50),
    LevelTitle("infinite", "Infinite", R.string.profile_level_title_infinite, 55)
)
val AVATAR_BORDERS = listOf(
    AvatarBorder("ember", "Ember", R.string.profile_border_ember, 3, listOf(Color(0xFFFF7043))),
    AvatarBorder("tide", "Tide", R.string.profile_border_tide, 5, listOf(Color(0xFF29B6F6))),
    AvatarBorder("leaf", "Leaf", R.string.profile_border_leaf, 7, listOf(Color(0xFF66BB6A))),
    AvatarBorder("violet", "Violet", R.string.profile_border_violet, 9, listOf(Color(0xFF7E57C2))),
    AvatarBorder(
        "rose", "Rose", R.string.profile_border_rose, 11,
        listOf(Color(0xFFEC407A)), BorderEffect.SHINE
    ),
    AvatarBorder(
        "moss", "Moss", R.string.profile_border_moss, 14,
        listOf(Color(0xFF9CCC65), Color(0xFF2E7D32))
    ),
    AvatarBorder(
        "gold", "Gold", R.string.profile_border_gold, 17,
        listOf(Color(0xFFFFE082), Color(0xFFFF8F00))
    ),
    AvatarBorder(
        "frost", "Frost", R.string.profile_border_frost, 20,
        listOf(Color(0xFF80DEEA), Color(0xFF00838F))
    ),
    AvatarBorder(
        "magma", "Magma", R.string.profile_border_magma, 23,
        listOf(Color(0xFFFF8A65), Color(0xFFBF360C))
    ),
    AvatarBorder(
        "lagoon", "Lagoon", R.string.profile_border_lagoon, 26,
        listOf(Color(0xFF4DD0E1), Color(0xFF0D47A1)), BorderEffect.SPIN
    ),
    AvatarBorder(
        "dusk", "Dusk", R.string.profile_border_dusk, 29,
        listOf(Color(0xFFCE93D8), Color(0xFF7B1FA2), Color(0xFF311B92))
    ),
    AvatarBorder(
        "sunset", "Sunset", R.string.profile_border_sunset, 32,
        listOf(Color(0xFFFFD54F), Color(0xFFFF7043), Color(0xFFAD1457))
    ),
    AvatarBorder(
        "ocean", "Ocean", R.string.profile_border_ocean, 35,
        listOf(Color(0xFF80DEEA), Color(0xFF1E88E5), Color(0xFF0D47A1))
    ),
    AvatarBorder(
        "forest", "Forest", R.string.profile_border_forest, 38,
        listOf(Color(0xFFAED581), Color(0xFF43A047), Color(0xFF1B5E20))
    ),
    AvatarBorder(
        "candy", "Candy", R.string.profile_border_candy, 41,
        listOf(Color(0xFFF48FB1), Color(0xFFEC407A), Color(0xFF880E4F)),
        BorderEffect.SPIN
    ),
    AvatarBorder(
        "treasure", "Treasure", R.string.profile_border_treasure, 44,
        listOf(
            Color(0xFFFFF176), Color(0xFFFFC107),
            Color(0xFFFF8F00), Color(0xFFE65100)
        )
    ),
    AvatarBorder(
        "aurora", "Aurora", R.string.profile_border_aurora, 47,
        listOf(
            Color(0xFFA7FFEB), Color(0xFF4DD0E1),
            Color(0xFF7B1FA2), Color(0xFF311B92)
        )
    ),
    AvatarBorder(
        "inferno", "Inferno", R.string.profile_border_inferno, 50,
        listOf(
            Color(0xFFFFD180), Color(0xFFFF7043),
            Color(0xFFD84315), Color(0xFF3E2723)
        )
    ),
    AvatarBorder(
        "glacier", "Glacier", R.string.profile_border_glacier, 53,
        listOf(
            Color(0xFFE1F5FE), Color(0xFF80DEEA),
            Color(0xFF00838F), Color(0xFF004D40)
        )
    ),
    AvatarBorder(
        "spring", "Spring", R.string.profile_border_spring, 56,
        listOf(
            Color(0xFFF0F4C3), Color(0xFFAED581),
            Color(0xFF689F38), Color(0xFF33691E)
        ),
        BorderEffect.SPIN
    ),
    AvatarBorder(
        "prism", "Prism", R.string.profile_border_prism, 59,
        listOf(
            Color(0xFFFF8A80), Color(0xFFFFD180), Color(0xFFA7FFEB),
            Color(0xFF82B1FF), Color(0xFFEA80FC)
        )
    ),
    AvatarBorder(
        "spectrum", "Spectrum", R.string.profile_border_spectrum, 62,
        listOf(
            Color(0xFFFF5252), Color(0xFFFFD740), Color(0xFF69F0AE),
            Color(0xFF40C4FF), Color(0xFFE040FB)
        )
    ),
    AvatarBorder(
        "nebula", "Nebula", R.string.profile_border_nebula, 65,
        listOf(
            Color(0xFFEA80FC), Color(0xFF7B1FA2), Color(0xFF29B6F6),
            Color(0xFFA7FFEB), Color(0xFFFFF176)
        )
    ),
    AvatarBorder(
        "carnival", "Carnival", R.string.profile_border_carnival, 68,
        listOf(
            Color(0xFFFF8A80), Color(0xFFFFAB40), Color(0xFFB9F6CA),
            Color(0xFF84FFFF), Color(0xFFB388FF)
        )
    ),
    AvatarBorder(
        "eternity", "Eternity", R.string.profile_border_eternity, 71,
        listOf(
            Color(0xFFD1C4E9), Color(0xFFB39DDB), Color(0xFF90CAF9),
            Color(0xFF80DEEA), Color(0xFFA5D6A7)
        ),
        BorderEffect.SPIN
    )
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
data class ProfileShapeDef(
    val id: String,
    val name: String,
    @StringRes val nameRes: Int,
    val requiredLevel: Int,
    val shapeResolver: @Composable () -> Shape
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val PROFILE_SHAPES = listOf(
    ProfileShapeDef("circle", "Circle", R.string.profile_shape_circle, 1) { CircleShape },
    ProfileShapeDef("square", "Square", R.string.profile_shape_square, 4) {
        MaterialShapes.Square.toShape()
    },
    ProfileShapeDef("pill", "Pill", R.string.profile_shape_pill, 8) { MaterialShapes.Pill.toShape() },
    ProfileShapeDef("pentagon", "Pentagon", R.string.profile_shape_pentagon, 12) {
        MaterialShapes.Pentagon.toShape()
    },
    ProfileShapeDef("gem", "Gem", R.string.profile_shape_gem, 16) { MaterialShapes.Gem.toShape() },
    ProfileShapeDef("cookie7", "Cookie 7", R.string.profile_shape_cookie7, 20) {
        MaterialShapes.Cookie7Sided.toShape()
    },
    ProfileShapeDef("sunny", "Sunny", R.string.profile_shape_sunny, 24) {
        MaterialShapes.Sunny.toShape()
    },
    ProfileShapeDef("slanted", "Slanted", R.string.profile_shape_slanted, 28) {
        MaterialShapes.Slanted.toShape()
    },
    ProfileShapeDef("arch", "Arch", R.string.profile_shape_arch, 32) { MaterialShapes.Arch.toShape() },
    ProfileShapeDef("burst", "Burst", R.string.profile_shape_burst, 36) {
        MaterialShapes.Burst.toShape()
    },
    ProfileShapeDef("puffy", "Puffy", R.string.profile_shape_puffy, 40) {
        MaterialShapes.Puffy.toShape()
    },
    ProfileShapeDef("bun", "Bun", R.string.profile_shape_bun, 44) { MaterialShapes.Bun.toShape() },
    ProfileShapeDef("pixel", "Pixel Circle", R.string.profile_shape_pixel, 48) {
        MaterialShapes.PixelCircle.toShape()
    },
    ProfileShapeDef("flower", "Flower", R.string.profile_shape_flower, 52) {
        MaterialShapes.Flower.toShape()
    },
    ProfileShapeDef("cookie12", "Cookie 12", R.string.profile_shape_cookie12, 58) {
        MaterialShapes.Cookie12Sided.toShape()
    },
    ProfileShapeDef("clover", "Clover 4", R.string.profile_shape_clover, 64) {
        MaterialShapes.Clover4Leaf.toShape()
    },
    ProfileShapeDef("verysunny", "Very Sunny", R.string.profile_shape_verysunny, 70) {
        MaterialShapes.VerySunny.toShape()
    }
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProfileShapeDef.resolveShape(): Shape = shapeResolver()

enum class LevelRewardKind { TITLE, BORDER, SHAPE }

/** One upcoming unlock shown in the "next reward" copy; [nameRes] is the localized name. */
data class LevelReward(
    val requiredLevel: Int,
    @StringRes val nameRes: Int,
    val kind: LevelRewardKind
)

/** Localized label for a reward, e.g. "余烬边框" / "五边形形状". */
@Composable
fun LevelReward.label(): String = when (kind) {
    LevelRewardKind.TITLE -> stringResource(nameRes)
    LevelRewardKind.BORDER ->
        stringResource(R.string.profile_reward_border, stringResource(nameRes))
    LevelRewardKind.SHAPE ->
        stringResource(R.string.profile_reward_shape, stringResource(nameRes))
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun nextLevelReward(level: Int): LevelReward? =
    (LEVEL_TITLES.map { LevelReward(it.requiredLevel, it.nameRes, LevelRewardKind.TITLE) } +
        AVATAR_BORDERS.map { LevelReward(it.requiredLevel, it.nameRes, LevelRewardKind.BORDER) } +
        PROFILE_SHAPES.map { LevelReward(it.requiredLevel, it.nameRes, LevelRewardKind.SHAPE) })
        .filter { it.requiredLevel > level }
        .minByOrNull { it.requiredLevel }

fun borderBrushFor(border: AvatarBorder): Brush =
    if (border.colors.size == 1) SolidColor(border.colors.first())
    else Brush.sweepGradient(border.colors + border.colors.first())

enum class AchievementCategory { EXPLORER, ACCUMULATION }

data class TierThreshold(val tier: ProfileTier, val required: Long, val requireLabel: String)

data class AchievementDef(
    val id: String,
    @StringRes val title: Int,
    @StringRes val desc: Int,
    val icon: ImageVector,
    val category: AchievementCategory,
    val thresholds: List<TierThreshold>,
    val tierNameRes: List<Int> = emptyList(),
    val unlimited: Boolean = false,
    val curveGrowth: Double = 2.0
)

data class AchievementState(
    val def: AchievementDef,
    val current: Long,
    val earnedTier: ProfileTier?,
    val earnedLevel: Int,
    val next: TierThreshold?,
    val progressFraction: Float,
    val unlockedDates: Map<Int, String> = emptyMap()
)

data class ProfileAchievementStats(
    val hasPomodoro: Boolean = false,
    val hasLockdown: Boolean = false,
    val hasBedtime: Boolean = false,
    val hasAlarm: Boolean = false,
    val hasPausePoint: Boolean = false,
    val hasEyeCare: Boolean = false,
    val hasGracePeriod: Boolean = false,
    val hasShield: Boolean = false,
    val hasGoal: Boolean = false,
    val hasSchedule: Boolean = false,
    val hasQr: Boolean = false,
    val hasNfc: Boolean = false,
    val hasPreset: Boolean = false,
    val hasCustomTheme: Boolean = false,
    val hasBackup: Boolean = false,
    val hasMindful: Boolean = false,
    val hasGlimpse: Boolean = false,
    val globalBestStreak: Int = 0,
    val totalSavedMillis: Long = 0L,
    val maxAppBestStreak: Int = 0,
    val pomodoroSessions: Int = 0,
    val pomodoroFocusMillis: Long = 0L,
    val lifetimeMillis: Long = 0L,
    val bedtimeBestStreak: Int = 0,
    val shieldCount: Int = 0,
    val hasOverlayHud: Boolean = false,
    val hasDelayShield: Boolean = false,
    val hasStrictShield: Boolean = false,
    val hasCallerShield: Boolean = false,
    val hasAutoQuitShield: Boolean = false,
    val hasTarget: Boolean = false,
    val hasWebsiteTracking: Boolean = false,
    val isDeveloper: Boolean = false,
    val isCustomPerf: Boolean = false,
    val hasRepair: Boolean = false,
    val hasExcluded: Boolean = false,
    val hasWindDown: Boolean = false,
    val hasSharedProfile: Boolean = false,
    val globalCurrentStreak: Int = 0,
    val emergencyTotal: Int = 0,
    val scheduleCount: Int = 0,
    val alarmCount: Int = 0,
    val goalCount: Int = 0,
    val qrCount: Int = 0,
    val nfcCount: Int = 0,
    val taskTypeCount: Int = 0,
    val userXpTotal: Long = 0L,
    val hasCustomOverlay: Boolean = false,
    val hasFloatingBar: Boolean = false,
    val hasUsagePill: Boolean = false,
    val hasIncentiveLock: Boolean = false,
    val hasEarlyKick: Boolean = false,
    val hasBatteryReset: Boolean = false,
    val hasCustomDayStart: Boolean = false,
    val hasUnusedHours: Boolean = false,
    val hasRecovery: Boolean = false,
    val hasCleaned: Boolean = false,
    val hasBedtimeWhitelist: Boolean = false,
    val hasBedtimeDnd: Boolean = false,
    val hasShorts: Boolean = false,
    val widgetCount: Int = 0,
    val webDomainCount: Int = 0,
    val webTotalMillis: Long = 0L,
    val interceptedCount: Int = 0,
    val trackedDayCount: Int = 0,
    val underBudgetDays: Int = 0,
    val lifetimeAppCount: Int = 0,
    val customVariantCount: Int = 0,
    val infoSheetCount: Int = 0,
    val sevenDayStreak: Int = 0,
    val weekendDays: Int = 0,
    val overdrawDays: Int = 0,
    val distinctDatesCount: Int = 0,
    val nightNights: Int = 0,
    val earlyMornings: Int = 0,
    val profileFields: Int = 0,
    val hasCustomThemeAlt: Boolean = false,
    val fontChanged: Boolean = false,
    val contrastOff: Boolean = false,
    val hasCalendarEvent: Boolean = false,
    val hasOverlayTint: Boolean = false,
    val onboardingChaptersCompleted: Int = 0,
    val onboardingMastered: Boolean = false
)

private fun hoursLabel(millis: Long): String {
    val h = millis / 3_600_000L
    return if (h < 1) "${millis / 60_000L}m" else "${h}h"
}
fun formatCompactDuration(millis: Long): String {
    if (millis <= 0L) return "0m"
    val hours = millis / 3_600_000.0
    if (hours < 1) return "${millis / 60_000L}m"
    val text = "%.2f".format(hours).trimEnd('0').trimEnd('.')
    return "${text}h"
}

private val MILLIS_ACHIEVEMENTS = setOf("focus_hours", "time_saver", "loyal_tracker")

fun formatProgressNumber(defId: String, value: Long): String =
    if (defId in MILLIS_ACHIEVEMENTS) formatCompactDuration(value) else value.toString()
@Composable
fun tierDisplayName(def: AchievementDef, level: Int): String {
    if (level > 0) {
        def.tierNameRes.getOrNull(level - 1)?.let { return stringResource(it) }
        if (isUnlimited(def)) return generatedTierName(def.id, level)
    }
    return stringResource(def.title)
}

/** Non-composable variant for share cards and other background rendering. */
fun tierDisplayName(context: android.content.Context, def: AchievementDef, level: Int): String {
    if (level > 0) {
        def.tierNameRes.getOrNull(level - 1)?.let { return context.getString(it) }
        if (isUnlimited(def)) return generatedTierName(def.id, level)
    }
    return context.getString(def.title)
}
private val CAPPED_ACCUMULATION_IDS = setOf("profile_polisher", "taskmaster", "widget_wielder")

fun isUnlimited(def: AchievementDef): Boolean =
    def.unlimited || (def.category == AchievementCategory.ACCUMULATION && def.id !in CAPPED_ACCUMULATION_IDS)
fun highestDenomForLevel(level: Int): ProfileTier =
    ProfileTier.entries.sortedByDescending { it.value }.firstOrNull { level >= it.value }
        ?: ProfileTier.I
fun niceNumber(v: Double): Long {
    if (!v.isFinite() || v <= 0) return Long.MAX_VALUE
    val cap = Long.MAX_VALUE / 4.0
    if (v >= cap) return Long.MAX_VALUE
    val magnitude = 10.0.pow(kotlin.math.floor(kotlin.math.log10(v)))
    val f = v / magnitude
    val snapped = when {
        f < 1.2 -> 1.0
        f < 1.75 -> 1.5
        f < 2.75 -> 2.0
        f < 3.75 -> 3.0
        f < 6.0 -> 5.0
        f < 8.5 -> 8.0
        else -> 10.0
    }
    return (snapped * magnitude).toLong().coerceAtLeast(1L)
}
fun requiredFor(def: AchievementDef, level: Int): Long {
    if (level < 1) return 1L
    val i = level - 1
    if (i < def.thresholds.size) return def.thresholds[i].required
    if (!isUnlimited(def)) return Long.MAX_VALUE
    val growth = if (def.curveGrowth > 1.0) def.curveGrowth else 2.0
    var v = def.thresholds.lastOrNull()?.required?.toDouble() ?: 1.0
    var k = def.thresholds.size
    val cap = Long.MAX_VALUE / 4.0
    while (k < level) {
        v *= growth
        if (v >= cap) return Long.MAX_VALUE
        k++
    }
    return niceNumber(v)
}
fun thresholdAtOrNull(def: AchievementDef, level: Int): TierThreshold? {
    if (level < 1) return null
    def.thresholds.getOrNull(level - 1)?.let { return it }
    if (!isUnlimited(def)) return null
    val required = requiredFor(def, level)
    if (required == Long.MAX_VALUE) return null
    return TierThreshold(
        tier = highestDenomForLevel(level),
        required = required,
        requireLabel = generatedRequireLabel(def, required)
    )
}
fun tierCountFor(def: AchievementDef, current: Long): Int {
    var level = 0
    while (level < 100000) {
        val req = requiredFor(def, level + 1)
        if (req == Long.MAX_VALUE || current < req) break
        level++
    }
    return level
}
fun nextThresholdFor(def: AchievementDef, current: Long): TierThreshold? =
    thresholdAtOrNull(def, tierCountFor(def, current) + 1)

private fun generatedRequireLabel(def: AchievementDef, required: Long): String {
    val lastLabel = def.thresholds.lastOrNull()?.requireLabel ?: ""
    val suffix = lastLabel.replace(Regex("^\\d[\\d.,]*\\s*"), "")
    return if (def.id in MILLIS_ACHIEVEMENTS) {
        val unit = suffix.replace(Regex("^[a-zA-Z]+"), "").trim()
        val base = formatCompactDuration(required)
        if (unit.isEmpty()) base else "$base $unit"
    } else {
        "$required ${suffix.trim()}".trim()
    }
}
fun historyKeyFor(def: AchievementDef, level: Int): Int {
    if (level < 1) return level
    return def.thresholds.getOrNull(level - 1)?.tier?.value ?: -level
}
fun levelForHistoryKey(def: AchievementDef, key: Int): Int {
    if (key < 0) return -key
    val idx = def.thresholds.indexOfFirst { it.tier.value == key }
    return if (idx >= 0) idx + 1 else -1
}

private val TIER_ADJECTIVES = listOf(
    "Amber", "Arctic", "Astral", "Bouncy", "Brisk", "Cardinal",
    "Celestial", "Cheerful", "Cinder", "Clockwork", "Cosmic", "Crimson",
    "Dapper", "Drowsy", "Electric", "Ember", "Fern", "Fizz",
    "Foggy", "Gallant", "Gilded", "Ginger", "Glacier", "Gleeful",
    "Granite", "Hazel", "Honeyed", "Indigo", "Iron", "Ivory",
    "Jolly", "Juniper", "Lively", "Lunar", "Maple", "Marble",
    "Mellow", "Midnight", "Misty", "Molasses", "Mossy", "Neon",
    "Nimble", "Onyx", "Opal", "Paprika", "Pebble", "Pepper",
    "Quartz", "Ripple", "Roaring", "Ruby", "Saffron", "Sassy",
    "Scarlet", "Solar", "Stellar", "Thunder", "Topaz", "Velvet",
    "Wandering", "Whisper", "Willow", "Zephyr"
)

private val TIER_NOUNS = listOf(
    "Acorn", "Badger", "Beacon", "Bison", "Bluff", "Bonsai",
    "Boulder", "Breeze", "Brook", "Burrow", "Cabin", "Cactus",
    "Canoe", "Canyon", "Carousel", "Castle", "Cedar", "Comet",
    "Compass", "Cougar", "Coyote", "Crane", "Dune", "Eagle",
    "Elm", "Falcon", "Fox", "Galaxy", "Gecko", "Grove",
    "Harbor", "Hawk", "Hedgehog", "Heron", "Hollow", "Ibex",
    "Island", "Jackal", "Jaguar", "Jasper", "Kestrel", "Lagoon",
    "Lark", "Lighthouse", "Lynx", "Mammoth", "Manatee", "Meadow",
    "Mesa", "Meteor", "Mole", "Monarch", "Moose", "Narwhal",
    "Nugget", "Otter", "Owl", "Oxbow", "Panda", "Pine",
    "Pinnacle", "Plover", "Quail", "Raccoon"
)

private val generatedNameOrder = java.util.concurrent.ConcurrentHashMap<String, List<Int>>()
fun generatedTierName(defId: String, level: Int): String {
    val total = TIER_ADJECTIVES.size * TIER_NOUNS.size
    val order = generatedNameOrder.getOrPut(defId) {
        (0 until total).shuffled(kotlin.random.Random(defId.hashCode().toLong()))
    }
    val idx = order[(level - 1) % total]
    return "${TIER_ADJECTIVES[idx / TIER_NOUNS.size]} ${TIER_NOUNS[idx % TIER_NOUNS.size]}"
}
fun countTierSymbols(achievements: List<AchievementState>): Map<ProfileTier, Int> {
    val counts = ProfileTier.entries.associateWith { 0 }.toMutableMap()
    achievements.forEach { state ->
        var rest = state.earnedLevel
        for (tier in ProfileTier.entries.sortedByDescending { it.value }) {
            while (rest >= tier.value) {
                counts[tier] = (counts[tier] ?: 0) + 1
                rest -= tier.value
            }
        }
    }
    return counts
}

fun buildAchievementDefs(): List<AchievementDef> = listOf(
    AchievementDef(
        id = "onboarding_path", title = R.string.ach_onboarding_path_title,
        desc = R.string.ach_onboarding_path_desc,
        icon = Icons.Outlined.Explore, category = AchievementCategory.EXPLORER,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 1, "1 chapter"),
            TierThreshold(ProfileTier.V, 2, "2 chapters"),
            TierThreshold(ProfileTier.X, 3, "3 chapters"),
            TierThreshold(ProfileTier.L, 4, "4 chapters"),
            TierThreshold(ProfileTier.C, 5, "5 chapters"),
            TierThreshold(ProfileTier.D, 6, "6 chapters"),
            TierThreshold(ProfileTier.M, 7, "Mastery")
        ),
        tierNameRes = listOf(R.string.ach_onboarding_path_tier_1, R.string.ach_onboarding_path_tier_2, R.string.ach_onboarding_path_tier_3, R.string.ach_onboarding_path_tier_4, R.string.ach_onboarding_path_tier_5, R.string.ach_onboarding_path_tier_6, R.string.ach_onboarding_path_tier_7)
    ),
    AchievementDef(
        id = "pomo_first", title = R.string.ach_pomo_first_title,
        desc = R.string.ach_pomo_first_desc,
        icon = Icons.Outlined.Timer, category = AchievementCategory.EXPLORER,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 1, "1 session"),
            TierThreshold(ProfileTier.V, 10, "10 sessions"),
            TierThreshold(ProfileTier.X, 50, "50 sessions"),
            TierThreshold(ProfileTier.L, 100, "100 sessions"),
            TierThreshold(ProfileTier.C, 250, "250 sessions"),
            TierThreshold(ProfileTier.D, 500, "500 sessions"),
            TierThreshold(ProfileTier.M, 1000, "1000 sessions")
        ),
        tierNameRes = listOf(R.string.ach_pomo_first_tier_1, R.string.ach_pomo_first_tier_2, R.string.ach_pomo_first_tier_3, R.string.ach_pomo_first_tier_4, R.string.ach_pomo_first_tier_5, R.string.ach_pomo_first_tier_6, R.string.ach_pomo_first_tier_7)
    ),
    AchievementDef(
        id = "focus_hours", title = R.string.ach_focus_hours_title,
        desc = R.string.ach_focus_hours_desc,
        icon = Icons.Outlined.HourglassEmpty, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 3_600_000L, "1h"),
            TierThreshold(ProfileTier.V, 36_000_000L, "10h"),
            TierThreshold(ProfileTier.X, 90_000_000L, "25h"),
            TierThreshold(ProfileTier.L, 180_000_000L, "50h"),
            TierThreshold(ProfileTier.C, 360_000_000L, "100h"),
            TierThreshold(ProfileTier.D, 900_000_000L, "250h"),
            TierThreshold(ProfileTier.M, 1_800_000_000L, "500h")
        ),
        tierNameRes = listOf(R.string.ach_focus_hours_tier_1, R.string.ach_focus_hours_tier_2, R.string.ach_focus_hours_tier_3, R.string.ach_focus_hours_tier_4, R.string.ach_focus_hours_tier_5, R.string.ach_focus_hours_tier_6, R.string.ach_focus_hours_tier_7)
    ),
    AchievementDef(
        id = "streak_keeper", title = R.string.ach_streak_keeper_title,
        desc = R.string.ach_streak_keeper_desc,
        icon = Icons.Outlined.LocalFireDepartment, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 5, "5 days"),
            TierThreshold(ProfileTier.V, 15, "15 days"),
            TierThreshold(ProfileTier.X, 30, "30 days"),
            TierThreshold(ProfileTier.L, 50, "50 days"),
            TierThreshold(ProfileTier.C, 100, "100 days"),
            TierThreshold(ProfileTier.D, 200, "200 days"),
            TierThreshold(ProfileTier.M, 365, "365 days")
        ),
        tierNameRes = listOf(R.string.ach_streak_keeper_tier_1, R.string.ach_streak_keeper_tier_2, R.string.ach_streak_keeper_tier_3, R.string.ach_streak_keeper_tier_4, R.string.ach_streak_keeper_tier_5, R.string.ach_streak_keeper_tier_6, R.string.ach_streak_keeper_tier_7)
    ),
    AchievementDef(
        id = "time_saver", title = R.string.ach_time_saver_title,
        desc = R.string.ach_time_saver_desc,
        icon = Icons.Outlined.Shield, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 3_600_000L, "1h saved"),
            TierThreshold(ProfileTier.V, 18_000_000L, "5h saved"),
            TierThreshold(ProfileTier.X, 72_000_000L, "20h saved"),
            TierThreshold(ProfileTier.L, 180_000_000L, "50h saved"),
            TierThreshold(ProfileTier.C, 360_000_000L, "100h saved"),
            TierThreshold(ProfileTier.D, 900_000_000L, "250h saved"),
            TierThreshold(ProfileTier.M, 1_800_000_000L, "500h saved")
        ),
        tierNameRes = listOf(R.string.ach_time_saver_tier_1, R.string.ach_time_saver_tier_2, R.string.ach_time_saver_tier_3, R.string.ach_time_saver_tier_4, R.string.ach_time_saver_tier_5, R.string.ach_time_saver_tier_6, R.string.ach_time_saver_tier_7)
    ),
    AchievementDef(
        id = "app_streaker", title = R.string.ach_app_streaker_title,
        desc = R.string.ach_app_streaker_desc,
        icon = Icons.Outlined.TrackChanges, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 5, "5 days"),
            TierThreshold(ProfileTier.V, 15, "15 days"),
            TierThreshold(ProfileTier.X, 30, "30 days"),
            TierThreshold(ProfileTier.L, 50, "50 days"),
            TierThreshold(ProfileTier.C, 100, "100 days"),
            TierThreshold(ProfileTier.D, 200, "200 days"),
            TierThreshold(ProfileTier.M, 365, "365 days")
        ),
        tierNameRes = listOf(R.string.ach_app_streaker_tier_1, R.string.ach_app_streaker_tier_2, R.string.ach_app_streaker_tier_3, R.string.ach_app_streaker_tier_4, R.string.ach_app_streaker_tier_5, R.string.ach_app_streaker_tier_6, R.string.ach_app_streaker_tier_7)
    ),
    AchievementDef(
        id = "lockdown_explorer", title = R.string.ach_lockdown_explorer_title,
        desc = R.string.ach_lockdown_explorer_desc, icon = Icons.Outlined.Lock,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "bedtime_explorer", title = R.string.ach_bedtime_explorer_title,
        desc = R.string.ach_bedtime_explorer_desc, icon = Icons.Outlined.Bedtime,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "alarm_explorer", title = R.string.ach_alarm_explorer_title,
        desc = R.string.ach_alarm_explorer_desc, icon = Icons.Outlined.Alarm,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "1 alarm"))
    ),
    AchievementDef(
        id = "pausepoint_explorer", title = R.string.ach_pausepoint_explorer_title,
        desc = R.string.ach_pausepoint_explorer_desc, icon = Icons.Outlined.PauseCircle,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "goal_setter", title = R.string.ach_goal_setter_title,
        desc = R.string.ach_goal_setter_desc, icon = Icons.Outlined.TrackChanges,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "1 goal"))
    ),
    AchievementDef(
        id = "shield_setter", title = R.string.ach_shield_setter_title,
        desc = R.string.ach_shield_setter_desc, icon = Icons.Outlined.Shield,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "1 shield"))
    ),
    AchievementDef(
        id = "eyecare_explorer", title = R.string.ach_eyecare_explorer_title,
        desc = R.string.ach_eyecare_explorer_desc, icon = Icons.Outlined.LightMode,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "grace_explorer", title = R.string.ach_grace_explorer_title,
        desc = R.string.ach_grace_explorer_desc, icon = Icons.Outlined.HourglassEmpty,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "hud_pilot", title = R.string.ach_hud_pilot_title,
        desc = R.string.ach_hud_pilot_desc, icon = Icons.Outlined.PictureInPicture,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "patient_player", title = R.string.ach_patient_player_title,
        desc = R.string.ach_patient_player_desc, icon = Icons.Outlined.HourglassFull,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "1 delayed app"))
    ),
    AchievementDef(
        id = "strict_enforcer", title = R.string.ach_strict_enforcer_title,
        desc = R.string.ach_strict_enforcer_desc, icon = Icons.Outlined.Gavel,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "caller_tuned", title = R.string.ach_caller_tuned_title,
        desc = R.string.ach_caller_tuned_desc, icon = Icons.Outlined.Call,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "auto_quitter", title = R.string.ach_auto_quitter_title,
        desc = R.string.ach_auto_quitter_desc, icon = Icons.Outlined.ExitToApp,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "target_setter", title = R.string.ach_target_setter_title,
        desc = R.string.ach_target_setter_desc, icon = Icons.Outlined.Adjust,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Target set"))
    ),
    AchievementDef(
        id = "web_watcher", title = R.string.ach_web_watcher_title,
        desc = R.string.ach_web_watcher_desc, icon = Icons.Outlined.Public,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "dev_mode", title = R.string.ach_dev_mode_title,
        desc = R.string.ach_dev_mode_desc, icon = Icons.Outlined.Code,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "perf_tuner", title = R.string.ach_perf_tuner_title,
        desc = R.string.ach_perf_tuner_desc, icon = Icons.Outlined.Tune,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Custom profile"))
    ),
    AchievementDef(
        id = "data_doctor", title = R.string.ach_data_doctor_title,
        desc = R.string.ach_data_doctor_desc, icon = Icons.Outlined.Healing,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "ghost_mode", title = R.string.ach_ghost_mode_title,
        desc = R.string.ach_ghost_mode_desc, icon = Icons.Outlined.VisibilityOff,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "1 excluded app"))
    ),
    AchievementDef(
        id = "wind_downer", title = R.string.ach_wind_downer_title,
        desc = R.string.ach_wind_downer_desc, icon = Icons.Outlined.NightsStay,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "town_crier", title = R.string.ach_town_crier_title,
        desc = R.string.ach_town_crier_desc, icon = Icons.Outlined.Campaign,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Shared once"))
    ),
    AchievementDef(
        id = "hot_streak", title = R.string.ach_hot_streak_title,
        desc = R.string.ach_hot_streak_desc,
        icon = Icons.Outlined.Whatshot, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 3, "3 days"),
            TierThreshold(ProfileTier.V, 7, "7 days"),
            TierThreshold(ProfileTier.X, 14, "14 days"),
            TierThreshold(ProfileTier.L, 30, "30 days"),
            TierThreshold(ProfileTier.C, 60, "60 days"),
            TierThreshold(ProfileTier.D, 100, "100 days"),
            TierThreshold(ProfileTier.M, 200, "200 days")
        ),
        tierNameRes = listOf(R.string.ach_hot_streak_tier_1, R.string.ach_hot_streak_tier_2, R.string.ach_hot_streak_tier_3, R.string.ach_hot_streak_tier_4, R.string.ach_hot_streak_tier_5, R.string.ach_hot_streak_tier_6, R.string.ach_hot_streak_tier_7)
    ),
    AchievementDef(
        id = "escape_artist", title = R.string.ach_escape_artist_title,
        desc = R.string.ach_escape_artist_desc,
        icon = Icons.Outlined.DirectionsRun, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 1, "1 escape"),
            TierThreshold(ProfileTier.V, 3, "3 escapes"),
            TierThreshold(ProfileTier.X, 5, "5 escapes"),
            TierThreshold(ProfileTier.L, 10, "10 escapes"),
            TierThreshold(ProfileTier.C, 20, "20 escapes"),
            TierThreshold(ProfileTier.D, 35, "35 escapes"),
            TierThreshold(ProfileTier.M, 50, "50 escapes")
        ),
        tierNameRes = listOf(R.string.ach_escape_artist_tier_1, R.string.ach_escape_artist_tier_2, R.string.ach_escape_artist_tier_3, R.string.ach_escape_artist_tier_4, R.string.ach_escape_artist_tier_5, R.string.ach_escape_artist_tier_6, R.string.ach_escape_artist_tier_7)
    ),
    AchievementDef(
        id = "scheduler_pro", title = R.string.ach_scheduler_pro_title,
        desc = R.string.ach_scheduler_pro_desc,
        icon = Icons.Outlined.CalendarMonth, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 1, "1 schedule"),
            TierThreshold(ProfileTier.V, 2, "2 schedules"),
            TierThreshold(ProfileTier.X, 3, "3 schedules"),
            TierThreshold(ProfileTier.L, 5, "5 schedules"),
            TierThreshold(ProfileTier.M, 8, "8 schedules")
        ),
        tierNameRes = listOf(R.string.ach_scheduler_pro_tier_1, R.string.ach_scheduler_pro_tier_2, R.string.ach_scheduler_pro_tier_3, R.string.ach_scheduler_pro_tier_4, R.string.ach_scheduler_pro_tier_5)
    ),
    AchievementDef(
        id = "alarm_collector", title = R.string.ach_alarm_collector_title,
        desc = R.string.ach_alarm_collector_desc,
        icon = Icons.Outlined.AlarmOn, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 1, "1 alarm"),
            TierThreshold(ProfileTier.V, 2, "2 alarms"),
            TierThreshold(ProfileTier.X, 3, "3 alarms"),
            TierThreshold(ProfileTier.L, 5, "5 alarms"),
            TierThreshold(ProfileTier.M, 8, "8 alarms")
        ),
        tierNameRes = listOf(R.string.ach_alarm_collector_tier_1, R.string.ach_alarm_collector_tier_2, R.string.ach_alarm_collector_tier_3, R.string.ach_alarm_collector_tier_4, R.string.ach_alarm_collector_tier_5)
    ),
    AchievementDef(
        id = "goal_getter", title = R.string.ach_goal_getter_title,
        desc = R.string.ach_goal_getter_desc,
        icon = Icons.Outlined.Flag, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 1, "1 goal"),
            TierThreshold(ProfileTier.V, 2, "2 goals"),
            TierThreshold(ProfileTier.X, 3, "3 goals"),
            TierThreshold(ProfileTier.L, 5, "5 goals"),
            TierThreshold(ProfileTier.M, 8, "8 goals")
        ),
        tierNameRes = listOf(R.string.ach_goal_getter_tier_1, R.string.ach_goal_getter_tier_2, R.string.ach_goal_getter_tier_3, R.string.ach_goal_getter_tier_4, R.string.ach_goal_getter_tier_5)
    ),
    AchievementDef(
        id = "xp_hoarder", title = R.string.ach_xp_hoarder_title,
        desc = R.string.ach_xp_hoarder_desc,
        icon = Icons.Outlined.Savings, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 500, "500 XP"),
            TierThreshold(ProfileTier.V, 1000, "1000 XP"),
            TierThreshold(ProfileTier.X, 2500, "2500 XP"),
            TierThreshold(ProfileTier.L, 5000, "5000 XP"),
            TierThreshold(ProfileTier.C, 10000, "10000 XP"),
            TierThreshold(ProfileTier.D, 25000, "25000 XP"),
            TierThreshold(ProfileTier.M, 50000, "50000 XP")
        ),
        tierNameRes = listOf(R.string.ach_xp_hoarder_tier_1, R.string.ach_xp_hoarder_tier_2, R.string.ach_xp_hoarder_tier_3, R.string.ach_xp_hoarder_tier_4, R.string.ach_xp_hoarder_tier_5, R.string.ach_xp_hoarder_tier_6, R.string.ach_xp_hoarder_tier_7)
    ),
    AchievementDef(
        id = "taskmaster", title = R.string.ach_taskmaster_title,
        desc = R.string.ach_taskmaster_desc,
        icon = Icons.Outlined.Assignment, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 1, "1 type"),
            TierThreshold(ProfileTier.V, 2, "2 types"),
            TierThreshold(ProfileTier.X, 4, "4 types"),
            TierThreshold(ProfileTier.M, 6, "6 types")
        ),
        tierNameRes = listOf(R.string.ach_taskmaster_tier_1, R.string.ach_taskmaster_tier_2, R.string.ach_taskmaster_tier_3, R.string.ach_taskmaster_tier_4)
    ),
    AchievementDef(
        id = "palette_painter", title = R.string.ach_palette_painter_title,
        desc = R.string.ach_palette_painter_desc, icon = Icons.Outlined.Colorize,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Custom hue"))
    ),
    AchievementDef(
        id = "floater", title = R.string.ach_floater_title,
        desc = R.string.ach_floater_desc, icon = Icons.Outlined.Dock,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "pill_keeper", title = R.string.ach_pill_keeper_title,
        desc = R.string.ach_pill_keeper_desc, icon = Icons.Outlined.Medication,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "locked_in", title = R.string.ach_locked_in_title,
        desc = R.string.ach_locked_in_desc, icon = Icons.Outlined.Lock,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "head_start", title = R.string.ach_head_start_title,
        desc = R.string.ach_head_start_desc, icon = Icons.Outlined.Bolt,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "full_charge", title = R.string.ach_full_charge_title,
        desc = R.string.ach_full_charge_desc, icon = Icons.Outlined.BatteryChargingFull,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "day_maker", title = R.string.ach_day_maker_title,
        desc = R.string.ach_day_maker_desc, icon = Icons.Outlined.WbSunny,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Custom start"))
    ),
    AchievementDef(
        id = "night_off", title = R.string.ach_night_off_title,
        desc = R.string.ach_night_off_desc, icon = Icons.Outlined.BedtimeOff,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "second_chance", title = R.string.ach_second_chance_title,
        desc = R.string.ach_second_chance_desc, icon = Icons.Outlined.Replay,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Recovered once"))
    ),
    AchievementDef(
        id = "cleaner", title = R.string.ach_cleaner_title,
        desc = R.string.ach_cleaner_desc, icon = Icons.Outlined.CleaningServices,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Cleaned once"))
    ),
    AchievementDef(
        id = "bedtime_bouncer", title = R.string.ach_bedtime_bouncer_title,
        desc = R.string.ach_bedtime_bouncer_desc, icon = Icons.Outlined.Hotel,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "1 allowed app"))
    ),
    AchievementDef(
        id = "silent_sleeper", title = R.string.ach_silent_sleeper_title,
        desc = R.string.ach_silent_sleeper_desc, icon = Icons.Outlined.NotificationsOff,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "shorts_spotter", title = R.string.ach_shorts_spotter_title,
        desc = R.string.ach_shorts_spotter_desc, icon = Icons.Outlined.PlayCircle,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Spotted once"))
    ),
    AchievementDef(
        id = "widget_wielder", title = R.string.ach_widget_wielder_title,
        desc = R.string.ach_widget_wielder_desc,
        icon = Icons.Outlined.Widgets, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 1, "1 widget"),
            TierThreshold(ProfileTier.V, 2, "2 widgets"),
            TierThreshold(ProfileTier.X, 3, "3 widgets"),
            TierThreshold(ProfileTier.M, 5, "5 widgets")
        ),
        tierNameRes = listOf(R.string.ach_widget_wielder_tier_1, R.string.ach_widget_wielder_tier_2, R.string.ach_widget_wielder_tier_3, R.string.ach_widget_wielder_tier_4)
    ),
    AchievementDef(
        id = "web_cartographer", title = R.string.ach_web_cartographer_title,
        desc = R.string.ach_web_cartographer_desc,
        icon = Icons.Outlined.Explore, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 3, "3 domains"),
            TierThreshold(ProfileTier.V, 10, "10 domains"),
            TierThreshold(ProfileTier.X, 25, "25 domains"),
            TierThreshold(ProfileTier.M, 50, "50 domains")
        ),
        tierNameRes = listOf(R.string.ach_web_cartographer_tier_1, R.string.ach_web_cartographer_tier_2, R.string.ach_web_cartographer_tier_3, R.string.ach_web_cartographer_tier_4)
    ),
    AchievementDef(
        id = "interceptor", title = R.string.ach_interceptor_title,
        desc = R.string.ach_interceptor_desc,
        icon = Icons.Outlined.NotificationsActive, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 10, "10 caught"),
            TierThreshold(ProfileTier.V, 50, "50 caught"),
            TierThreshold(ProfileTier.X, 200, "200 caught"),
            TierThreshold(ProfileTier.M, 1000, "1000 caught")
        ),
        tierNameRes = listOf(R.string.ach_interceptor_tier_1, R.string.ach_interceptor_tier_2, R.string.ach_interceptor_tier_3, R.string.ach_interceptor_tier_4)
    ),
    AchievementDef(
        id = "historian", title = R.string.ach_historian_title,
        desc = R.string.ach_historian_desc,
        icon = Icons.Outlined.AutoStories, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 7, "7 days"),
            TierThreshold(ProfileTier.V, 30, "30 days"),
            TierThreshold(ProfileTier.X, 90, "90 days"),
            TierThreshold(ProfileTier.L, 180, "180 days"),
            TierThreshold(ProfileTier.M, 365, "365 days")
        ),
        tierNameRes = listOf(R.string.ach_historian_tier_1, R.string.ach_historian_tier_2, R.string.ach_historian_tier_3, R.string.ach_historian_tier_4, R.string.ach_historian_tier_5)
    ),
    AchievementDef(
        id = "app_atlas", title = R.string.ach_app_atlas_title,
        desc = R.string.ach_app_atlas_desc,
        icon = Icons.Outlined.Map, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 10, "10 apps"),
            TierThreshold(ProfileTier.V, 25, "25 apps"),
            TierThreshold(ProfileTier.X, 50, "50 apps"),
            TierThreshold(ProfileTier.L, 100, "100 apps"),
            TierThreshold(ProfileTier.M, 200, "200 apps")
        ),
        tierNameRes = listOf(R.string.ach_app_atlas_tier_1, R.string.ach_app_atlas_tier_2, R.string.ach_app_atlas_tier_3, R.string.ach_app_atlas_tier_4, R.string.ach_app_atlas_tier_5)
    ),
    AchievementDef(
        id = "variant_vanguard", title = R.string.ach_variant_vanguard_title,
        desc = R.string.ach_variant_vanguard_desc,
        icon = Icons.Outlined.DashboardCustomize, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 1, "1 variant"),
            TierThreshold(ProfileTier.V, 2, "2 variants"),
            TierThreshold(ProfileTier.X, 4, "4 variants"),
            TierThreshold(ProfileTier.M, 6, "6 variants")
        ),
        tierNameRes = listOf(R.string.ach_variant_vanguard_tier_1, R.string.ach_variant_vanguard_tier_2, R.string.ach_variant_vanguard_tier_3, R.string.ach_variant_vanguard_tier_4)
    ),
    AchievementDef(
        id = "under_budget", title = R.string.ach_under_budget_title,
        desc = R.string.ach_under_budget_desc,
        icon = Icons.Outlined.Savings, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 5, "5 days"),
            TierThreshold(ProfileTier.V, 10, "10 days"),
            TierThreshold(ProfileTier.X, 15, "15 days"),
            TierThreshold(ProfileTier.L, 20, "20 days"),
            TierThreshold(ProfileTier.M, 25, "25 days")
        ),
        tierNameRes = listOf(R.string.ach_under_budget_tier_1, R.string.ach_under_budget_tier_2, R.string.ach_under_budget_tier_3, R.string.ach_under_budget_tier_4, R.string.ach_under_budget_tier_5)
    ),
    AchievementDef(
        id = "info_hunter", title = R.string.ach_info_hunter_title,
        desc = R.string.ach_info_hunter_desc,
        icon = Icons.Outlined.Explore, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 3, "3 screens"),
            TierThreshold(ProfileTier.V, 8, "8 screens"),
            TierThreshold(ProfileTier.X, 15, "15 screens"),
            TierThreshold(ProfileTier.M, 22, "22 screens")
        ),
        tierNameRes = listOf(R.string.ach_info_hunter_tier_1, R.string.ach_info_hunter_tier_2, R.string.ach_info_hunter_tier_3, R.string.ach_info_hunter_tier_4)
    ),
    AchievementDef(
        id = "night_owl_lite", title = R.string.ach_night_owl_lite_title,
        desc = R.string.ach_night_owl_lite_desc,
        icon = Icons.Outlined.Bedtime, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 3, "3 nights"),
            TierThreshold(ProfileTier.X, 7, "7 nights"),
            TierThreshold(ProfileTier.M, 14, "14 nights")
        ),
        tierNameRes = listOf(R.string.ach_night_owl_lite_tier_1, R.string.ach_night_owl_lite_tier_2, R.string.ach_night_owl_lite_tier_3)
    ),
    AchievementDef(
        id = "early_bird", title = R.string.ach_early_bird_title,
        desc = R.string.ach_early_bird_desc,
        icon = Icons.Outlined.WbSunny, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 3, "3 mornings"),
            TierThreshold(ProfileTier.X, 7, "7 mornings"),
            TierThreshold(ProfileTier.M, 14, "14 mornings")
        ),
        tierNameRes = listOf(R.string.ach_early_bird_tier_1, R.string.ach_early_bird_tier_2, R.string.ach_early_bird_tier_3)
    ),
    AchievementDef(
        id = "weekend_warrior", title = R.string.ach_weekend_warrior_title,
        desc = R.string.ach_weekend_warrior_desc,
        icon = Icons.Outlined.CalendarViewMonth, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 4, "4 weekends"),
            TierThreshold(ProfileTier.X, 8, "8 weekends"),
            TierThreshold(ProfileTier.M, 16, "16 weekends")
        ),
        tierNameRes = listOf(R.string.ach_weekend_warrior_tier_1, R.string.ach_weekend_warrior_tier_2, R.string.ach_weekend_warrior_tier_3)
    ),
    AchievementDef(
        id = "profile_polisher", title = R.string.ach_profile_polisher_title,
        desc = R.string.ach_profile_polisher_desc,
        icon = Icons.Outlined.Badge, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 1, "1 field"),
            TierThreshold(ProfileTier.X, 2, "2 fields"),
            TierThreshold(ProfileTier.M, 3, "3 fields")
        ),
        tierNameRes = listOf(R.string.ach_profile_polisher_tier_1, R.string.ach_profile_polisher_tier_2, R.string.ach_profile_polisher_tier_3)
    ),
    AchievementDef(
        id = "streak_roller", title = R.string.ach_streak_roller_title,
        desc = R.string.ach_streak_roller_desc,
        icon = Icons.Outlined.EventRepeat, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 7, "7 days"),
            TierThreshold(ProfileTier.X, 14, "14 days"),
            TierThreshold(ProfileTier.M, 21, "21 days")
        ),
        tierNameRes = listOf(R.string.ach_streak_roller_tier_1, R.string.ach_streak_roller_tier_2, R.string.ach_streak_roller_tier_3)
    ),
    AchievementDef(
        id = "overachiever", title = R.string.ach_overachiever_title,
        desc = R.string.ach_overachiever_desc,
        icon = Icons.Outlined.LocalFireDepartment, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 1, "1 day"),
            TierThreshold(ProfileTier.X, 3, "3 days"),
            TierThreshold(ProfileTier.M, 7, "7 days")
        ),
        tierNameRes = listOf(R.string.ach_overachiever_tier_1, R.string.ach_overachiever_tier_2, R.string.ach_overachiever_tier_3)
    ),
    AchievementDef(
        id = "midnight_oil", title = R.string.ach_midnight_oil_title,
        desc = R.string.ach_midnight_oil_desc, icon = Icons.Outlined.DarkMode,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Spotted once"))
    ),
    AchievementDef(
        id = "sunrise_club", title = R.string.ach_sunrise_club_title,
        desc = R.string.ach_sunrise_club_desc, icon = Icons.Outlined.LightMode,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Spotted once"))
    ),
    AchievementDef(
        id = "web_surf", title = R.string.ach_web_surf_title,
        desc = R.string.ach_web_surf_desc, icon = Icons.Outlined.Language,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Spotted once"))
    ),
    AchievementDef(
        id = "theme_explorer", title = R.string.ach_theme_explorer_title,
        desc = R.string.ach_theme_explorer_desc, icon = Icons.Outlined.Palette,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Switched once"))
    ),
    AchievementDef(
        id = "font_explorer", title = R.string.ach_font_explorer_title,
        desc = R.string.ach_font_explorer_desc, icon = Icons.Outlined.TextFields,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Switched once"))
    ),
    AchievementDef(
        id = "contrast_seeker", title = R.string.ach_contrast_seeker_title,
        desc = R.string.ach_contrast_seeker_desc, icon = Icons.Outlined.Contrast,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Turned off"))
    ),
    AchievementDef(
        id = "calendar_seeker", title = R.string.ach_calendar_seeker_title,
        desc = R.string.ach_calendar_seeker_desc, icon = Icons.Outlined.DateRange,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Opened once"))
    ),
    AchievementDef(
        id = "overlay_tinter", title = R.string.ach_overlay_tinter_title,
        desc = R.string.ach_overlay_tinter_desc, icon = Icons.Outlined.ColorLens,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Custom once"))
    ),
    AchievementDef(
        id = "schedule_keeper", title = R.string.ach_schedule_keeper_title,
        desc = R.string.ach_schedule_keeper_desc, icon = Icons.Outlined.EventRepeat,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "1 schedule"))
    ),
    AchievementDef(
        id = "qr_collector", title = R.string.ach_qr_collector_title,
        desc = R.string.ach_qr_collector_desc, icon = Icons.Outlined.QrCode2,
        category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 1, "1 code"),
            TierThreshold(ProfileTier.V, 2, "2 codes"),
            TierThreshold(ProfileTier.X, 3, "3 codes"),
            TierThreshold(ProfileTier.M, 5, "5 codes")
        ),
        tierNameRes = listOf(R.string.ach_qr_collector_tier_1, R.string.ach_qr_collector_tier_2, R.string.ach_qr_collector_tier_3, R.string.ach_qr_collector_tier_4)
    ),
    AchievementDef(
        id = "nfc_collector", title = R.string.ach_nfc_collector_title,
        desc = R.string.ach_nfc_collector_desc, icon = Icons.Outlined.Nfc,
        category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 1, "1 tag"),
            TierThreshold(ProfileTier.V, 2, "2 tags"),
            TierThreshold(ProfileTier.X, 3, "3 tags"),
            TierThreshold(ProfileTier.M, 5, "5 tags")
        ),
        tierNameRes = listOf(R.string.ach_nfc_collector_tier_1, R.string.ach_nfc_collector_tier_2, R.string.ach_nfc_collector_tier_3, R.string.ach_nfc_collector_tier_4)
    ),
    AchievementDef(
        id = "preset_saver", title = R.string.ach_preset_saver_title,
        desc = R.string.ach_preset_saver_desc, icon = Icons.Outlined.Save,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "1 preset"))
    ),
    AchievementDef(
        id = "theme_stylist", title = R.string.ach_theme_stylist_title,
        desc = R.string.ach_theme_stylist_desc, icon = Icons.Outlined.Palette,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "backup_guardian", title = R.string.ach_backup_guardian_title,
        desc = R.string.ach_backup_guardian_desc, icon = Icons.Outlined.Backup,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Backed up"))
    ),
    AchievementDef(
        id = "mindful_explorer", title = R.string.ach_mindful_explorer_title,
        desc = R.string.ach_mindful_explorer_desc, icon = Icons.Outlined.SelfImprovement,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "glimpse_user", title = R.string.ach_glimpse_user_title,
        desc = R.string.ach_glimpse_user_desc, icon = Icons.Outlined.Visibility,
        category = AchievementCategory.EXPLORER,
        thresholds = listOf(TierThreshold(ProfileTier.I, 1, "Enabled once"))
    ),
    AchievementDef(
        id = "loyal_tracker", title = R.string.ach_loyal_tracker_title,
        desc = R.string.ach_loyal_tracker_desc,
        icon = Icons.Outlined.History, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 36_000_000L, "10h"),
            TierThreshold(ProfileTier.V, 180_000_000L, "50h"),
            TierThreshold(ProfileTier.X, 360_000_000L, "100h"),
            TierThreshold(ProfileTier.L, 900_000_000L, "250h"),
            TierThreshold(ProfileTier.C, 1_800_000_000L, "500h"),
            TierThreshold(ProfileTier.D, 3_600_000_000L, "1000h"),
            TierThreshold(ProfileTier.M, 7_200_000_000L, "2000h")
        ),
        tierNameRes = listOf(R.string.ach_loyal_tracker_tier_1, R.string.ach_loyal_tracker_tier_2, R.string.ach_loyal_tracker_tier_3, R.string.ach_loyal_tracker_tier_4, R.string.ach_loyal_tracker_tier_5, R.string.ach_loyal_tracker_tier_6, R.string.ach_loyal_tracker_tier_7)
    ),
    AchievementDef(
        id = "night_guardian", title = R.string.ach_night_guardian_title,
        desc = R.string.ach_night_guardian_desc,
        icon = Icons.Outlined.Bedtime, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 7, "7 nights"),
            TierThreshold(ProfileTier.V, 14, "14 nights"),
            TierThreshold(ProfileTier.X, 30, "30 nights"),
            TierThreshold(ProfileTier.L, 60, "60 nights"),
            TierThreshold(ProfileTier.C, 100, "100 nights"),
            TierThreshold(ProfileTier.D, 200, "200 nights"),
            TierThreshold(ProfileTier.M, 365, "365 nights")
        ),
        tierNameRes = listOf(R.string.ach_night_guardian_tier_1, R.string.ach_night_guardian_tier_2, R.string.ach_night_guardian_tier_3, R.string.ach_night_guardian_tier_4, R.string.ach_night_guardian_tier_5, R.string.ach_night_guardian_tier_6, R.string.ach_night_guardian_tier_7)
    ),
    AchievementDef(
        id = "fortress_builder", title = R.string.ach_fortress_builder_title,
        desc = R.string.ach_fortress_builder_desc,
        icon = Icons.Outlined.Security, category = AchievementCategory.ACCUMULATION,
        thresholds = listOf(
            TierThreshold(ProfileTier.I, 1, "1 app"),
            TierThreshold(ProfileTier.V, 3, "3 apps"),
            TierThreshold(ProfileTier.X, 5, "5 apps"),
            TierThreshold(ProfileTier.L, 10, "10 apps"),
            TierThreshold(ProfileTier.C, 15, "15 apps"),
            TierThreshold(ProfileTier.D, 20, "20 apps"),
            TierThreshold(ProfileTier.M, 30, "30 apps")
        ),
        tierNameRes = listOf(R.string.ach_fortress_builder_tier_1, R.string.ach_fortress_builder_tier_2, R.string.ach_fortress_builder_tier_3, R.string.ach_fortress_builder_tier_4, R.string.ach_fortress_builder_tier_5, R.string.ach_fortress_builder_tier_6, R.string.ach_fortress_builder_tier_7)
    )
)
private val JOURNEY_ORDER = listOf(
    "onboarding_path",
    "target_setter", "shield_setter", "goal_setter",
    "lockdown_explorer", "bedtime_explorer", "alarm_explorer",
    "pausepoint_explorer", "grace_explorer",
    "pomo_first", "preset_saver", "schedule_keeper",
    "hot_streak", "streak_keeper", "streak_roller", "under_budget", "overachiever",
    "time_saver", "night_guardian", "night_owl_lite", "early_bird",
    "midnight_oil", "sunrise_club",
    "historian", "loyal_tracker", "app_atlas", "fortress_builder", "goal_getter",
    "weekend_warrior", "web_surf",
    "theme_stylist", "theme_explorer", "font_explorer", "contrast_seeker",
    "palette_painter", "overlay_tinter", "floater", "pill_keeper", "hud_pilot",
    "day_maker", "head_start", "info_hunter", "profile_polisher", "calendar_seeker",
    "patient_player", "strict_enforcer", "caller_tuned", "auto_quitter",
    "locked_in", "ghost_mode", "night_off",
    "mindful_explorer", "glimpse_user", "web_watcher", "full_charge",
    "eyecare_explorer", "wind_downer", "bedtime_bouncer", "silent_sleeper",
    "alarm_collector", "scheduler_pro", "qr_collector", "nfc_collector", "taskmaster", "variant_vanguard",
    "focus_hours", "xp_hoarder",
    "town_crier", "shorts_spotter", "widget_wielder",
    "second_chance", "cleaner", "backup_guardian", "data_doctor",
    "app_streaker", "web_cartographer", "interceptor", "escape_artist",
    "dev_mode", "perf_tuner"
)

fun buildAchievementStates(stats: ProfileAchievementStats): List<AchievementState> {
    val defs = buildAchievementDefs().sortedBy {
        JOURNEY_ORDER.indexOf(it.id).takeIf { i -> i >= 0 } ?: Int.MAX_VALUE
    }
    val currentById = mapOf(
        "onboarding_path" to (
            stats.onboardingChaptersCompleted + if (stats.onboardingMastered) 1 else 0
        ).toLong(),
        "pomo_first" to stats.pomodoroSessions.toLong(),
        "focus_hours" to stats.pomodoroFocusMillis,
        "streak_keeper" to stats.globalBestStreak.toLong(),
        "time_saver" to stats.totalSavedMillis,
        "app_streaker" to stats.maxAppBestStreak.toLong(),
        "lockdown_explorer" to if (stats.hasLockdown) 1L else 0L,
        "bedtime_explorer" to if (stats.hasBedtime) 1L else 0L,
        "alarm_explorer" to if (stats.hasAlarm) 1L else 0L,
        "pausepoint_explorer" to if (stats.hasPausePoint) 1L else 0L,
        "goal_setter" to if (stats.hasGoal) 1L else 0L,
        "shield_setter" to if (stats.hasShield) 1L else 0L,
        "eyecare_explorer" to if (stats.hasEyeCare) 1L else 0L,
        "grace_explorer" to if (stats.hasGracePeriod) 1L else 0L,
        "schedule_keeper" to if (stats.hasSchedule) 1L else 0L,
        "qr_collector" to stats.qrCount.toLong(),
        "nfc_collector" to stats.nfcCount.toLong(),
        "preset_saver" to if (stats.hasPreset) 1L else 0L,
        "hud_pilot" to if (stats.hasOverlayHud) 1L else 0L,
        "patient_player" to if (stats.hasDelayShield) 1L else 0L,
        "strict_enforcer" to if (stats.hasStrictShield) 1L else 0L,
        "caller_tuned" to if (stats.hasCallerShield) 1L else 0L,
        "auto_quitter" to if (stats.hasAutoQuitShield) 1L else 0L,
        "target_setter" to if (stats.hasTarget) 1L else 0L,
        "web_watcher" to if (stats.hasWebsiteTracking) 1L else 0L,
        "dev_mode" to if (stats.isDeveloper) 1L else 0L,
        "perf_tuner" to if (stats.isCustomPerf) 1L else 0L,
        "data_doctor" to if (stats.hasRepair) 1L else 0L,
        "ghost_mode" to if (stats.hasExcluded) 1L else 0L,
        "wind_downer" to if (stats.hasWindDown) 1L else 0L,
        "town_crier" to if (stats.hasSharedProfile) 1L else 0L,
        "hot_streak" to stats.globalCurrentStreak.toLong(),
        "escape_artist" to stats.emergencyTotal.toLong(),
        "scheduler_pro" to stats.scheduleCount.toLong(),
        "alarm_collector" to stats.alarmCount.toLong(),
        "goal_getter" to stats.goalCount.toLong(),
        "xp_hoarder" to stats.userXpTotal,
        "taskmaster" to stats.taskTypeCount.toLong(),
        "theme_explorer" to if (stats.hasCustomThemeAlt) 1L else 0L,
        "font_explorer" to if (stats.fontChanged) 1L else 0L,
        "contrast_seeker" to if (stats.contrastOff) 1L else 0L,
        "calendar_seeker" to if (stats.hasCalendarEvent) 1L else 0L,
        "overlay_tinter" to if (stats.hasOverlayTint) 1L else 0L,
        "info_hunter" to stats.infoSheetCount.toLong(),
        "night_owl_lite" to stats.nightNights.toLong(),
        "early_bird" to stats.earlyMornings.toLong(),
        "weekend_warrior" to stats.weekendDays.toLong(),
        "profile_polisher" to stats.profileFields.toLong(),
        "streak_roller" to stats.sevenDayStreak.toLong(),
        "overachiever" to stats.overdrawDays.toLong(),
        "midnight_oil" to if (stats.nightNights > 0) 1L else 0L,
        "sunrise_club" to if (stats.earlyMornings > 0) 1L else 0L,
        "web_surf" to if (stats.webTotalMillis > 0L) 1L else 0L,
        "palette_painter" to if (stats.hasCustomOverlay) 1L else 0L,
        "floater" to if (stats.hasFloatingBar) 1L else 0L,
        "pill_keeper" to if (stats.hasUsagePill) 1L else 0L,
        "locked_in" to if (stats.hasIncentiveLock) 1L else 0L,
        "head_start" to if (stats.hasEarlyKick) 1L else 0L,
        "full_charge" to if (stats.hasBatteryReset) 1L else 0L,
        "day_maker" to if (stats.hasCustomDayStart) 1L else 0L,
        "night_off" to if (stats.hasUnusedHours) 1L else 0L,
        "second_chance" to if (stats.hasRecovery) 1L else 0L,
        "cleaner" to if (stats.hasCleaned) 1L else 0L,
        "bedtime_bouncer" to if (stats.hasBedtimeWhitelist) 1L else 0L,
        "silent_sleeper" to if (stats.hasBedtimeDnd) 1L else 0L,
        "shorts_spotter" to if (stats.hasShorts) 1L else 0L,
        "widget_wielder" to stats.widgetCount.toLong(),
        "web_cartographer" to stats.webDomainCount.toLong(),
        "interceptor" to stats.interceptedCount.toLong(),
        "historian" to stats.trackedDayCount.toLong(),
        "app_atlas" to stats.lifetimeAppCount.toLong(),
        "variant_vanguard" to stats.customVariantCount.toLong(),
        "under_budget" to stats.underBudgetDays.toLong(),
        "theme_stylist" to if (stats.hasCustomTheme) 1L else 0L,
        "backup_guardian" to if (stats.hasBackup) 1L else 0L,
        "mindful_explorer" to if (stats.hasMindful) 1L else 0L,
        "glimpse_user" to if (stats.hasGlimpse) 1L else 0L,
        "loyal_tracker" to stats.lifetimeMillis,
        "night_guardian" to stats.bedtimeBestStreak.toLong(),
        "fortress_builder" to stats.shieldCount.toLong()
    )
    return defs.map { def ->
        val current = currentById[def.id] ?: 0L
        val earnedLevel = tierCountFor(def, current)
        val earnedTier = thresholdAtOrNull(def, earnedLevel)?.tier
        val next = nextThresholdFor(def, current)
        val progress = when {
            next == null -> 1f
            next.required <= 0L -> 1f
            else -> (current.toFloat() / next.required).coerceIn(0f, 1f)
        }
        AchievementState(
            def = def,
            current = current,
            earnedTier = earnedTier,
            earnedLevel = earnedLevel,
            next = next,
            progressFraction = progress
        )
    }
}

@Composable
fun achievementProgressLabel(state: AchievementState): String {
    val next = state.next ?: return stringResource(R.string.achievement_max_tier, state.earnedTier?.name ?: "")
    return if (state.def.id in MILLIS_ACHIEVEMENTS) {
        "${formatCompactDuration(state.current)} / ${next.requireLabel}"
    } else {
        "${state.current} / ${next.requireLabel}"
    }
}
