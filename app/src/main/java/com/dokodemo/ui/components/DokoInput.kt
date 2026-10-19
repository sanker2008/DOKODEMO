package com.dokodemo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * 通用输入框（替代旧 IndustrialInput）
 * Material3 filled TextField，圆角16dp
 */
@Composable
fun DokoInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    singleLine: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null,
    // 兼容旧调用形式的多余参数
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = if (label.isNotEmpty()) ({ Text(label.trimEnd(':')) }) else null,
        placeholder = if (placeholder.isNotEmpty()) ({ Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) }) else null,
        singleLine = singleLine,
        enabled = enabled,
        trailingIcon = trailingContent,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        isError = isError || errorMessage != null,
        supportingText = if (errorMessage != null) ({ Text(errorMessage, color = MaterialTheme.colorScheme.error) }) else null,
        shape = RoundedCornerShape(16.dp),
        colors = dokoTextFieldColors()
    )
}

/**
 * 搜索输入框（替代旧 IndustrialSearchInput）
 */
@Composable
fun DokoSearchInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = androidx.compose.ui.res.stringResource(com.dokodemo.R.string.review_search)
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        shape = RoundedCornerShape(16.dp),
        colors = dokoTextFieldColors()
    )
}

/** Filled inputs use a background change for focus, with no outlines in any state. */
@Composable
fun dokoTextFieldColors() = TextFieldDefaults.colors(
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    errorIndicatorColor = Color.Transparent,
    focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surface,
    errorContainerColor = MaterialTheme.colorScheme.errorContainer
)
