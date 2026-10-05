package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.ui.RecordUiTags

/** What the university flagged on the enrollment of a subject, as a line under its row. */
@Composable
fun ScheduleTableErrorView(
	modifier: Modifier = Modifier,
	attemptId: String,
	text: String
) {
	val color = AcademicStatusColors.warning()

	Row(
		modifier = modifier.testTag(RecordUiTags.scheduleTableError(attemptId)),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall),
		verticalAlignment = Alignment.CenterVertically
	) {
		// Decorative: the text beside it carries the meaning.
		Icon(
			modifier = Modifier.size(TuIndiceComponentSizes.IconSmall),
			imageVector = Icons.Outlined.WarningAmber,
			contentDescription = null,
			tint = color
		)

		Text(
			text = text,
			style = MaterialTheme.typography.labelMedium,
			color = color,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)
	}
}
