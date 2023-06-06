package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.utils.MIN_SUBJECT_GRADE

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecordContentView(
	state: Record.State.Content,
	onSubjectGradeChange: (subjectId: String, newGrade: Int, isSelected: Boolean) -> Unit
) {
	val subjectStates = rememberSubjectsStates(quarters = state.quarters)

	LazyColumn {
		state.quarters.forEach { quarter ->
			stickyHeader {
				with(quarter) {
					QuarterView(
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
					SubjectView(
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