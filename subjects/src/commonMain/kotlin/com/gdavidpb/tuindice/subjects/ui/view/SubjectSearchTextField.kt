package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.text.EditableTextFieldState
import com.gdavidpb.tuindice.base.ui.view.SearchTextField
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.subjects_search_clear_content_description
import tuindice.subjects.generated.resources.subjects_search_placeholder

@Composable
fun SubjectSearchTextField(
	fieldState: EditableTextFieldState,
	focusRequester: FocusRequester,
	onQueryChange: (String) -> Unit,
	onClearClick: () -> Unit,
	onSearch: () -> Unit
) {
	SearchTextField(
		modifier = Modifier
			.focusRequester(focusRequester)
			.testTag(SubjectsUiTags.SearchTextField),
		fieldState = fieldState,
		placeholderText = stringResource(Res.string.subjects_search_placeholder),
		clearContentDescription = stringResource(Res.string.subjects_search_clear_content_description),
		onQueryChange = onQueryChange,
		onClearClick = onClearClick,
		onSearch = onSearch,
		clearButtonModifier = Modifier.testTag(SubjectsUiTags.SearchClear)
	)
}
