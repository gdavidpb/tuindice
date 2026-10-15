package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.summary.presentation.model.SummaryEvaluationRowItem

/**
 * The title line of an evaluation row: the icon of its type and its name, with the [SummaryEvaluationStatusChip] of its
 * state at the end when the evaluation has a state to show. The mapper leaves the state out for a scheduled evaluation,
 * so the line then holds only the name.
 */
@Composable
fun SummaryEvaluationTitleLine(item: SummaryEvaluationRowItem) {
	SummaryTitleLine(
		icon = item.typeIcon,
		iconTag = SummaryEvaluationRowUiTags.typeIcon(item.evaluationId),
		titleText = item.titleText.asString(),
		trailing = {
			item.statusText?.let { statusText ->
				SummaryEvaluationStatusChip(item = item, text = statusText.asString())
			}
		}
	)
}
