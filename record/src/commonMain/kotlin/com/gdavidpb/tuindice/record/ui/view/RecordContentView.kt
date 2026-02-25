package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.mapper.RecordMapperTexts
import com.gdavidpb.tuindice.record.presentation.mapper.toQuarterItemList

@Composable
fun RecordContentView(
	state: Record.State.Content,
	texts: RecordMapperTexts,
	highlightColor: Color,
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
		.toQuarterItemList(
			texts = texts,
			highlightColor = highlightColor
		)

	QuartersView(
		lazyListState = lazyColumState,
		quarters = quarters,
		onSubjectGradeChange = onSubjectGradeChange
	)
}
