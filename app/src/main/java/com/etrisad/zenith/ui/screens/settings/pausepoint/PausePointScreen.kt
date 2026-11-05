package com.etrisad.zenith.ui.screens.settings.pausepoint

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.etrisad.zenith.R
import com.etrisad.zenith.data.preferences.UserPreferences
import com.etrisad.zenith.data.preferences.UserPreferencesRepository
import com.etrisad.zenith.ui.components.ZenithButton
import com.etrisad.zenith.ui.components.ZenithButtonSize
import com.etrisad.zenith.ui.components.ZenithButtonType
import com.etrisad.zenith.ui.components.pausepoint.PausePointTaskType
import com.etrisad.zenith.ui.screens.settings.PreferenceCategory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PausePointScreen(
    preferences: UserPreferences,
    innerPadding: PaddingValues,
    preferencesRepository: UserPreferencesRepository,
    onTaskTypeClick: (PausePointTaskType) -> Unit = {},
    onBack: () -> Unit = {},
    backInterceptor: MutableState<() -> Boolean> = remember { mutableStateOf({ false }) }
) {
    val coroutineScope = rememberCoroutineScope()
    val isSingleType = preferences.pausePointTaskTypes.size == 1
    var showExitWarning by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun hideWarningSheet(afterHide: () -> Unit = {}) {
        coroutineScope.launch {
            try {
                if (sheetState.isVisible) sheetState.hide()
            } catch (_: Exception) { }
            showExitWarning = false
            afterHide()
        }
    }

    val tryInterceptBack: () -> Boolean = {
        if (isSingleType && !showExitWarning) {
            showExitWarning = true
            true
        } else {
            false
        }
    }
    val currentTryInterceptBack by rememberUpdatedState(tryInterceptBack)
    LaunchedEffect(Unit) {
        backInterceptor.value = { currentTryInterceptBack() }
    }
    DisposableEffect(Unit) {
        onDispose { backInterceptor.value = { false } }
    }

    BackHandler(enabled = isSingleType && !showExitWarning) {
        showExitWarning = true
    }

    if (showExitWarning) {
        val onlyTypeName = preferences.pausePointTaskTypes.firstOrNull()
            ?.let { stringResource(it.displayNameRes) }
            ?: stringResource(R.string.reels_settings_po_unknown_type)
        ModalBottomSheet(
            onDismissRequest = { hideWarningSheet() },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.WarningAmber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.reels_settings_po_only_one_type),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.reels_settings_po_only_one_type_body, onlyTypeName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                ZenithButton(
                    onClick = { hideWarningSheet() },
                    text = stringResource(R.string.reels_settings_po_add_another_type),
                    type = ZenithButtonType.Filled,
                    size = ZenithButtonSize.ExtraLarge,
                    fillMaxWidth = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                ZenithButton(
                    onClick = { hideWarningSheet { onBack() } },
                    text = stringResource(R.string.reels_settings_po_leave_anyway),
                    type = ZenithButtonType.Text,
                    contentColor = MaterialTheme.colorScheme.error,
                    size = ZenithButtonSize.ExtraLarge,
                    fillMaxWidth = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            )
    ) {
        AnimatedVisibility(
            visible = !preferences.pausePointEnabled,
            enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                    expandVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)),
            exit = fadeOut(spring(stiffness = Spring.StiffnessLow)) +
                   shrinkVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow))
        ) {
            Column {
                PausePointToggleCard(
                    enabled = preferences.pausePointEnabled,
                    onToggle = { enabled ->
                        coroutineScope.launch { preferencesRepository.setPausePointEnabled(enabled) }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (preferences.pausePointEnabled) {
            PreferenceCategory(title = stringResource(R.string.reels_settings_po_task_type))
            val toastContext = androidx.compose.ui.platform.LocalContext.current

            PausePointTaskType.entries.forEachIndexed { index, taskType ->
                val shape = when (index) {
                    0 -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                    PausePointTaskType.entries.lastIndex -> RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                    else -> RoundedCornerShape(8.dp)
                }
                val checked = taskType in preferences.pausePointTaskTypes
                val onCheckedChange: (Boolean) -> Unit = { isNowChecked ->
                    if (isNowChecked) {
                        val blockReason = when (taskType) {
                            com.etrisad.zenith.ui.components.pausepoint.PausePointTaskType.QR_SCAN ->
                                if (preferences.pausePointQrCodes.none { it.trim().isNotEmpty() })
                                    toastContext.getString(R.string.reels_settings_po_register_qr_first) else null
                            com.etrisad.zenith.ui.components.pausepoint.PausePointTaskType.NFC_SCAN ->
                                if (preferences.pausePointNfcTagIds.none {
                                        com.etrisad.zenith.ui.components.pausepoint.PausePointEngine.normalizeNfcId(it).isNotEmpty()
                                    }
                                ) toastContext.getString(R.string.reels_settings_po_register_nfc_first) else null
                            else -> null
                        }
                        if (blockReason != null) {
                            android.widget.Toast.makeText(toastContext, blockReason, android.widget.Toast.LENGTH_SHORT).show()
                            onTaskTypeClick(taskType)
                        } else {
                            val updated = preferences.pausePointTaskTypes + taskType
                            coroutineScope.launch { preferencesRepository.setPausePointTaskTypes(updated) }
                        }
                    } else {
                        val updated = preferences.pausePointTaskTypes - taskType
                        coroutineScope.launch { preferencesRepository.setPausePointTaskTypes(updated) }
                    }
                }

                SettingsToggleWithNavigation(
                    title = stringResource(taskType.displayNameRes),
                    description = stringResource(taskType.descriptionRes),
                    summary = pausePointSummary(toastContext, taskType, preferences),
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    icon = taskType.icon,
                    shape = shape,
                    onClick = { onTaskTypeClick(taskType) }
                )
                if (index != PausePointTaskType.entries.lastIndex) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun PausePointToggleCard(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    val containerColor by animateColorAsState(
        targetValue = if (enabled) MaterialTheme.colorScheme.primaryContainer
                      else MaterialTheme.colorScheme.surfaceContainerLow,
        label = "toggleCardColor"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (enabled) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PauseCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.reels_settings_po_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (enabled) stringResource(R.string.reels_settings_po_enabled_desc)
                    else stringResource(R.string.reels_settings_po_disabled_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                thumbContent = {
                    val thumbSize by animateDpAsState(
                        targetValue = if (enabled) 28.dp else 24.dp,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                        label = "thumb_size"
                    )
                    val iconColor by animateColorAsState(
                        targetValue = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "switch_icon_color"
                    )
                    Box(
                        modifier = Modifier.size(thumbSize),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = enabled,
                            transitionSpec = {
                                (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                 scaleIn(initialScale = 0.5f, animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMediumLow)))
                                    .togetherWith(fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                            scaleOut(targetScale = 0.5f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                            },
                            label = "switch_icon_anim"
                        ) { isChecked ->
                            Icon(
                                imageVector = if (isChecked) Icons.Filled.Check else Icons.Filled.Close,
                                contentDescription = null,
                                modifier = Modifier.size(if (isChecked) 18.dp else 16.dp),
                                tint = iconColor
                            )
                        }
                    }
                }
            )
        }
    }
}

private fun pausePointSummary(
    context: Context,
    taskType: PausePointTaskType,
    preferences: UserPreferences
): String = when (taskType) {
    PausePointTaskType.WAITING -> countSummary(context, preferences.pausePointWaitingVariants.size, R.string.reels_settings_po_subtask_singular, R.string.reels_settings_po_subtask_plural)
    PausePointTaskType.BREATHING -> countSummary(context, preferences.pausePointBreathingVariants.size, R.string.reels_settings_po_subtask_singular, R.string.reels_settings_po_subtask_plural)
    PausePointTaskType.WALK -> countSummary(context, preferences.pausePointWalkVariants.size, R.string.reels_settings_po_subtask_singular, R.string.reels_settings_po_subtask_plural)
    PausePointTaskType.QR_SCAN -> countSummary(context, preferences.pausePointQrCodes.count { it.isNotBlank() }, R.string.reels_settings_po_saved_code_singular, R.string.reels_settings_po_saved_code_plural)
    PausePointTaskType.NFC_SCAN -> countSummary(context, preferences.pausePointNfcTagIds.count { com.etrisad.zenith.ui.components.pausepoint.PausePointEngine.normalizeNfcId(it).isNotEmpty() }, R.string.reels_settings_po_saved_tag_singular, R.string.reels_settings_po_saved_tag_plural)
    PausePointTaskType.NUMBER_SLIDE -> countSummary(context, preferences.pausePointNumberSlideVariants.size, R.string.reels_settings_po_subtask_singular, R.string.reels_settings_po_subtask_plural)
    PausePointTaskType.SWITCH -> countSummary(context, preferences.pausePointSwitchVariants.size, R.string.reels_settings_po_subtask_singular, R.string.reels_settings_po_subtask_plural)
    PausePointTaskType.MATH -> countSummary(context, preferences.pausePointMathVariants.size, R.string.reels_settings_po_subtask_singular, R.string.reels_settings_po_subtask_plural)
    PausePointTaskType.COUNTING -> countSummary(context, preferences.pausePointCountingVariants.size, R.string.reels_settings_po_subtask_singular, R.string.reels_settings_po_subtask_plural)
    PausePointTaskType.TYPING -> countSummary(context, preferences.pausePointTypingVariants.size, R.string.reels_settings_po_text_singular, R.string.reels_settings_po_text_plural)
    PausePointTaskType.CHOOSE_APP -> context.getString(R.string.reels_settings_po_uses_goal_apps)
}

private fun countSummary(context: Context, count: Int, singularRes: Int, pluralRes: Int): String =
    context.getString(if (count == 1) singularRes else pluralRes, count)

@Composable
private fun SettingsToggleWithNavigation(
    title: String,
    description: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    shape: androidx.compose.ui.graphics.Shape,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            VerticalDivider(
                modifier = Modifier.height(40.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                thumbContent = {
                    val thumbSize by animateDpAsState(
                        targetValue = if (checked) 28.dp else 24.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "thumb_size"
                    )

                    val iconColor by animateColorAsState(
                        targetValue = if (checked) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerHighest,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "switch_icon_color"
                    )

                    Box(
                        modifier = Modifier.size(thumbSize),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = checked,
                            transitionSpec = {
                                (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                        scaleIn(initialScale = 0.5f, animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMediumLow)))
                                    .togetherWith(fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                                            scaleOut(targetScale = 0.5f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                            },
                            label = "switch_icon_anim"
                        ) { isChecked ->
                            Icon(
                                imageVector = if (isChecked) Icons.Filled.Check else Icons.Filled.Close,
                                contentDescription = null,
                                modifier = Modifier.size(if (isChecked) 18.dp else 16.dp),
                                tint = iconColor
                            )
                        }
                    }
                }
            )
        }
    }
}