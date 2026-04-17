package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.record.presentation.model.AttemptItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags

@Composable
fun AttemptCardItemView(
	modifier: Modifier = Modifier,
	item: AttemptItem,
	gradeState: MutableIntState? = null,
	onSubjectClick: (subjectCode: String) -> Unit = {},
	onGradeChange: (newGrade: Int?, newOutcome: AttemptOutcome?, isSelected: Boolean) -> Unit
) {
	ElevatedCard(
		modifier = modifier
			.fillMaxWidth()
			.padding(
				horizontal = 16.dp,
				vertical = 6.dp
			)
			.testTag(RecordUiTags.attemptCard(item.attemptId))
	) {
		AttemptItemView(
			item = item,
			gradeState = gradeState,
			onSubjectClick = onSubjectClick,
			onGradeChange = onGradeChange
		)
	}
}
