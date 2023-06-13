package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.mapper.dateLabel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EvaluationsContentView(
	state: Evaluations.State.Content
) {
	LazyColumn(
		modifier = Modifier
			.fillMaxSize()
	) {
		state.evaluations.forEach { (group, evaluations) ->
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
					isAttentionRequired = evaluation.isAttentionRequired,
					isCompleted = evaluation.isCompleted,
					isCompletedChange = { isCompleted -> }
				)
			}
		}
	}
}