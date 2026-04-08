package com.gdavidpb.tuindice.record.ui.view

import com.gdavidpb.tuindice.base.domain.model.subject.GradingMode
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
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
import com.gdavidpb.tuindice.record.presentation.model.TermItem

@Composable
fun TermItemView(
	modifier: Modifier = Modifier,
	item: TermItem,
	onAttemptSelectionChange: (
		termId: String,
		attemptId: String,
		newGrade: Int,
		isSelected: Boolean
	) -> Unit
) {
	TermItemView(
		modifier = modifier,
		item = item,
		onAttemptSelectionChange = { termId, attemptId, newGrade, _, isSelected ->
			onAttemptSelectionChange(termId, attemptId, newGrade ?: 0, isSelected)
		}
	)
}

@Composable
fun TermItemView(
	modifier: Modifier = Modifier,
	item: TermItem,
	onAttemptSelectionChange: (
		termId: String,
		attemptId: String,
		newGrade: Int?,
		newStatus: SubjectStatus?,
		isSelected: Boolean
	) -> Unit
) {
	val gradeStates = remember(
		item.termId,
		item.attempts.map { attempt -> attempt.attemptId to attempt.grade }
	) {
		HashMap(
			item.attempts.associate { attempt ->
				attempt.attemptId to mutableIntStateOf(attempt.grade)
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
				TermSummaryContent(item = item)

				item.attempts.forEach { attempt ->
					key(attempt.attemptId) {
						val gradeState = gradeStates.getOrPut(attempt.attemptId) {
							mutableIntStateOf(attempt.grade)
						}

						AttemptItemView(
							item = attempt,
							gradeState = gradeState.takeIf { attempt.gradingMode == GradingMode.NUMERIC },
							onGradeChange = { newGrade, newStatus, isSelected ->
								onAttemptSelectionChange(
									attempt.termId,
									attempt.attemptId,
									newGrade,
									newStatus,
									isSelected
								)
							}
						)
					}
				}
			}
		}
	}
