package com.etrisad.zenith.ui.screens.settings.pausepoint

import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.etrisad.zenith.BuildConfig
import com.etrisad.zenith.R
import com.etrisad.zenith.data.preferences.UserPreferences
import com.etrisad.zenith.data.preferences.UserPreferencesRepository
import com.etrisad.zenith.service.InterceptOverlayManager
import com.etrisad.zenith.ui.components.ZenithButton
import com.etrisad.zenith.ui.components.ZenithButtonSize
import com.etrisad.zenith.ui.components.ZenithButtonType
import com.etrisad.zenith.ui.components.ZenithButtonWeighted
import com.etrisad.zenith.ui.components.ZenithGroupedButton
import com.etrisad.zenith.ui.components.pausepoint.MathOperator
import com.etrisad.zenith.ui.components.pausepoint.PausePointTask
import com.etrisad.zenith.ui.components.pausepoint.PausePointTaskType
import com.etrisad.zenith.ui.components.pausepoint.PausePointVariant
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PausePointTypeSettingsScreen(
    taskType: PausePointTaskType,
    preferences: UserPreferences,
    innerPadding: PaddingValues,
    preferencesRepository: UserPreferencesRepository
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val variants = preferences.pausePointConfig.variantsFor(taskType).distinct()

    val saveVariants: (List<PausePointVariant>) -> Unit = { newList ->
        coroutineScope.launch { preferencesRepository.setPausePointVariants(taskType, newList) }
    }

    val launchTest: () -> Unit = {
        if (Settings.canDrawOverlays(context)) {
            val manager = InterceptOverlayManager(context.applicationContext, preferencesRepository)
            manager.showOverlay(
                packageName = context.packageName,
                appName = "Zenith",
                shield = null,
                totalUsageToday = 0,
                totalGlobalUsageToday = 0,
                delayDurationSeconds = 0,
                forcedTaskType = taskType,
                onAllowUse = { _, _ -> },
                onCloseApp = {},
                onGoalDismiss = {}
            )
        } else {
            Toast.makeText(context, context.getString(R.string.pause_toast_overlay_permission_required), Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding() + 16.dp,
            bottom = innerPadding.calculateBottomPadding() + 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            val headerCount = when (taskType) {
                PausePointTaskType.QR_SCAN -> preferences.pausePointQrCodes.count { it.trim().isNotEmpty() }
                PausePointTaskType.NFC_SCAN -> preferences.pausePointNfcTagIds.count {
                    com.etrisad.zenith.ui.components.pausepoint.PausePointEngine.normalizeNfcId(it).isNotEmpty()
                }
                PausePointTaskType.CHOOSE_APP -> -1
                else -> variants.size
            }
            PausePointTypeHeader(taskType, variantCount = headerCount)
            if (BuildConfig.DEBUG) {
                Spacer(modifier = Modifier.height(16.dp))
                PausePointTestButton(onClick = launchTest)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        when {
            taskType == PausePointTaskType.QR_SCAN -> {
                item {
                    PausePointQrManagerContent(
                        preferences = preferences,
                        preferencesRepository = preferencesRepository
                    )
                }
            }
            taskType == PausePointTaskType.NFC_SCAN -> {
                item {
                    PausePointNfcManagerContent(
                        preferences = preferences,
                        preferencesRepository = preferencesRepository
                    )
                }
            }
            taskType == PausePointTaskType.CHOOSE_APP -> {
                item {
                    ChooseAppConfigCard()
                }
            }
            else -> {
                if (variants.isEmpty()) {
                    item {
                        EmptyVariantsHintCard(
                            hint = when (taskType) {
                                PausePointTaskType.TYPING -> stringResource(R.string.pause_empty_typing_hint)
                                else -> stringResource(R.string.pause_empty_variants_hint)
                            }
                        )
                    }
                } else {
                    itemsIndexed(
                        items = variants,
                        key = { _, variant -> variant.
                        composesKey() }
                    ) { index, variant ->
                        PausePointVariantCard(
                            index = index,
                            total = variants.size,
                            variant = variant,
                            taskType = taskType,
                            onDelete = { saveVariants(variants.filterIndexed { i, _ -> i != index }) },
                            modifier = Modifier.animateItem(
                                fadeInSpec = spring(stiffness = Spring.StiffnessLow),
                                fadeOutSpec = spring(stiffness = Spring.StiffnessLow),
                                placementSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)
                            )
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    AddVariantEditor(
                        taskType = taskType,
                        onAdd = { newVariant ->
                            if (variants.none { it == newVariant }) {
                                saveVariants(variants + newVariant)
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun PausePointVariant.composesKey(): String =
    buildString {
        append(seconds).append(';')
        append(rounds).append(';')
        append(steps).append(';')
        append(size).append(';')
        append(levers).append(';')
        append(maxOperand).append(';')
        append(target).append(';')
        append(MathOperator.normalize(mathOperator)).append(';')
        append(label.length).append(':').append(label).append(';')
        append(text.length).append(':').append(text)
    }

@Composable
internal fun PausePointTestButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ZenithButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        text = stringResource(R.string.pause_test),
        icon = Icons.Filled.PlayArrow,
        type = ZenithButtonType.Tonal,
        size = ZenithButtonSize.Large
    )
}

@Composable
private fun PausePointTypeHeader(taskType: PausePointTaskType, variantCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = taskType.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(taskType.displayNameRes),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(taskType.descriptionRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = CircleShape,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = when {
                    variantCount < 0 -> stringResource(R.string.pause_header_uses_goal_apps)
                    taskType == com.etrisad.zenith.ui.components.pausepoint.PausePointTaskType.QR_SCAN ->
                        if (variantCount == 1) stringResource(R.string.pause_header_saved_code_one)
                        else stringResource(R.string.pause_header_saved_codes_other, variantCount)
                    taskType == com.etrisad.zenith.ui.components.pausepoint.PausePointTaskType.NFC_SCAN ->
                        if (variantCount == 1) stringResource(R.string.pause_header_saved_tag_one)
                        else stringResource(R.string.pause_header_saved_tags_other, variantCount)
                    variantCount == 0 -> stringResource(R.string.pause_header_default_value)
                    variantCount == 1 -> stringResource(R.string.pause_header_subtask_one)
                    else -> stringResource(R.string.pause_header_subtasks_other, variantCount)
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PausePointVariantCard(
    index: Int,
    total: Int,
    variant: PausePointVariant,
    taskType: PausePointTaskType,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = when {
            total == 1 -> RoundedCornerShape(24.dp)
            index == 0 -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            index == total - 1 -> RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
            else -> RoundedCornerShape(8.dp)
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHighest,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = variantSummary(taskType, variant),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(taskType.displayNameRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                        CircleShape
                    ),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.pause_delete_subtask),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyVariantsHintCard(hint: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AddVariantEditor(
    taskType: PausePointTaskType,
    onAdd: (PausePointVariant) -> Unit
) {
    var sliderValue by remember(taskType) {
        mutableFloatStateOf(defaultEditorValue(taskType).toFloat())
    }
    var label by remember(taskType) { mutableStateOf("") }
    var text by remember(taskType) { mutableStateOf("") }
    var switchTimeEnabled by remember(taskType) { mutableStateOf(false) }
    var switchTimeValue by remember(taskType) { mutableFloatStateOf(15f) }
    var mathOperator by remember(taskType) { mutableStateOf(MathOperator.ADD) }

    val canAdd = when (taskType) {
        PausePointTaskType.TYPING -> text.isNotBlank()
        else -> true
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = taskType.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.pause_add_sub_task),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = addEditorHint(taskType),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (taskType) {
                PausePointTaskType.WAITING -> EditorSlider(
                    title = stringResource(R.string.pause_duration),
                    value = sliderValue,
                    valueRange = 5f..60f,
                    steps = 54,
                    unit = stringResource(R.string.pause_unit_sec),
                    onValueChange = { sliderValue = it }
                )
                PausePointTaskType.BREATHING -> EditorSlider(
                    title = stringResource(R.string.pause_breathing_rounds),
                    value = sliderValue,
                    valueRange = 1f..10f,
                    steps = 8,
                    unit = stringResource(R.string.pause_unit_rounds),
                    onValueChange = { sliderValue = it }
                )
                PausePointTaskType.WALK -> EditorSlider(
                    title = stringResource(R.string.pause_steps),
                    value = sliderValue,
                    valueRange = 1f..50f,
                    steps = 48,
                    unit = stringResource(R.string.pause_unit_steps),
                    onValueChange = { sliderValue = it }
                )
                PausePointTaskType.NUMBER_SLIDE -> EditorSlider(
                    title = stringResource(R.string.pause_grid_size),
                    value = sliderValue,
                    valueRange = 3f..6f,
                    steps = 3,
                    unit = "x",
                    onValueChange = { sliderValue = it }
                )
                PausePointTaskType.SWITCH -> {
                    EditorSlider(
                        title = stringResource(R.string.pause_lever_count),
                        value = sliderValue,
                        valueRange = 2f..8f,
                        steps = 6,
                        unit = stringResource(R.string.pause_unit_levers),
                        onValueChange = { sliderValue = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.pause_time_limit),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.pause_reset_puzzle_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = switchTimeEnabled,
                            onCheckedChange = { switchTimeEnabled = it },
                            thumbContent = {
                                val thumbSize by animateDpAsState(
                                    targetValue = if (switchTimeEnabled) 28.dp else 24.dp,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    ),
                                    label = "thumb_size"
                                )

                                val iconColor by animateColorAsState(
                                    targetValue = if (switchTimeEnabled) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceContainerHighest,
                                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                    label = "switch_icon_color"
                                )

                                Box(
                                    modifier = Modifier.size(thumbSize),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AnimatedContent(
                                        targetState = switchTimeEnabled,
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
                    if (switchTimeEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        EditorSlider(
                            title = stringResource(R.string.pause_time_limit_caps),
                            value = switchTimeValue,
                            valueRange = 5f..60f,
                            steps = 54,
                            unit = stringResource(R.string.pause_unit_sec),
                            onValueChange = { switchTimeValue = it }
                        )
                    }
                }
                PausePointTaskType.MATH -> {
                    MathOperatorSelector(
                        selected = mathOperator,
                        onSelect = {
                            mathOperator = it
                            if (it == MathOperator.MULTIPLY || it == MathOperator.DIVIDE) {
                                if (sliderValue > 12f) sliderValue = 12f
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    EditorSlider(
                        title = stringResource(R.string.pause_max_operand),
                        value = sliderValue,
                        valueRange = 1f..100f,
                        steps = 98,
                        unit = "",
                        onValueChange = {
                            sliderValue =
                                if (mathOperator == MathOperator.MULTIPLY || mathOperator == MathOperator.DIVIDE) {
                                    it.coerceAtMost(12f)
                                } else it
                        }
                    )
                }
                PausePointTaskType.COUNTING -> {
                    EditorSlider(
                        title = stringResource(R.string.pause_target_number),
                        value = sliderValue,
                        valueRange = 5f..30f,
                        steps = 24,
                        unit = "",
                        onValueChange = { sliderValue = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.pause_exercise_name_label)) },
                        placeholder = { Text(stringResource(R.string.pause_exercise_name_placeholder)) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )
                }
                PausePointTaskType.TYPING -> {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.pause_sentence_label)) },
                        placeholder = { Text(stringResource(R.string.pause_sentence_placeholder)) },
                        minLines = 2,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(20.dp))

            ZenithButton(
                onClick = {
                    onAdd(
                        when (taskType) {
                            PausePointTaskType.WAITING -> PausePointVariant(seconds = sliderValue.toInt())
                            PausePointTaskType.BREATHING -> PausePointVariant(rounds = sliderValue.toInt())
                            PausePointTaskType.WALK -> PausePointVariant(steps = sliderValue.toInt())
                            PausePointTaskType.NUMBER_SLIDE -> PausePointVariant(size = sliderValue.toInt())
                            PausePointTaskType.SWITCH -> PausePointVariant(
                                levers = sliderValue.toInt(),
                                seconds = if (switchTimeEnabled) switchTimeValue.toInt() else 0
                            )
                            PausePointTaskType.MATH -> PausePointVariant(
                                maxOperand = sliderValue.toInt(),
                                mathOperator = mathOperator.symbol
                            )
                            PausePointTaskType.COUNTING -> PausePointVariant(target = sliderValue.toInt(), label = label.trim())
                            PausePointTaskType.TYPING -> PausePointVariant(text = text.trim())
                            else -> PausePointVariant()
                        }
                    )
                    label = ""
                    text = ""
                },
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.pause_add_sub_task),
                icon = Icons.Filled.Add,
                size = ZenithButtonSize.Large,
                enabled = canAdd
            )
        }
    }
}

@Composable
private fun MathOperatorSelector(
    selected: MathOperator,
    onSelect: (MathOperator) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.pause_operator),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        ZenithGroupedButton(size = ZenithButtonSize.Small) {
            val ops = MathOperator.entries
            ops.forEachIndexed { index, op ->
                val isSelected = op == selected
                val shape = when (index) {
                    0 -> RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 8.dp, bottomEnd = 8.dp)
                    ops.lastIndex -> RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp, topStart = 8.dp, bottomStart = 8.dp)
                    else -> RoundedCornerShape(8.dp)
                }
                ZenithButtonWeighted(
                    onClick = { onSelect(op) },
                    text = op.displaySymbol,
                    type = ZenithButtonType.Filled,
                    containerColor = if (isSelected) null else MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = if (isSelected) null else MaterialTheme.colorScheme.primary,
                    size = ZenithButtonSize.Small,
                    selected = isSelected,
                    shape = shape,
                    isFirst = index == 0,
                    isLast = index == ops.lastIndex,
                    contentScaleEnabled = false
                )
            }
        }
    }
}

@Composable
private fun EditorSlider(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    unit: String,
    onValueChange: (Float) -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(4.dp))
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        text = if (unit.isEmpty()) "${value.toInt()}" else "${value.toInt()} $unit",
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun addEditorHint(taskType: PausePointTaskType): String = when (taskType) {
    PausePointTaskType.WAITING -> stringResource(R.string.pause_add_hint_waiting)
    PausePointTaskType.BREATHING -> stringResource(R.string.pause_add_hint_breathing)
    PausePointTaskType.WALK -> stringResource(R.string.pause_add_hint_walk)
    PausePointTaskType.NUMBER_SLIDE -> stringResource(R.string.pause_add_hint_number_slide)
    PausePointTaskType.SWITCH -> stringResource(R.string.pause_add_hint_switch)
    PausePointTaskType.MATH -> stringResource(R.string.pause_add_hint_math)
    PausePointTaskType.COUNTING -> stringResource(R.string.pause_add_hint_counting)
    PausePointTaskType.TYPING -> stringResource(R.string.pause_add_hint_typing)
    else -> ""
}

private fun defaultEditorValue(taskType: PausePointTaskType): Int = when (taskType) {
    PausePointTaskType.WAITING -> 15
    PausePointTaskType.BREATHING -> 3
    PausePointTaskType.WALK -> 10
    PausePointTaskType.NUMBER_SLIDE -> 3
    PausePointTaskType.SWITCH -> 4
    PausePointTaskType.MATH -> 20
    PausePointTaskType.COUNTING -> 15
    else -> 0
}

@Composable
private fun variantSummary(taskType: PausePointTaskType, variant: PausePointVariant): String = when (taskType) {
    PausePointTaskType.WAITING -> stringResource(R.string.pause_summary_wait, variant.seconds)
    PausePointTaskType.BREATHING ->
        if (variant.rounds == 1) stringResource(R.string.pause_summary_breath_one, variant.rounds)
        else stringResource(R.string.pause_summary_breaths_other, variant.rounds)
    PausePointTaskType.WALK -> stringResource(R.string.pause_summary_walk, variant.steps)
    PausePointTaskType.NUMBER_SLIDE -> stringResource(R.string.pause_summary_number_slide, variant.size, variant.size)
    PausePointTaskType.SWITCH ->
        if (variant.seconds > 0) stringResource(R.string.pause_summary_lever_time, variant.levers, variant.seconds)
        else stringResource(R.string.pause_summary_lever, variant.levers)
    PausePointTaskType.MATH -> {
        val op = MathOperator.fromSymbol(variant.mathOperator)
        stringResource(R.string.pause_summary_math, op.displaySymbol, variant.maxOperand)
    }
    PausePointTaskType.COUNTING ->
        if (variant.label.isNotBlank()) stringResource(R.string.pause_summary_counting_named, variant.target, variant.label)
        else stringResource(R.string.pause_summary_count_to, variant.target)
    PausePointTaskType.TYPING -> {
        val poolIndex = PausePointTask.sentencePool.indexOf(variant.text)
        val sentence = if (poolIndex >= 0) stringResource(PausePointTask.sentencePoolRes[poolIndex])
        else variant.text
        "\u201C$sentence\u201D"
    }
    else -> stringResource(R.string.pause_summary_default)
}

@Composable
private fun ChooseAppConfigCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.pause_choose_app_info),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}