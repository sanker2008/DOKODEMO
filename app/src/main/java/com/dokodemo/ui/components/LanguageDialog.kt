package com.dokodemo.ui.components

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.dokodemo.R

@Composable
fun LanguageDialog(
    onDismiss: () -> Unit
) {
    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val currentTag = if (currentLocales.isEmpty) "system" else currentLocales[0]?.toLanguageTag() ?: "system"

    val languages = listOf(
        "system" to stringResource(R.string.lang_system),
        "zh-CN" to stringResource(R.string.lang_zh_cn),
        "zh-TW" to stringResource(R.string.lang_zh_tw),
        "en" to stringResource(R.string.lang_en)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = { Text(stringResource(R.string.language), fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                Text(
                    stringResource(R.string.language_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                languages.forEach { (tag, name) ->
                    val isSelected = tag == currentTag || (tag == "zh-CN" && currentTag == "zh") // Handle generic zh mapping
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                if (tag == "system") {
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                                } else {
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
                                }
                                onDismiss()
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                if (tag == "system") {
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                                } else {
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
                                }
                                onDismiss()
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(name, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}