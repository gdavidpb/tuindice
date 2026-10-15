package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryEvaluationRowItem
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_summary_open_evaluations_week

/**
 * An evaluation of the week, drawn the way its row reads in the Evaluations tab, in its compact form: the icon of its
 * type and its name first, with its state at the end of that line only when it has one (a scheduled evaluation has
 * none). Under them, the chip of its subject, with the place beside it where its subject has class that day (blocks and
 * room, with no hour). Its day leads the row, at the width of the pill of the class row and on the baseline of the
 * name; nothing is cut, and a long name wraps. Tapping it opens the Evaluations tab on its week.
 */
@Composable
fun SummaryEvaluationRowView(
	item: SummaryEvaluationRowItem,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	SummaryEntryView(
		modifier = modifier
			.fillMaxWidth()
			.clickable(
				onClickLabel = stringResource(Res.string.a11y_summary_open_evaluations_week),
				role = Role.Button,
				onClick = onClick
			)
			.heightIn(min = SummaryRowMinHeight)
			.padding(horizontal = TuIndiceSpacing.Screen, vertical = TuIndiceSpacing.Large),
		lead = {
			SummaryLeadTextView(
				modifier = Modifier
					.alignBy(FirstBaseline)
					.testTag(SummaryEvaluationRowUiTags.lead(item.evaluationId)),
				text = item.leadText.asString()
			)
		},
		titleLine = {
			SummaryEvaluationTitleLine(item = item)
		},
		subjectCode = item.subjectCode,
		place = item.place
	)
}
