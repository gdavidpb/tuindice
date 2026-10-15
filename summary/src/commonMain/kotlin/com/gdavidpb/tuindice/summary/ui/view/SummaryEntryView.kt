package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.ui.model.SubjectCodeChipVariant
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.ClassPlaceLine
import com.gdavidpb.tuindice.base.ui.view.SubjectCodeChip

/**
 * What a row of the week is made of, in one shape for a class and for an evaluation: the [lead] at the start, centered
 * with the first line, and beside it the [titleLine], the icon and the title of the row with its state at the end when
 * it has one. Under the title line, starting where it starts, the chip of the subject and, beside the chip where the
 * subject has class that day, its place. What does not fit goes down to the next line, and nothing is cut.
 */
@Composable
fun SummaryEntryView(
	lead: @Composable () -> Unit,
	titleLine: @Composable () -> Unit,
	subjectCode: String,
	place: ClassPlaceItem?,
	modifier: Modifier = Modifier
) {
	Layout(
		modifier = modifier,
		content = {
			lead()
			titleLine()
			SummaryDetailsLine(subjectCode = subjectCode, place = place)
		}
	) { measurables, constraints ->
		measureEntry(measurables = measurables, constraints = constraints)
	}
}

/**
 * The first line of a week row: the icon of what the row is and its title in capitals, and at the end the [trailing]
 * content, which is the chip of the state. The title takes the room the icon and the chip leave, and wraps instead of
 * being cut.
 */
@Composable
internal fun SummaryTitleLine(
	icon: ImageVector,
	iconTag: String,
	titleText: String,
	modifier: Modifier = Modifier,
	trailing: @Composable () -> Unit = {}
) {
	Row(
		modifier = modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
		verticalAlignment = Alignment.CenterVertically
	) {
		Row(
			modifier = Modifier.weight(1f),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				modifier = Modifier
					.testTag(iconTag)
					.size(SummaryTitleIconSize),
				imageVector = icon,
				tint = MaterialTheme.colorScheme.onBackground,
				contentDescription = null
			)

			Text(
				modifier = Modifier.padding(start = TuIndiceSpacing.Medium),
				text = titleText,
				style = MaterialTheme.typography.titleSmall,
				color = MaterialTheme.colorScheme.onBackground
			)
		}

		trailing()
	}
}

// The subject's chip, and its place beside it when the subject has class: the place goes down to the next line as a
// whole when it does not fit beside the chip, and inside it the blocks and the room wrap as they always do.
@Composable
private fun SummaryDetailsLine(subjectCode: String, place: ClassPlaceItem?) {
	FlowRow(
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall),
		itemVerticalAlignment = Alignment.CenterVertically
	) {
		SubjectCodeChip(
			subjectCode = subjectCode,
			variant = SubjectCodeChipVariant.Dense
		)

		if (place != null) {
			ClassPlaceLine(place = place)
		}
	}
}

// The lead keeps its own width and the content starts after it. The first line is as tall as its taller part, and the
// lead is centered in it; the details go under the first line, starting where it starts. A row shorter than its minimum
// height keeps the block in its middle.
private fun MeasureScope.measureEntry(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
	val lead = measurables[LEAD_INDEX].measure(constraints.copy(minWidth = 0, minHeight = 0))
	val contentStart = lead.width + LeadGap.roundToPx()
	val contentConstraints = Constraints(maxWidth = (constraints.maxWidth - contentStart).coerceAtLeast(0))
	val title = measurables[TITLE_INDEX].measure(contentConstraints)
	val details = measurables[DETAILS_INDEX].measure(contentConstraints)

	val firstLineHeight = maxOf(lead.height, title.height)
	val detailsTop = firstLineHeight + TitleDetailsGap.roundToPx()
	val blockHeight = detailsTop + details.height
	val height = maxOf(blockHeight, constraints.minHeight)
	val blockTop = (height - blockHeight) / 2
	val width = (contentStart + maxOf(title.width, details.width)).coerceIn(constraints.minWidth, constraints.maxWidth)

	return layout(width, height) {
		lead.placeRelative(x = 0, y = blockTop + (firstLineHeight - lead.height) / 2)
		title.placeRelative(x = contentStart, y = blockTop + (firstLineHeight - title.height) / 2)
		details.placeRelative(x = contentStart, y = blockTop + detailsTop)
	}
}

// The slots of a week row, in the order its content is measured.
private const val LEAD_INDEX = 0
private const val TITLE_INDEX = 1
private const val DETAILS_INDEX = 2

// The sizes of the lines of a week row: the icon of the title line, the gap between the lead and the content, and the
// gap between the title line and the details under it.
private val SummaryTitleIconSize = 18.dp
private val LeadGap = TuIndiceSpacing.XLarge
private val TitleDetailsGap = TuIndiceSpacing.Medium
