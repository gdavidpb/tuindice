package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags

@Composable
fun QuartersView(
	modifier: Modifier = Modifier,
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
		modifier = modifier.testTag(RecordUiTags.QuartersList),
		state = lazyListState
	) {
		itemsIndexed(
			items = quarters,
			key = { _, quarter -> quarter.quarterId }
		) { index, quarter ->
			QuarterItemView(
				modifier = Modifier.testTag(RecordUiTags.quarterItem(index)),
				item = quarter,
				onSubjectGradeChange = onSubjectGradeChange
			)
		}
	}
}
