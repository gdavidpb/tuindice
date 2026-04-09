package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags

@Composable
fun TermItemView(
	modifier: Modifier = Modifier,
	item: TermItem,
	onAttemptSelectionChange: (
		termId: String,
		attemptId: String,
		newGrade: Int?,
		newStatus: SubjectStatus?,
		isSelected: Boolean
	) -> Unit
) {
	Column(modifier = modifier.fillMaxSize()) {
		TermSummaryView(
			modifier = Modifier
				.fillMaxWidth()
				.testTag(RecordUiTags.SelectedTermSummary),
			item = item
		)

		SelectedTermView(
			modifier = Modifier
				.fillMaxWidth()
				.weight(1f),
			term = item,
			onAttemptSelectionChange = onAttemptSelectionChange
		)
	}
}
