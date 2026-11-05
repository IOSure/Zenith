package com.etrisad.zenith.ui.components.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.etrisad.zenith.R
import com.etrisad.zenith.data.preferences.UserPreferencesRepository
import com.etrisad.zenith.ui.components.ZenithButton
import com.etrisad.zenith.ui.components.ZenithButtonType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingStatsBottomSheet(
    repository: UserPreferencesRepository,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        OnboardingStatsContent(
            onSelection = { useSystemUsage ->
                scope.launch {
                    repository.setPreferSystemUsageHistory(useSystemUsage)
                    repository.setOnboardingStatsCompleted(true)
                    sheetState.hide()
                    onDismiss()
                }
            },
            onSkip = {
                scope.launch {
                    repository.setOnboardingStatsCompleted(true)
                    sheetState.hide()
                    onDismiss()
                }
            }
        )
    }
}

@Composable
fun OnboardingStatsContent(
    onSelection: (Boolean) -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.BarChart,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.winddown_stats_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.winddown_stats_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        OptionCard(
            title = stringResource(R.string.winddown_stats_start_fresh_title),
            description = stringResource(R.string.winddown_stats_start_fresh_description),
            pros = listOf(
                stringResource(R.string.winddown_stats_start_fresh_pro_accurate),
                stringResource(R.string.winddown_stats_start_fresh_pro_clean)
            ),
            cons = listOf(
                stringResource(R.string.winddown_stats_start_fresh_con_no_history),
                stringResource(R.string.winddown_stats_start_fresh_con_needs_usage)
            ),
            icon = Icons.Outlined.AutoAwesome,
            isHighlighted = true,
            onClick = { onSelection(false) },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        OptionCard(
            title = stringResource(R.string.winddown_stats_system_title),
            description = stringResource(R.string.winddown_stats_system_description),
            pros = listOf(
                stringResource(R.string.winddown_stats_system_pro_instant),
                stringResource(R.string.winddown_stats_system_pro_overview)
            ),
            cons = listOf(
                stringResource(R.string.winddown_stats_system_con_inaccurate),
                stringResource(R.string.winddown_stats_system_con_background),
                stringResource(R.string.winddown_stats_system_con_less_precise)
            ),
            icon = Icons.Outlined.History,
            isHighlighted = false,
            onClick = { onSelection(true) },
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 28.dp, bottomEnd = 28.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        ZenithButton(
            onClick = {},
            onHoldComplete = onSkip,
            text = stringResource(R.string.winddown_hold_to_skip),
            type = ZenithButtonType.Hold,
            fillMaxWidth = true,
            holdDuration = 2000L
        )
    }
}
