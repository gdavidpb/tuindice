package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem

@Composable
fun QuarterItemView(
	modifier: Modifier = Modifier,
	item: QuarterItem,
	onSubjectGradeChange: (
		quarterId: String,
		subjectId: String,
		newGrade: Int,
		isSelected: Boolean
	) -> Unit
) {
	ElevatedCard(
		modifier = modifier
			.fillMaxWidth()
			.padding(
				horizontal = 16.dp,
				vertical = 8.dp
			)
	) {
		Column(
			modifier = Modifier
				.padding(8.dp)
		) {
			Text(
				modifier = Modifier
					.fillMaxWidth()
					.padding(8.dp),
				text = item.nameText,
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Black
			)

			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 4.dp),
				horizontalArrangement = Arrangement.SpaceAround
			) {
				Text(
					text = item.gradeText,
					style = MaterialTheme.typography.titleMedium
				)
				Text(
					text = item.gradeSumText,
					style = MaterialTheme.typography.titleMedium
				)
				Text(
					text = item.creditsText,
					style = MaterialTheme.typography.titleMedium
				)
			}

			item.subjects.forEach { subject ->
				SubjectItemView(
					item = subject,
					onGradeChange = { newGrade, isSelected ->
						item.states[subject.subjectId]?.intValue = newGrade

						onSubjectGradeChange(
							subject.quarterId,
							subject.subjectId,
							newGrade,
							isSelected
						)
					}
				)
			}
		}
	}
}
