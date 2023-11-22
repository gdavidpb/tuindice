package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.mapper.toQuarterItemList

@Composable
fun RecordContentView(
	state: Record.State.Content,
	onSubjectGradeChange: (
		quarterId: String,
		subjectId: String,
		newGrade: Int,
		isSelected: Boolean
	) -> Unit
) {
	val lazyColumState = rememberLazyListState()

	val quarters = state
		.quarters
		.toQuarterItemList()

	QuartersView(
		lazyListState = lazyColumState,
		quarters = quarters,
		onSubjectGradeChange = onSubjectGradeChange
	)
}