package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.formatAsDayOfWeekAndDate
import com.gdavidpb.tuindice.evaluations.presentation.mapper.formatAsToNow
import com.gdavidpb.tuindice.evaluations.presentation.mapper.iconRes
import com.gdavidpb.tuindice.evaluations.presentation.mapper.stringRes

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EvaluationsView(
	evaluations: List<Evaluation>,
	lazyListState: LazyListState,
	onEvaluationClick: (evaluationId: String) -> Unit
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

				EvaluationItemView(
					modifier = Modifier
						.clickable {
							onEvaluationClick(evaluation.evaluationId)
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
					isCompleted = evaluation.isCompleted,
					isCompletedChange = { isCompleted -> TODO() }
				)
			}
		}
	}
}