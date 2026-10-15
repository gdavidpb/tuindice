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
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryClassRowItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_summary_open_schedule

/**
 * The class in progress or next, first row of the week, drawn in the shape of an evaluation row: its icon and its title
 * ("CLASE") first, and under them the chip of its subject with its blocks, its room and its hour beside it. Tapping it
 * opens the Schedule sheet; it announces itself as one thing and says where the tap leads.
 */
@Composable
fun SummaryClassRowView(
	item: SummaryClassRowItem,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	SummaryEntryView(
		modifier = modifier
			.fillMaxWidth()
			.clickable(
				onClickLabel = stringResource(Res.string.a11y_summary_open_schedule),
				role = Role.Button,
				onClick = onClick
			)
			.heightIn(min = SummaryRowMinHeight)
			.padding(horizontal = TuIndiceSpacing.Screen, vertical = TuIndiceSpacing.Large),
		lead = {
			SummaryPillView(
				modifier = Modifier
					.alignBy(FirstBaseline)
					.testTag(SummaryUiTags.WeekClassPill),
				text = item.leadText.asString(),
				isAccent = item.isInProgress
			)
		},
		titleLine = {
			SummaryTitleLine(
				icon = item.icon,
				iconTag = SummaryClassRowUiTags.TitleIcon,
				titleText = item.titleText.asString()
			)
		},
		subjectCode = item.subjectCode,
		place = item.place
	)
}

/** The height a row of the week has at least, and the one a row of a card is tall enough to tap. */
internal val SummaryRowMinHeight = 56.dp
