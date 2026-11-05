package com.etrisad.zenith.ui.screens.settings

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.etrisad.zenith.R
import com.etrisad.zenith.util.AppLanguage

/**
 * Settings entry that lets the user override the app language.
 *
 * Selecting a language recreates the hosting activity so that every composable
 * resolves its strings again against the new configuration.
 */
@Composable
fun LanguageSettingsItem() {
    val context = LocalContext.current
    val activity = context as? Activity
    var selection by remember { mutableStateOf(AppLanguage.selection(context)) }
    var showDialog by remember { mutableStateOf(false) }

    SettingsActionItem(
        title = stringResource(R.string.settings_language_title),
        summary = languageLabel(selection),
        onClick = { showDialog = true },
        icon = Icons.Outlined.Translate,
        shape = RoundedCornerShape(24.dp)
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.settings_language_dialog_title)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.settings_language_dialog_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppLanguage.options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selection = option
                                    showDialog = false
                                    AppLanguage.setSelection(context, option)
                                    activity?.recreate()
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selection == option, onClick = null)
                            Text(
                                text = languageLabel(option),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.settings_language_cancel))
                }
            }
        )
    }
}

@Composable
private fun languageLabel(option: String): String = when (option) {
    AppLanguage.SIMPLIFIED_CHINESE -> stringResource(R.string.settings_language_chinese)
    AppLanguage.ENGLISH -> stringResource(R.string.settings_language_english)
    else -> stringResource(R.string.settings_language_system)
}
