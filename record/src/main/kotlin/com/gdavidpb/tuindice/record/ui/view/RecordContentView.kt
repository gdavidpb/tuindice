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
	onSubjectGradeChange: (
		quarterId: String,
		subjectId: String,
		newGrade: Int,
		isSelected: Boolean
	) -> Unit
) {
	LazyColumn(
		modifier = Modifier
			.fillMaxSize()
	) {
		items(
			items = state.quarters,
			key = { quarter -> quarter.id }
		) { quarter ->
			with(quarter) {
				QuarterItemView(
					name = name,
					grade = grade,
					gradeSum = gradeSum,
					credits = credits,
					subjects = quarter.subjects,
					onSubjectGradeChange = { subject, grade, isSelected ->
						onSubjectGradeChange(subject.qid, subject.id, grade, isSelected)
					}
				)
			}
		}
	}
}