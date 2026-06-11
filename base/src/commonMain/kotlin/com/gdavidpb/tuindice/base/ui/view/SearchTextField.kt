package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius

@Composable
fun SearchTextField(
	value: TextFieldValue,
	placeholderText: String,
	clearContentDescription: String,
	onValueChange: (TextFieldValue) -> Unit,
	onClearClick: () -> Unit,
	onSearch: () -> Unit,
	modifier: Modifier = Modifier,
	clearButtonModifier: Modifier = Modifier,
	textStyle: TextStyle = MaterialTheme.typography.bodyLarge
) {
	OutlinedTextField(
		modifier = modifier.fillMaxWidth(),
		value = value,
		onValueChange = onValueChange,
		singleLine = true,
		shape = RoundedCornerShape(TuIndiceRadius.Large),
		textStyle = textStyle,
		placeholder = {
			Text(
				text = placeholderText,
				style = textStyle,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		},
		leadingIcon = {
			Icon(
				imageVector = Icons.Outlined.Search,
				contentDescription = null
			)
		},
		trailingIcon = {
			if (value.text.isNotEmpty()) {
				IconButton(
					modifier = clearButtonModifier,
					onClick = onClearClick
				) {
					Icon(
						imageVector = Icons.Outlined.Close,
						contentDescription = clearContentDescription
					)
				}
			}
		},
		keyboardOptions = KeyboardOptions(
			capitalization = KeyboardCapitalization.None,
			autoCorrectEnabled = false,
			keyboardType = KeyboardType.Ascii,
			imeAction = ImeAction.Search
		),
		keyboardActions = KeyboardActions(onSearch = { onSearch() }),
		colors = OutlinedTextFieldDefaults.colors(
			focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = SearchTextFieldContainerAlpha),
			unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = SearchTextFieldContainerAlpha)
		)
	)
}

private const val SearchTextFieldContainerAlpha = 0.48f
