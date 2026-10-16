package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.model.SubjectCodeChipVariant
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.SubjectCodeChip

/**
 * The first line of a week row: the icon of what the row is, then its title in capitals and the chip of the
 * subject in one flow. The chip goes under the title only when it does not fit beside it, and the title keeps
 * the width the icon leaves it, so it wraps instead of being squeezed; the chip is never cut.
 */
@Composable
fun SummaryTitleLine(
	icon: ImageVector,
	iconTag: String,
	titleText: String,
	subjectCode: String,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			modifier = Modifier
				.testTag(iconTag)
				.size(TitleIconSize),
			imageVector = icon,
			tint = MaterialTheme.colorScheme.onBackground,
			contentDescription = null
		)

		FlowRow(
			modifier = Modifier
				.weight(1f)
				.padding(start = TuIndiceSpacing.Medium),
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall),
			itemVerticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = titleText,
				style = MaterialTheme.typography.titleSmall,
				color = MaterialTheme.colorScheme.onBackground
			)

			SubjectCodeChip(
				subjectCode = subjectCode,
				variant = SubjectCodeChipVariant.Dense
			)
		}
	}
}

private val TitleIconSize = 18.dp
