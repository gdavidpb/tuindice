package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags

@Composable
fun TermsView(
	modifier: Modifier = Modifier,
	lazyListState: LazyListState,
	terms: List<TermItem>,
	onAttemptSelectionChange: (
		termId: String,
		attemptId: String,
		newGrade: Int,
		isSelected: Boolean
	) -> Unit
) {
	TermsView(
		modifier = modifier,
		lazyListState = lazyListState,
		terms = terms,
		onAttemptSelectionChange = { termId, attemptId, newGrade, _, isSelected ->
			onAttemptSelectionChange(termId, attemptId, newGrade ?: 0, isSelected)
		}
	)
}

@Composable
fun TermsView(
	modifier: Modifier = Modifier,
	lazyListState: LazyListState,
	terms: List<TermItem>,
	onAttemptSelectionChange: (
		termId: String,
		attemptId: String,
		newGrade: Int?,
		newStatus: SubjectStatus?,
		isSelected: Boolean
	) -> Unit
) {
	LazyColumn(
		modifier = modifier.testTag(RecordUiTags.TermsList),
		state = lazyListState
	) {
		itemsIndexed(
			items = terms,
			key = { _, term -> term.termId }
		) { index, term ->
			TermItemView(
				modifier = Modifier.testTag(RecordUiTags.termItem(index)),
				item = term,
				onAttemptSelectionChange = onAttemptSelectionChange
			)
		}
	}
}
