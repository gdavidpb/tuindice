package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.presentation.contract.Record

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecordContentView(
	state: Record.State.Content,
	onSubjectGradeChange: (subjectId: String, newGrade: Int, isSelected: Boolean) -> Unit
) {
	val subjectStates = rememberSubjectsStates(quarters = state.quarters)

	LazyColumn(
		modifier = Modifier
			.fillMaxSize()
	) {
		state.quarters.forEach { quarter ->
			stickyHeader {
				with(quarter) {
					QuarterHeaderView(
						name = name,
						grade = grade,
						gradeSum = gradeSum,
						credits = credits
					)
				}
			}

			items(quarter.subjects) { subject ->
				val subjectState = subjectStates[subject.id]?.value

				with(subject) {
					SubjectItemView(
						code = code,
						name = name,
						credits = credits,
						grade = subjectState ?: grade,
						isRetired = if (subjectState != null) (subjectState == MIN_SUBJECT_GRADE) else isRetired,
						isNoEffect = isNoEffect,
						isEditable = isEditable,
						onGradeChange = { newGrade, isSelected ->
							subjectStates[subject.id]?.value = newGrade

							if (isSelected)
								onSubjectGradeChange(subject.id, newGrade, true)
						}
					)
				}
			}
		}
	}
}