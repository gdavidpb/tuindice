package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.mapper.dateGroup
import com.gdavidpb.tuindice.evaluations.presentation.mapper.dateLabel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EvaluationsContentView(
	state: Evaluations.State.Content,
	onAddEvaluationClick: () -> Unit
) {
	val evaluationsByDate =
		state.filteredEvaluations.groupBy { evaluation ->
			evaluation.date.dateGroup()
		}

	Box(
		modifier = Modifier
			.fillMaxSize()
	) {
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
		) {
			evaluationsByDate.forEach { (group, evaluations) ->
				stickyHeader {
					EvaluationHeaderView(label = group)
				}

				items(evaluations) { evaluation ->
					EvaluationItemView(
						name = evaluation.name,
						subjectCode = evaluation.subjectCode,
						date = evaluation.date.dateLabel(),
						type = stringResource(id = evaluation.type.stringRes),
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

		FloatingActionButton(
			modifier = Modifier
				.align(Alignment.BottomEnd)
				.padding(dimensionResource(id = R.dimen.dp_24)),
			onClick = onAddEvaluationClick
		) {
			Icon(
				imageVector = Icons.Outlined.Add,
				contentDescription = null
			)
		}
	}
}