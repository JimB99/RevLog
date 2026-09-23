package com.revlog.app.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.revlog.app.R
import com.revlog.domain.DecimalSeparator
import com.revlog.domain.InputNormalizer

@Composable
fun SelectableContent(content: @Composable () -> Unit) {
    SelectionContainer { content() }
}

@Composable
fun FormField(
    labelRes: Int,
    value: String,
    languageTag: String,
    placeholder: String = "",
    isError: Boolean = false,
    normalizeDecimal: Boolean = true,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val decimalSeparator = DecimalSeparator.forLanguageTag(languageTag)
    OutlinedTextField(
        value = value,
        onValueChange = { raw ->
            var v = if (normalizeDecimal) {
                InputNormalizer.normalizeDecimalSeparator(raw, decimalSeparator)
            } else {
                raw
            }
            v = InputNormalizer.sanitizeSingleLine(v)
            onValueChange(v)
        },
        label = { Text(stringResource(labelRes)) },
        placeholder = if (placeholder.isNotBlank()) ({ Text(placeholder) }) else null,
        isError = isError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        modifier = modifier.fillMaxWidth(),
    )
}
