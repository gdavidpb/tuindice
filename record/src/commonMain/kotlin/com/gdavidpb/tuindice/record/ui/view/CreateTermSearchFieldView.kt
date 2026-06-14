package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import com.gdavidpb.tuindice.base.ui.view.SearchTextField
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_search_clear_content_description
import tuindice.record.generated.resources.create_term_search_placeholder

@Composable
fun CreateTermSearchField(
	query: TextFieldValue,
	focusRequester: FocusRequester,
	onQueryChange: (TextFieldValue) -> Unit,
	onClearQueryClick: () -> Unit,
	onSearch: () -> Unit
) {
	SearchTextField(
		modifier = Modifier
			.focusRequester(focusRequester)
			.testTag(RecordUiTags.CreateSyntheticTermSearchField),
		value = query,
		placeholderText = stringResource(Res.string.create_term_search_placeholder),
		clearContentDescription = stringResource(Res.string.create_term_search_clear_content_description),
		onValueChange = onQueryChange,
		onClearClick = onClearQueryClick,
		onSearch = onSearch,
		clearButtonModifier = Modifier.testTag(RecordUiTags.CreateSyntheticTermSearchClearButton),
		textStyle = MaterialTheme.typography.bodyMedium
	)
}
