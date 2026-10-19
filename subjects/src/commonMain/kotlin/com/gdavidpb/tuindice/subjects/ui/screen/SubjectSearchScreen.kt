package com.gdavidpb.tuindice.subjects.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.keyboard.dismissKeyboardOnScroll
import com.gdavidpb.tuindice.base.ui.keyboard.dismissKeyboardOnTap
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.text.EditableTextFieldState
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectSearch
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.subjects.ui.view.SubjectSearchError
import com.gdavidpb.tuindice.subjects.ui.view.SubjectSearchGuidanceView
import com.gdavidpb.tuindice.subjects.ui.view.SubjectSearchResults
import com.gdavidpb.tuindice.subjects.ui.view.SubjectSearchTextField

@Composable
fun SubjectSearchScreen(
	state: SubjectSearch.State,
	onQueryChange: (String) -> Unit,
	onClearClick: () -> Unit,
	onRetryClick: () -> Unit,
	onSubjectClick: (String) -> Unit,
	modifier: Modifier = Modifier
) {
	val focusRequester = remember { FocusRequester() }
	val focusManager = LocalFocusManager.current
	val searchField = remember { EditableTextFieldState(state.query) }

	searchField.syncExternal(state.query, resetKey = null)

	LaunchedEffect(Unit) {
		focusRequester.requestFocus()
	}

	fun dismissKeyboard() {
		focusManager.clearFocus()
	}

	// The field stays at the top. What is under it ends above the keyboard.
	Column(
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
			// Before the padding: the margin around the content is background too.
			.dismissKeyboardOnTap(focusManager)
			.padding(horizontal = TuIndiceSpacing.Screen, vertical = TuIndiceSpacing.Screen)
			.testTag(SubjectsUiTags.SearchScreen)
	) {
		SubjectSearchTextField(
			fieldState = searchField,
			focusRequester = focusRequester,
			onQueryChange = onQueryChange,
			onClearClick = onClearClick,
			onSearch = ::dismissKeyboard
		)
		Spacer(modifier = Modifier.height(20.dp))

		SubjectSearchPanel(
			state = state,
			onExampleClick = { example ->
				searchField.replace(example)
				onQueryChange(example)
			},
			onRetryClick = onRetryClick,
			onSubjectClick = { subjectCode ->
				dismissKeyboard()
				onSubjectClick(subjectCode)
			},
			onDismissKeyboard = ::dismissKeyboard
		)
	}
}

// What is under the field: the guidance, the error or the results. The results are a list that scrolls in the room that
// is left; the guidance and the error are short texts that scroll too when the keyboard takes room.
@Composable
private fun ColumnScope.SubjectSearchPanel(
	state: SubjectSearch.State,
	onExampleClick: (String) -> Unit,
	onRetryClick: () -> Unit,
	onSubjectClick: (String) -> Unit,
	onDismissKeyboard: () -> Unit
) {
	val focusManager = LocalFocusManager.current

	when {
		state.query.trim().length < 2 ->
			SubjectSearchGuidanceView(
				query = state.query,
				onExampleClick = onExampleClick,
				modifier = Modifier
					.weight(1f)
					.imePadding()
					.dismissKeyboardOnScroll(focusManager)
					.verticalScroll(rememberScrollState())
			)

		state.hasRemoteError ->
			Column(
				modifier = Modifier
					.weight(1f)
					.imePadding()
					.dismissKeyboardOnScroll(focusManager)
					.verticalScroll(rememberScrollState())
			) {
				SubjectSearchError(
					onRetryClick = onRetryClick
				)
			}

		else ->
			SubjectSearchResults(
				query = state.query,
				results = state.results,
				isRefreshing = state.isRefreshing,
				onSubjectClick = onSubjectClick,
				onResultsInteraction = onDismissKeyboard,
				modifier = Modifier
					.weight(1f)
					.imePadding()
			)
	}
}
