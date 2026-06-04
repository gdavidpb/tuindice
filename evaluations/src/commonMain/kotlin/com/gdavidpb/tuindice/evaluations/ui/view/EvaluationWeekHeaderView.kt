package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags

@Composable
fun EvaluationWeekHeaderView(
	weekKey: EvaluationsWeekKey,
	label: String,
	countText: String
) {
	EvaluationHeaderView(
		modifier = Modifier.testTag(EvaluationsUiTags.evaluationsWeekHeader(weekKey)),
		label = label,
		countText = countText
	)
}
