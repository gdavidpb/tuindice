package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.mapper.formatAsDayOfWeekAndDate
import com.gdavidpb.tuindice.evaluations.presentation.mapper.formatAsToNow
import com.gdavidpb.tuindice.evaluations.presentation.mapper.iconRes
import com.gdavidpb.tuindice.evaluations.presentation.mapper.stringRes

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EvaluationsContentView(
	state: Evaluations.State.Content,
	onAddEvaluationClick: () -> Unit,
	onEvaluationClick: (evaluationId: String) -> Unit
) {
	val evaluationsByDate = remember {
		state.filteredEvaluations.groupBy { evaluation ->
			evaluation.date.formatAsToNow()
		}
	}

	val lazyColumState = rememberLazyListState()

	Box(
		modifier = Modifier
			.fillMaxSize()
	) {
		LazyColumn(
			modifier = Modifier
				.fillMaxSize(),
			state = lazyColumState
		) {
			evaluationsByDate.forEach { (group, evaluations) ->
				stickyHeader {
					EvaluationHeaderView(label = group)
				}

				items(evaluations) { evaluation ->
					EvaluationItemView(
						modifier = Modifier
							.clickable {
								onEvaluationClick(evaluation.evaluationId)
							}
							.animateItemPlacement(),
						name = evaluation.name,
						subjectCode = evaluation.subject.code,
						date = evaluation.date.formatAsDayOfWeekAndDate(),
						type = stringResource(id = evaluation.type.stringRes()),
						icon = evaluation.type.iconRes(),
						grade = evaluation.grade,
						maxGrade = evaluation.maxGrade,
						isContinuous = evaluation.isContinuous,
						isNotGraded = evaluation.isNotGraded,
						isCompleted = evaluation.isCompleted,
						isCompletedChanged = { isCompleted -> TODO() }
					)
				}
			}
		}

		AnimatedVisibility(
			modifier = Modifier
				.align(Alignment.BottomEnd)
				.padding(dimensionResource(id = R.dimen.dp_24)),
			visible = !lazyColumState.isScrollInProgress,
			enter = fadeIn(),
			exit = fadeOut()
		) {
			FloatingActionButton(
				onClick = onAddEvaluationClick
			) {
				Icon(
					imageVector = Icons.Outlined.Add,
					contentDescription = null
				)
			}
		}
	}
}