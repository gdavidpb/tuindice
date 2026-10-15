package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryEvaluationRowItem

/**
 * The chip of the state of an evaluation row, with its [text], in the colors the Evaluations tab gives it: the error
 * tone when the evaluation is overdue, the neutral one otherwise. It is smaller than the one of Evaluations, because
 * the row has to hold the place as well.
 */
@Composable
fun SummaryEvaluationStatusChip(item: SummaryEvaluationRowItem, text: String) {
	val container = if (item.isOverdue) {
		MaterialTheme.colorScheme.errorContainer
	} else {
		MaterialTheme.colorScheme.surfaceDim
	}
	val content = if (item.isOverdue) {
		MaterialTheme.colorScheme.onErrorContainer
	} else {
		MaterialTheme.colorScheme.onSurface
	}

	Box(
		modifier = Modifier
			.testTag(SummaryEvaluationRowUiTags.statusChip(item.evaluationId))
			.background(color = container, shape = RoundedCornerShape(TuIndiceRadius.Small))
			.padding(horizontal = StatusHorizontalPadding, vertical = StatusVerticalPadding)
	) {
		Text(
			text = text,
			color = content,
			style = MaterialTheme.typography.labelSmall
		)
	}
}

private val StatusHorizontalPadding = TuIndiceSpacing.Medium
private val StatusVerticalPadding = 3.dp
