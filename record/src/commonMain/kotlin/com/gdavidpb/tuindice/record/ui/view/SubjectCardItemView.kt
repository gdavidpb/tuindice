package com.gdavidpb.tuindice.record.ui.view

import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags

@Composable
fun SubjectCardItemView(
	modifier: Modifier = Modifier,
	item: SubjectItem,
	gradeState: MutableIntState? = null,
	onGradeChange: (newGrade: Int?, newStatus: SubjectStatus?, isSelected: Boolean) -> Unit
) {
	ElevatedCard(
		modifier = modifier
			.fillMaxWidth()
			.padding(
				horizontal = 16.dp,
				vertical = 6.dp
			)
			.testTag(RecordUiTags.subjectCard(item.subjectId))
	) {
		SubjectItemView(
			item = item,
			gradeState = gradeState,
			onGradeChange = onGradeChange
		)
	}
}
