package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
	val gradeStates = remember(
		item.quarterId,
		item.subjects.map { subject -> subject.subjectId to subject.grade }
	) {
		HashMap(
			item.subjects.associate { subject ->
				subject.subjectId to mutableIntStateOf(subject.grade)
			}
		)
	}

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
				QuarterSummaryContent(item = item)

				item.subjects.forEach { subject ->
					key(subject.subjectId) {
						val gradeState = gradeStates.getOrPut(subject.subjectId) {
							mutableIntStateOf(subject.grade)
						}

						SubjectItemView(
							item = subject,
							gradeState = gradeState,
							onGradeChange = { newGrade, isSelected ->
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
	}
