package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.formatAsDayOfWeekAndDate
import com.gdavidpb.tuindice.evaluations.presentation.mapper.formatAsToNow
import com.gdavidpb.tuindice.evaluations.presentation.mapper.iconRes
import com.gdavidpb.tuindice.evaluations.presentation.mapper.stringRes
import com.gdavidpb.tuindice.evaluations.utils.THRESHOLD_EVALUATION_SWIPE

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EvaluationsView(
	evaluations: List<Evaluation>,
	lazyListState: LazyListState,
	onEvaluationClick: (evaluationId: String) -> Unit,
	onEvaluationDelete: (evaluationId: String) -> Unit,
	onEvaluationIsCompletedChange: (evaluationId: String, isCompleted: Boolean) -> Unit
) {
	val evaluationsByDate =
		evaluations.groupBy { evaluation ->
			evaluation.date.formatAsToNow()
		}

	LazyColumn(
		state = lazyListState
	) {
		evaluationsByDate.forEach { (group, evaluations) ->
			stickyHeader {
				EvaluationHeaderView(label = group)
			}

			items(evaluations) { evaluation ->
				// TODO evaluation presentation model
				val typeName = stringResource(id = evaluation.type.stringRes())
				val evaluationName = "$typeName ${evaluation.ordinal}"

				val currentFraction = remember { mutableFloatStateOf(0f) }

				val dismissState = rememberDismissState(
					positionalThreshold = { _ -> 0f },
					confirmValueChange = { confirmValue ->
						when (confirmValue) {
							DismissValue.DismissedToEnd ->
								onEvaluationIsCompletedChange(
									evaluation.evaluationId,
									!evaluation.isCompleted
								)

							DismissValue.DismissedToStart ->
								onEvaluationDelete(
									evaluation.evaluationId
								)

							else -> {}
						}

						currentFraction.floatValue > THRESHOLD_EVALUATION_SWIPE
					}
				)

				currentFraction.floatValue = dismissState.progress

				EvaluationSwipeToDismiss(
					isCompleted = evaluation.isCompleted,
					state = dismissState
				) {
					EvaluationItemView(
						modifier = Modifier
							.clickable {
								onEvaluationClick(
									evaluation.evaluationId
								)
							}
							.animateItemPlacement(),
						name = evaluationName,
						subjectCode = evaluation.subject.code,
						date = evaluation.date.formatAsDayOfWeekAndDate(),
						type = typeName,
						icon = evaluation.type.iconRes(),
						grade = evaluation.grade,
						maxGrade = evaluation.maxGrade,
						isContinuous = evaluation.isContinuous,
						isNotGraded = evaluation.isNotGraded,
						isCompleted = evaluation.isCompleted
					)
				}
			}
		}
	}
}