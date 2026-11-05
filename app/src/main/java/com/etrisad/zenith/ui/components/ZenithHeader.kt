package com.etrisad.zenith.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.etrisad.zenith.R
import com.etrisad.zenith.ui.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private class SmoothedHeaderValues(
    val alpha: State<Float>,
    val offset: State<Float>
)

@Composable
private fun rememberSmoothedHeaderValues(state: TopAppBarState): SmoothedHeaderValues {
    val alpha = animateFloatAsState(
        targetValue = (1f - state.collapsedFraction).coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "SmoothedHeaderAlpha"
    )
    val offset = animateFloatAsState(
        targetValue = state.heightOffset,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "SmoothedHeaderOffset"
    )
    return SmoothedHeaderValues(alpha, offset)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenithHeader(
    currentRoute: String?,
    scrollBehavior: TopAppBarScrollBehavior,
    isNavRailVisible: Boolean = false,
    userName: String = stringResource(R.string.notifications_default_user_name),
    categoryName: String? = null,
    pausePointTypeName: String? = null,
    onBack: () -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null,
    showInfoButton: Boolean = false,
    onInfoClick: () -> Unit = {},
    infoNextToAction: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val isHome = currentRoute == Screen.Home.route
    val isDeepScreen =
        currentRoute == Screen.UsageStats.route || 
        currentRoute == Screen.Bedtime.route ||
        currentRoute == Screen.GracePeriod.route ||
        currentRoute == Screen.EyeCare.route ||
        currentRoute == Screen.ReelsShield.route ||
        currentRoute == Screen.RedirectShield.route ||
        currentRoute == Screen.Lockdown.route ||
        currentRoute == Screen.TamperProtection.route ||
        currentRoute == Screen.DatabaseDebug.route ||
        currentRoute == Screen.DataRepairment.route ||
        currentRoute == Screen.FontTest.route ||
        currentRoute == Screen.GSFlexCustomizer.route ||
        currentRoute == Screen.SystemUsageDebug.route ||
        currentRoute == Screen.OverlayAppearance.route ||
        currentRoute == Screen.Alarm.route ||
        currentRoute == Screen.Pomodoro.route ||
        currentRoute == Screen.PausePoint.route ||
        currentRoute?.startsWith("pause_point_type") == true ||
        currentRoute?.startsWith("settings_category") == true ||
        currentRoute?.startsWith("app_detail") == true ||
        currentRoute == Screen.Profile.route ||
        currentRoute == Screen.Achievements.route ||
        currentRoute == Screen.Level.route

    val sideSlotWidth = 68.dp

    val smoothed = rememberSmoothedHeaderValues(scrollBehavior.state)
    val smoothedAlpha = smoothed.alpha
    val smoothedOffset = smoothed.offset

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = smoothedAlpha.value }
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        Color.Transparent
                    )
                )
            )
    ) {
        CenterAlignedTopAppBar(
            scrollBehavior = scrollBehavior,
            windowInsets = WindowInsets.statusBars,
            navigationIcon = {},
            title = {},
            actions = {},
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            )
        )

        val titleBias by animateFloatAsState(
            targetValue = if (isDeepScreen) -1f else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "TitleAlignment"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(64.dp)
                .offset { IntOffset(0, smoothedOffset.value.roundToInt()) }
                .graphicsLayer {
                    val alpha = smoothedAlpha.value
                    this.alpha = alpha
                    val scale = 1f - ((1f - alpha) * 0.08f)
                    scaleX = scale
                    scaleY = scale
                }
                .zIndex(1f),
            contentAlignment = BiasAlignment(horizontalBias = titleBias, verticalBias = 0f)
        ) {
            val titleXOffset by animateDpAsState(
                targetValue = if (isDeepScreen) 80.dp else 0.dp,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "TitleOffset"
            )

            val settingsTitle = stringResource(R.string.notifications_settings)

            val title = when {
                isHome -> "home_header"
                currentRoute == Screen.Focus.route -> stringResource(R.string.notifications_focus)
                currentRoute == Screen.Settings.route -> settingsTitle
                currentRoute == Screen.UsageStats.route -> stringResource(R.string.notifications_usage_stats)
                currentRoute == Screen.Bedtime.route -> stringResource(R.string.notifications_bedtime)
                currentRoute == Screen.GracePeriod.route -> stringResource(R.string.notifications_grace_period)
                currentRoute == Screen.EyeCare.route -> stringResource(R.string.notifications_eye_care)
                currentRoute == Screen.ReelsShield.route -> stringResource(R.string.notifications_reels_shield)
                currentRoute == Screen.RedirectShield.route -> stringResource(R.string.notifications_redirect_shield)
                currentRoute == Screen.Lockdown.route -> stringResource(R.string.notifications_lockdown)
                currentRoute == Screen.TamperProtection.route -> stringResource(R.string.notifications_tamper_protection)
                currentRoute == Screen.DatabaseDebug.route -> stringResource(R.string.notifications_database)
                currentRoute == Screen.DataRepairment.route -> stringResource(R.string.notifications_data_repairment)
                currentRoute == Screen.FontTest.route -> stringResource(R.string.notifications_m3_expressive_editor)
                currentRoute == Screen.GSFlexCustomizer.route -> stringResource(R.string.notifications_gs_flex_designer)
                currentRoute == Screen.SystemUsageDebug.route -> stringResource(R.string.notifications_system_usage_fetch)
                currentRoute == Screen.OverlayAppearance.route -> stringResource(R.string.notifications_overlay_appearance)
                currentRoute == Screen.Alarm.route -> stringResource(R.string.notifications_alarm)
                currentRoute == Screen.Pomodoro.route -> stringResource(R.string.notifications_pomodoro)
                currentRoute == Screen.PausePoint.route -> stringResource(R.string.notifications_pause_point)
                currentRoute == Screen.Profile.route -> stringResource(R.string.notifications_profile)
                currentRoute == Screen.Achievements.route -> stringResource(R.string.notifications_achievements)
                currentRoute == Screen.Level.route -> stringResource(R.string.notifications_level)
                currentRoute?.startsWith("pause_point_type") == true ->
                    pausePointTypeName?.ifEmpty { null } ?: stringResource(R.string.notifications_pause_point)
                currentRoute?.startsWith("settings_category") == true -> {
                    val raw = categoryName ?: ""
                    val localizedCategory = when (raw.lowercase()) {
                        "developer" -> stringResource(R.string.notifications_developer)
                        "features" -> stringResource(R.string.notifications_features)
                        "appearance" -> stringResource(R.string.notifications_appearance)
                        "performance" -> stringResource(R.string.notifications_performance)
                        "data management" -> stringResource(R.string.notifications_data_management)
                        else -> raw.replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }
                    }
                    localizedCategory.ifEmpty { settingsTitle }
                }
                currentRoute?.startsWith("app_detail") == true -> stringResource(R.string.notifications_app_detail)
                else -> stringResource(R.string.notifications_app_name)
            }

            AnimatedContent(
                targetState = title,
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 0.92f)).togetherWith(fadeOut() + scaleOut(targetScale = 0.92f))
                },
                label = "HeaderTitleAnimation",
                modifier = Modifier.offset(x = titleXOffset)
            ) { state ->
                if (state == "home_header") {
                    val greetingOptions = listOf(
                        stringResource(R.string.notifications_greeting_welcome_back),
                        stringResource(R.string.notifications_greeting_stay_focused),
                        stringResource(R.string.notifications_greeting_keep_going),
                        stringResource(R.string.notifications_greeting_stay_strong),
                        stringResource(R.string.notifications_greeting_you_got_this)
                    )
                    val greeting = remember { greetingOptions.random() }
                    var stage by remember { mutableStateOf(0) }
                    LaunchedEffect(Unit) {
                        delay(1700)
                        stage = 1
                        delay(1700)
                        stage = 2
                    }

                    AnimatedContent(
                        targetState = stage,
                        transitionSpec = {
                            (fadeIn() + slideInVertically { it / 2 })
                                .togetherWith(fadeOut() + slideOutVertically { -it / 2 })
                        },
                        label = "HomeAppNameAnimation"
                    ) { currentStage ->
                        Text(
                            text = when (currentStage) {
                                0 -> greeting
                                1 -> userName
                                else -> stringResource(R.string.notifications_app_name)
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = if (currentStage == 2) FontWeight.ExtraBold else FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = state,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = if (isDeepScreen) TextAlign.Start else TextAlign.Center,
                            maxLines = 1
                        )
                        if (currentRoute == Screen.ReelsShield.route) {
                            val coroutineScope = rememberCoroutineScope()
                            val tooltipState = rememberTooltipState(isPersistent = true)
                            ZenithTooltipBox(
                                tooltipText = stringResource(R.string.notifications_beta),
                                state = tooltipState,
                                arrowPosition = TooltipArrowPosition.TopCenter
                            ) {
                                Surface(
                                    onClick = {
                                        coroutineScope.launch {
                                            if (tooltipState.isVisible) tooltipState.dismiss() else tooltipState.show()
                                        }
                                    },
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "β",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset { IntOffset(0, smoothedOffset.value.roundToInt()) }
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(64.dp)
                .widthIn(min = sideSlotWidth)
                .graphicsLayer {
                    val alpha = smoothedAlpha.value
                    this.alpha = alpha
                    val scale = 1f - ((1f - alpha) * 0.12f)
                    scaleX = scale
                    scaleY = scale
                }
                .zIndex(2f),
            contentAlignment = Alignment.CenterStart
        ) {
            navigationIcon?.invoke()

            AnimatedVisibility(
                visible = isDeepScreen,
                enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) + 
                        scaleIn(initialScale = 0.92f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)) +
                        slideInHorizontally(initialOffsetX = { -it / 2 }, animationSpec = spring(stiffness = Spring.StiffnessLow)),
                exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)) + 
                       scaleOut(targetScale = 0.92f) +
                       slideOutHorizontally(targetOffsetX = { -it / 2 }, animationSpec = spring(stiffness = Spring.StiffnessLow))
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset { IntOffset(0, smoothedOffset.value.roundToInt()) }
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(64.dp)
                .widthIn(min = sideSlotWidth)
                .graphicsLayer {
                    val alpha = smoothedAlpha.value
                    this.alpha = alpha
                    val scale = 1f - ((1f - alpha) * 0.12f)
                    scaleX = scale
                    scaleY = scale
                }
                .zIndex(3f),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AnimatedVisibility(
                    visible = showInfoButton,
                    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                            scaleIn(
                                initialScale = 0.7f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) +
                            slideInHorizontally(
                                initialOffsetX = { it / 2 },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ) +
                            expandHorizontally(
                                expandFrom = Alignment.End,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ),
                    exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                            scaleOut(
                                targetScale = 0.7f,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ) +
                            slideOutHorizontally(
                                targetOffsetX = { it / 2 },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ) +
                            shrinkHorizontally(
                                shrinkTowards = Alignment.End,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            )
                ) {
                IconButton(
                    onClick = onInfoClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = stringResource(R.string.notifications_screen_info),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
                }
                actions()
            }
        }
    }
}
