package com.gdavidpb.tuindice.summary.ui.view

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
 * The first line of a week row: the icon of what the row is, its title in capitals and, beside the
 * title, the chip of the subject. The title takes the room the icon and the chip leave and wraps
 * instead of being cut; the chip is never cut.
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

		Text(
			modifier = Modifier
				.weight(1f, fill = false)
				.padding(start = TuIndiceSpacing.Medium),
			text = titleText,
			style = MaterialTheme.typography.titleSmall,
			color = MaterialTheme.colorScheme.onBackground
		)

		SubjectCodeChip(
			modifier = Modifier.padding(start = TuIndiceSpacing.Medium),
			subjectCode = subjectCode,
			variant = SubjectCodeChipVariant.Dense
		)
	}
}

private val TitleIconSize = 18.dp
