package com.etrisad.zenith.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.etrisad.zenith.R
import com.etrisad.zenith.data.preferences.UserPreferencesRepository
import com.etrisad.zenith.ui.components.focus.PreferenceCategory
import kotlinx.coroutines.launch
private fun rewardGroupShape(index: Int, total: Int, highlighted: Boolean) = when {
    highlighted || total == 1 -> RoundedCornerShape(24.dp)
    index == 0 -> RoundedCornerShape(
        topStart = 24.dp, bottomStart = 24.dp, topEnd = 8.dp, bottomEnd = 8.dp
    )
    index == total - 1 -> RoundedCornerShape(
        topStart = 8.dp, bottomStart = 8.dp, topEnd = 24.dp, bottomEnd = 24.dp
    )
    else -> RoundedCornerShape(8.dp)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LevelScreen(
    profileViewModel: ProfileViewModel,
    preferencesRepository: UserPreferencesRepository,
    innerPadding: PaddingValues
) {
    val state by profileViewModel.uiState.collectAsState()
    val preferences by preferencesRepository.userPreferencesFlow.collectAsState(
        initial = com.etrisad.zenith.data.preferences.UserPreferences()
    )
    val scope = rememberCoroutineScope()
    val level = state.level

    val equippedTitleId = remember(preferences.userTitle, level) {
        LEVEL_TITLES.find { it.id == preferences.userTitle }
            ?.takeIf { it.requiredLevel <= level }?.id ?: ""
    }
    val equippedBorderId = remember(preferences.userAvatarBorder, level) {
        AVATAR_BORDERS.find { it.id == preferences.userAvatarBorder }
            ?.takeIf { it.requiredLevel <= level }?.id ?: ""
    }
    val equippedShapeDef = remember(preferences.userProfileShape, level) {
        PROFILE_SHAPES.find { it.id == preferences.userProfileShape }
            ?.takeIf { it.requiredLevel <= level } ?: PROFILE_SHAPES.first()
    }
    val equippedShape = equippedShapeDef.shapeResolver()
    val nextReward = remember(level) { nextLevelReward(level) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = innerPadding.calculateTopPadding() + 16.dp,
            bottom = innerPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "overview") {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .avatarRing(
                                    AVATAR_BORDERS.find { it.id == equippedBorderId }, 3.dp,
                                    SolidColor(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    ),
                                    shape = equippedShape
                                )
                                .padding(3.dp)
                                .background(MaterialTheme.colorScheme.primary, equippedShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$level",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.alarm_level_xp_total, state.xpTotal),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black
                            )
                            val nextRewardText = if (nextReward != null) {
                                stringResource(
                                    R.string.alarm_level_next_reward,
                                    nextReward.label(),
                                    nextReward.requiredLevel
                                )
                            } else {
                                stringResource(R.string.alarm_level_all_rewards_unlocked)
                            }
                            Text(
                                text = nextRewardText,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearWavyProgressIndicator(
                        progress = { state.levelProgress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        stroke = Stroke(
                            width = with(LocalDensity.current) { 4.dp.toPx() },
                            cap = StrokeCap.Round
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(
                            R.string.alarm_level_xp_to_next_level,
                            xpToNextLevel(state.xpTotal),
                            level + 1
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item(key = "titles") {
            Column {
                PreferenceCategory(title = stringResource(R.string.alarm_level_titles))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(LEVEL_TITLES, key = { _, t -> "title-${t.id}" }) { index, title ->
                            val unlocked = level >= title.requiredLevel
                            val equipped = equippedTitleId == title.id && unlocked
                            Card(
                                onClick = {
                                    scope.launch {
                                        preferencesRepository.setUserTitle(
                                            if (equipped) "" else title.id
                                        )
                                    }
                                },
                                enabled = unlocked,
                                shape = rewardGroupShape(index, LEVEL_TITLES.size, unlocked),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (unlocked) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    },
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                modifier = Modifier.width(124.dp).aspectRatio(0.78f)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (unlocked) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.WorkspacePremium,
                                            contentDescription = null,
                                            tint = if (unlocked) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(title.nameRes),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = if (unlocked) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = stringResource(R.string.alarm_level_lv, title.requiredLevel),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (unlocked) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                    Box(
                                        modifier = Modifier.height(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (equipped) {
                                            Icon(
                                                imageVector = Icons.Outlined.CheckCircleOutline,
                                                contentDescription = stringResource(R.string.alarm_level_equipped),
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else if (!unlocked) {
                                            Icon(
                                                imageVector = Icons.Outlined.Lock,
                                                contentDescription = stringResource(R.string.alarm_level_locked),
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item(key = "borders") {
            Column {
                PreferenceCategory(title = stringResource(R.string.alarm_level_avatar_borders))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(AVATAR_BORDERS, key = { _, b -> "border-${b.id}" }) { index, border ->
                            val unlocked = level >= border.requiredLevel
                            val equipped = equippedBorderId == border.id && unlocked
                            Card(
                                onClick = {
                                    scope.launch {
                                        preferencesRepository.setUserAvatarBorder(
                                            if (equipped) "" else border.id
                                        )
                                    }
                                },
                                enabled = unlocked,
                                shape = rewardGroupShape(index, AVATAR_BORDERS.size, unlocked),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (unlocked) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    },
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                modifier = Modifier.width(124.dp).aspectRatio(0.78f)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .then(
                                                if (unlocked) Modifier.avatarRing(
                                                    border, 4.dp,
                                                    SolidColor(
                                                        MaterialTheme.colorScheme.surfaceContainerHighest
                                                    ),
                                                    animate = false,
                                                    shape = equippedShape
                                                )
                                                else Modifier.border(
                                                    4.dp,
                                                    SolidColor(
                                                        MaterialTheme.colorScheme.surfaceContainerHighest
                                                    ),
                                                    equippedShape
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!unlocked) {
                                            Icon(
                                                imageVector = Icons.Outlined.Lock,
                                                contentDescription = stringResource(R.string.alarm_level_locked),
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(border.nameRes),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = if (unlocked) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    val borderColorCount = border.colors.size
                                    Text(
                                        text = if (borderColorCount > 1) {
                                            stringResource(
                                                R.string.alarm_level_border_level_colors,
                                                border.requiredLevel,
                                                borderColorCount
                                            )
                                        } else {
                                            stringResource(
                                                R.string.alarm_level_border_level_color,
                                                border.requiredLevel,
                                                borderColorCount
                                            )
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (unlocked) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Box(
                                        modifier = Modifier.height(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (equipped) {
                                            Icon(
                                                imageVector = Icons.Outlined.CheckCircleOutline,
                                                contentDescription = stringResource(R.string.alarm_level_equipped),
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item(key = "shapes") {
            Column {
                PreferenceCategory(title = stringResource(R.string.alarm_level_profile_shapes))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(PROFILE_SHAPES, key = { _, s -> "shape-${s.id}" }) { index, pShapeDef ->
                            val unlocked = level >= pShapeDef.requiredLevel
                            val equipped = equippedShapeDef.id == pShapeDef.id && unlocked
                            val pShape = pShapeDef.shapeResolver()
                            Card(
                                onClick = {
                                    scope.launch {
                                        preferencesRepository.setUserProfileShape(
                                            if (equipped) "" else pShapeDef.id
                                        )
                                    }
                                },
                                enabled = unlocked,
                                shape = rewardGroupShape(index, PROFILE_SHAPES.size, unlocked),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (unlocked) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    },
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                ),
                                modifier = Modifier.width(124.dp).aspectRatio(0.78f)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(pShape)
                                            .background(
                                                if (unlocked) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!unlocked) {
                                            Icon(
                                                imageVector = Icons.Outlined.Lock,
                                                contentDescription = stringResource(R.string.alarm_level_locked),
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(pShape)
                                                    .background(MaterialTheme.colorScheme.onPrimary)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(pShapeDef.nameRes),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = if (unlocked) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = stringResource(R.string.alarm_level_lv, pShapeDef.requiredLevel),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (unlocked) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                    Box(
                                        modifier = Modifier.height(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (equipped) {
                                            Icon(
                                                imageVector = Icons.Outlined.CheckCircleOutline,
                                                contentDescription = stringResource(R.string.alarm_level_equipped),
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item(key = "history") {
            Column {
                PreferenceCategory(title = stringResource(R.string.alarm_level_daily_xp))
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = stringResource(
                                R.string.alarm_level_saved_total,
                                formatCompactDuration(state.totalSavedMillis)
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (state.xpHistory.isEmpty()) {
                            Text(
                                text = stringResource(R.string.alarm_level_no_xp_yet),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                state.xpHistory.forEach { day ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = prettyProfileDate(day.date),
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = stringResource(R.string.alarm_level_xp_gained, day.xp),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.alarm_level_xp_explanation),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
