package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.record.presentation.contract.Record

@Composable
fun RecordContentView(
	state: Record.State.Content,
	onSubjectGradeChanged: (subjectId: String, newGrade: Int, isSelected: Boolean) -> Unit
) {
	val subjectStates = rememberSubjectsStates(quarters = state.quarters)

	LazyColumn(
		modifier = Modifier
			.fillMaxSize()
	) {
		items(state.quarters) { quarter ->
			with(quarter) {
				QuarterItemView(
					name = name,
					grade = grade,
					gradeSum = gradeSum,
					credits = credits,
					subjects = quarter.subjects,
					getSubjectGrade = { subject ->
						subjectStates[subject.id]?.intValue ?: subject.grade
					},
					onSubjectGradeChanged = { subject, grade, isSelected ->
						subjectStates[subject.id]?.intValue = grade

						if (isSelected)
							onSubjectGradeChanged(subject.id, grade, true)
					}
				)
			}
		}
	}
}