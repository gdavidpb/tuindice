package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem

@Composable
fun QuartersView(
	lazyListState: LazyListState,
	quarters: List<QuarterItem>,
	onSubjectGradeChange: (
		quarterId: String,
		subjectId: String,
		newGrade: Int,
		isSelected: Boolean
	) -> Unit
) {
	LazyColumn(
		state = lazyListState
	) {
		items(
			items = quarters,
			key = { quarter -> quarter.quarterId }
		) { quarter ->
			QuarterItemView(
				item = quarter,
				onSubjectGradeChange = onSubjectGradeChange
			)
		}
	}
}