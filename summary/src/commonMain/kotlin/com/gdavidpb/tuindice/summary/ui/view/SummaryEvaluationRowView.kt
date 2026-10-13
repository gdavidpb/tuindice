package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryEvaluationRowItem
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_summary_open_evaluations_week

/**
 * An evaluation of the week: its day at the start, then its name, the chip of its subject and where
 * its subject has class that day (blocks and room, never the hour). Tapping it opens the
 * Evaluations tab on its week.
 */
@Composable
fun SummaryEvaluationRowView(
	item: SummaryEvaluationRowItem,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.clickable(
				onClickLabel = stringResource(Res.string.a11y_summary_open_evaluations_week),
				role = Role.Button,
				onClick = onClick
			)
			.heightIn(min = SummaryRowMinHeight)
			.padding(horizontal = TuIndiceSpacing.Screen, vertical = TuIndiceSpacing.Large),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge),
		verticalAlignment = Alignment.CenterVertically
	) {
		SummaryLeadTextView(text = item.leadText.asString())

		SummaryEntryView(
			modifier = Modifier.weight(1f),
			titleText = item.titleText.asString(),
			subjectCode = item.subjectCode,
			place = item.place
		)
	}
}
