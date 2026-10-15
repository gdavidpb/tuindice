package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryEvaluationRowItem

/**
 * The title line of an evaluation row: the icon of its type, its name and the chip of its subject in one flow, and
 * at the end the [SummaryEvaluationStatusChip] of its state when the evaluation has a state to show. The state stays
 * on the first line of the title, even when the subject goes under the name. The mapper leaves the state out for a
 * scheduled evaluation, so the line then holds only the name and the subject.
 */
@Composable
fun SummaryEvaluationTitleLine(item: SummaryEvaluationRowItem) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
		verticalAlignment = Alignment.Top
	) {
		SummaryTitleLine(
			modifier = Modifier.weight(1f),
			icon = item.typeIcon,
			iconTag = SummaryEvaluationRowUiTags.typeIcon(item.evaluationId),
			titleText = item.titleText.asString(),
			subjectCode = item.subjectCode
		)

		item.statusText?.let { statusText ->
			Box(modifier = Modifier.padding(top = StatusChipTopOffset)) {
				SummaryEvaluationStatusChip(item = item, text = statusText.asString())
			}
		}
	}
}

// The state chip is shorter than the subject chip beside the title: 4 dp down centers it on the first line.
private val StatusChipTopOffset = 4.dp
