package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.base.ui.view.EmptyView
import com.gdavidpb.tuindice.evaluations.R

@Composable
fun EvaluationsEmptyView(
	onAddEvaluationClick: () -> Unit
) {
	EmptyView(
		title = stringResource(id = R.string.title_empty_evaluations),
		message = stringResource(id = R.string.message_empty_evaluations),
		action = stringResource(id = R.string.button_add_evaluation),
		onActionClick = onAddEvaluationClick
	)
}