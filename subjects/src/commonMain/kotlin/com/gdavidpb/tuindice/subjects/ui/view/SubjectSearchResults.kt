package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectSearchResultItem
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.subjects_search_no_results_message
import tuindice.subjects.generated.resources.subjects_search_no_results_title
import tuindice.subjects.generated.resources.subjects_search_results

@Composable
fun SubjectSearchResults(
	query: String,
	results: List<SubjectSearchResultItem>,
	isRefreshing: Boolean,
	onSubjectClick: (String) -> Unit,
	onResultsInteraction: () -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.pointerInput(onResultsInteraction) {
				awaitEachGesture {
					awaitFirstDown(requireUnconsumed = false)
					onResultsInteraction()
				}
			}
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			SubjectSearchMessage(
				title = if (results.isEmpty() && !isRefreshing) {
					stringResource(Res.string.subjects_search_no_results_title)
				} else {
					pluralStringResource(Res.plurals.subjects_search_results, results.size, results.size, query)
				},
				description = if (results.isEmpty() && !isRefreshing) {
					stringResource(Res.string.subjects_search_no_results_message, query)
				} else {
					null
				},
				modifier = Modifier.weight(1f)
			)
			if (isRefreshing) {
				CircularProgressIndicator(
					modifier = Modifier.size(18.dp),
					strokeWidth = 2.dp
				)
			}
		}
		Spacer(modifier = Modifier.height(12.dp))
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.testTag(SubjectsUiTags.SearchResults),
			contentPadding = PaddingValues(bottom = 80.dp),
			verticalArrangement = Arrangement.spacedBy(10.dp)
		) {
			items(
				items = results,
				key = SubjectSearchResultItem::subjectCode,
				contentType = { SubjectSearchResultContentType }
			) { item ->
				SubjectSearchResultCard(
					item = item,
					onClick = { onSubjectClick(item.subjectCode) }
				)
			}
		}
	}
}

private const val SubjectSearchResultContentType = "subject_search_result"
