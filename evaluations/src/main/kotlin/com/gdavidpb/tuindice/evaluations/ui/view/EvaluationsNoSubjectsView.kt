package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.base.ui.view.EmptyView
import com.gdavidpb.tuindice.evaluations.R

@Composable
fun EvaluationsNoSubjectsView() {
	EmptyView(
		title = stringResource(id = R.string.title_no_subjects_evaluations),
		message = stringResource(id = R.string.message_no_subjects_evaluations)
	)
}