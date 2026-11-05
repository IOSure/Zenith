package com.etrisad.zenith.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.etrisad.zenith.R
import com.etrisad.zenith.ui.components.ZenithButton
import com.etrisad.zenith.ui.components.ZenithButtonSize
import com.etrisad.zenith.ui.components.ZenithButtonType
import com.etrisad.zenith.ui.components.focus.CardGroup
import kotlinx.coroutines.launch

private data class PrivacySection(
    val titleRes: Int,
    val icon: ImageVector,
    val summaryRes: Int,
    val detailResList: List<Int>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyInfoBottomSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val sections = remember {
        listOf(
            PrivacySection(
                titleRes = R.string.intercept_accessibility_service,
                icon = Icons.Outlined.AccessibilityNew,
                summaryRes = R.string.intercept_accessibility_service_summary,
                detailResList = listOf(
                    R.string.intercept_accessibility_why,
                    R.string.intercept_accessibility_standard_mode,
                    R.string.intercept_accessibility_limited_mode,
                    R.string.intercept_accessibility_no_keylogging,
                    R.string.intercept_accessibility_revoked,
                    R.string.intercept_accessibility_limited_scope
                )
            ),
            PrivacySection(
                titleRes = R.string.intercept_notification_interception,
                icon = Icons.Outlined.NotificationsActive,
                summaryRes = R.string.intercept_notification_interception_summary,
                detailResList = listOf(
                    R.string.intercept_notification_why,
                    R.string.intercept_notification_turn_off,
                    R.string.intercept_notification_disabled_effects,
                    R.string.intercept_notification_content_control,
                    R.string.intercept_notification_private_apps,
                    R.string.intercept_notification_storage,
                    R.string.intercept_notification_revoke_access
                )
            ),
            PrivacySection(
                titleRes = R.string.intercept_local_storage_cloud_backup,
                icon = Icons.Outlined.Storage,
                summaryRes = R.string.intercept_local_storage_summary,
                detailResList = listOf(
                    R.string.intercept_local_database,
                    R.string.intercept_android_cloud_backup,
                    R.string.intercept_what_android_backs_up,
                    R.string.intercept_local_backups,
                    R.string.intercept_private_apps_purge,
                    R.string.intercept_hidden_exclusions,
                    R.string.intercept_data_control
                )
            ),
            PrivacySection(
                titleRes = R.string.intercept_private_hidden_vs_bypass,
                icon = Icons.Outlined.Security,
                summaryRes = R.string.intercept_private_hidden_summary,
                detailResList = listOf(
                    R.string.intercept_private_ignored,
                    R.string.intercept_hidden_monitored,
                    R.string.intercept_bypass_tracked,
                    R.string.intercept_private_hidden_in_short
                )
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = screenHeight * 0.9f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Icon(
                            Icons.Outlined.Security,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.intercept_privacy_data_guarantees),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = stringResource(R.string.intercept_privacy_data_guarantees_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        sections.forEachIndexed { index, section ->
                            PrivacySectionCard(
                                section = section,
                                shape = when {
                                    sections.size == 1 -> RoundedCornerShape(24.dp)
                                    index == 0 -> RoundedCornerShape(
                                        topStart = 24.dp,
                                        topEnd = 24.dp,
                                        bottomStart = 8.dp,
                                        bottomEnd = 8.dp
                                    )
                                    index == sections.lastIndex -> RoundedCornerShape(
                                        topStart = 8.dp,
                                        topEnd = 8.dp,
                                        bottomStart = 24.dp,
                                        bottomEnd = 24.dp
                                    )
                                    else -> RoundedCornerShape(8.dp)
                                }
                            )
                            if (index != sections.lastIndex) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(120.dp))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(MaterialTheme.colorScheme.surface, Color.Transparent)
                                )
                            )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, MaterialTheme.colorScheme.surface),
                            startY = 0f,
                            endY = 40f
                        )
                    )
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                ZenithButton(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.intercept_understood),
                    size = ZenithButtonSize.ExtraLarge,
                    fillMaxWidth = true
                )
            }
        }
    }
}

@Composable
private fun PrivacySectionCard(
    section: PrivacySection,
    shape: Shape
) {
    var expanded by remember { mutableStateOf(false) }

    CardGroup(
        shape = shape,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = section.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = stringResource(section.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(section.summaryRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            ZenithButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.Start),
                text = if (expanded) {
                    stringResource(R.string.intercept_hide_details)
                } else {
                    stringResource(R.string.intercept_show_details)
                },
                icon = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                type = ZenithButtonType.Text,
                size = ZenithButtonSize.Small
            )

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(durationMillis = 220)),
                exit = shrinkVertically(animationSpec = tween(durationMillis = 180))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    section.detailResList.forEach { detailRes ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(14.dp)
                            )
                            Text(
                                text = stringResource(detailRes),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
