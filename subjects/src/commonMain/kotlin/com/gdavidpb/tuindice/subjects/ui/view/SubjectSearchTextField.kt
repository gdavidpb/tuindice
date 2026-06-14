package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.gdavidpb.tuindice.base.ui.view.SearchTextField
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
	val fieldValueState = remember {
		mutableStateOf(
			TextFieldValue(
				text = query,
				selection = TextRange(query.length)
			)
		)
	}
	val fieldValue = if (fieldValueState.value.text == query) {
		fieldValueState.value
	} else {
		TextFieldValue(
			text = query,
			selection = TextRange(query.length)
		)
	}

	SearchTextField(
		modifier = Modifier
			.focusRequester(focusRequester)
			.testTag(SubjectsUiTags.SearchTextField),
		value = fieldValue,
		placeholderText = stringResource(Res.string.subjects_search_placeholder),
		clearContentDescription = stringResource(Res.string.subjects_search_clear_content_description),
		onValueChange = { value ->
			fieldValueState.value = value
			onQueryChange(value.text)
		},
		onClearClick = onClearClick,
		onSearch = onSearch,
		clearButtonModifier = Modifier.testTag(SubjectsUiTags.SearchClear)
	)
}
