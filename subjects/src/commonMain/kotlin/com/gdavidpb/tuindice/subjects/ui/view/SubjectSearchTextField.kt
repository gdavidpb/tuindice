package com.gdavidpb.tuindice.subjects.ui.view

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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.subjects_search_clear_content_description
import tuindice.subjects.generated.resources.subjects_search_placeholder

@Composable
fun SubjectSearchTextField(
	query: String,
	focusRequester: FocusRequester,
	onQueryChange: (String) -> Unit,
	onClearClick: () -> Unit,
	onSearch: () -> Unit
) {
	OutlinedTextField(
		modifier = Modifier
			.fillMaxWidth()
			.focusRequester(focusRequester)
			.testTag(SubjectsUiTags.SearchTextField),
		value = query,
		onValueChange = onQueryChange,
		singleLine = true,
		shape = RoundedCornerShape(16.dp),
		textStyle = MaterialTheme.typography.bodyLarge,
		placeholder = {
			Text(
				text = stringResource(Res.string.subjects_search_placeholder),
				style = MaterialTheme.typography.bodyLarge,
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
			if (query.isNotEmpty()) {
				IconButton(
					modifier = Modifier.testTag(SubjectsUiTags.SearchClear),
					onClick = onClearClick
				) {
					Icon(
						imageVector = Icons.Outlined.Close,
						contentDescription = stringResource(Res.string.subjects_search_clear_content_description)
					)
				}
			}
		},
		keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
		keyboardActions = KeyboardActions(onSearch = { onSearch() }),
		colors = OutlinedTextFieldDefaults.colors(
			focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.48f),
			unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.48f)
		)
	)
}
