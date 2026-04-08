package com.gdavidpb.tuindice.record.ui.view

import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags

@Composable
fun SelectedQuarterView(
	modifier: Modifier = Modifier,
	quarter: QuarterItem,
	onSubjectGradeChange: (
		quarterId: String,
		subjectId: String,
		newGrade: Int?,
		newStatus: SubjectStatus?,
		isSelected: Boolean
	) -> Unit
) {
	val lazyListState = rememberLazyListState()
	val gradeStates = remember(
		quarter.quarterId,
		quarter.subjects.map { subject -> subject.subjectId to subject.grade }
	) {
		HashMap(
			quarter.subjects.associate { subject ->
				subject.subjectId to mutableIntStateOf(subject.grade)
			}
		)
	}

	LaunchedEffect(quarter.quarterId) {
		lazyListState.scrollToItem(0)
	}

	LazyColumn(
		modifier = modifier.testTag(RecordUiTags.SubjectsList),
		state = lazyListState,
		contentPadding = PaddingValues(bottom = 16.dp)
	) {
		items(
			items = quarter.subjects,
			key = { subject -> subject.subjectId }
		) { subject ->
			val gradeState = gradeStates.getOrPut(subject.subjectId) {
				mutableIntStateOf(subject.grade)
			}

			SubjectCardItemView(
				item = subject,
				gradeState = gradeState.takeIf { subject.gradingMode == com.gdavidpb.tuindice.base.domain.model.subject.GradingMode.NUMERIC },
				onGradeChange = { newGrade, newStatus, isSelected ->
					onSubjectGradeChange(
						subject.quarterId,
						subject.subjectId,
						newGrade,
						newStatus,
						isSelected
					)
				}
			)
		}
	}
}
