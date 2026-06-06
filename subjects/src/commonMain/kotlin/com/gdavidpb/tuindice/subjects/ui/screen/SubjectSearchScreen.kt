package com.gdavidpb.tuindice.subjects.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
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

	LaunchedEffect(Unit) {
		focusRequester.requestFocus()
	}

	fun dismissKeyboard() {
		focusManager.clearFocus()
	}

	Column(
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
			.imePadding()
			.padding(horizontal = 20.dp, vertical = 20.dp)
			.testTag(SubjectsUiTags.SearchScreen)
	) {
		SubjectSearchTextField(
			query = state.query,
			focusRequester = focusRequester,
			onQueryChange = onQueryChange,
			onClearClick = onClearClick,
			onSearch = ::dismissKeyboard
		)
		Spacer(modifier = Modifier.height(20.dp))

		when {
			state.query.trim().length < 2 ->
				SubjectSearchGuidanceView(
					query = state.query,
					onExampleClick = onQueryChange,
					modifier = Modifier.weight(1f)
				)

			state.hasRemoteError ->
				SubjectSearchError(
					onRetryClick = onRetryClick
				)

			else ->
				SubjectSearchResults(
					query = state.query,
					results = state.results,
					isRefreshing = state.isRefreshing,
					onSubjectClick = { subjectCode ->
						dismissKeyboard()
						onSubjectClick(subjectCode)
					},
					onResultsInteraction = ::dismissKeyboard,
					modifier = Modifier.weight(1f)
				)
		}
	}
}
