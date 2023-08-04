package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow

interface DropdownMenuItem {
	val text: String
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : DropdownMenuItem> DropdownMenuTextField(
	modifier: Modifier = Modifier,
	items: List<T>,
	selectedItem: T? = items.firstOrNull(),
	onItemSelected: (item: T) -> Unit,
	error: String? = null,
	placeholder: @Composable (() -> Unit)? = null,
	leadingIcon: @Composable (() -> Unit)? = null
) {
	val expanded = remember { mutableStateOf(false) }
	val selectedItemText = remember { mutableStateOf(selectedItem?.text.orEmpty()) }
	val supportingText = remember { mutableStateOf(error) }

	ExposedDropdownMenuBox(
		modifier = modifier,
		expanded = expanded.value,
		onExpandedChange = {
			expanded.value = !expanded.value
		}
	) {
		OutlinedTextField(
			modifier = modifier
				.menuAnchor(),
			readOnly = true,
			value = selectedItemText.value,
			onValueChange = { },
			isError = supportingText.value != null,
			supportingText = {
				val text = supportingText.value

				if (text != null) Text(text)
			},
			trailingIcon = {
				ExposedDropdownMenuDefaults.TrailingIcon(
					expanded = expanded.value
				)
			},
			placeholder = placeholder,
			leadingIcon = leadingIcon,
			colors = ExposedDropdownMenuDefaults.textFieldColors(
				focusedContainerColor = MaterialTheme.colorScheme.background,
				unfocusedContainerColor = MaterialTheme.colorScheme.background
			),
			singleLine = true
		)
		ExposedDropdownMenu(
			expanded = expanded.value,
			onDismissRequest = {
				expanded.value = false
			}
		) {
			items.forEach { item ->
				DropdownMenuItem(
					text = {
						Text(
							text = item.text,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis
						)
					},
					onClick = {
						onItemSelected(item)
						selectedItemText.value = item.text
						expanded.value = false
					}
				)
			}
		}
	}
}