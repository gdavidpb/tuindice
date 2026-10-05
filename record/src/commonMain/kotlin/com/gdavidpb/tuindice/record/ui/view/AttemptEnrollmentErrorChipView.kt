package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.ui.RecordUiTags

/** What the university flagged on the enrollment of a subject, in the alert tone of the load chip. */
@Composable
fun AttemptEnrollmentErrorChipView(
	modifier: Modifier = Modifier,
	attemptId: String,
	text: String
) {
	val color = AcademicStatusColors.warning()

	Surface(
		modifier = modifier.testTag(RecordUiTags.attemptEnrollmentError(attemptId)),
		shape = RoundedCornerShape(TuIndiceRadius.Medium),
		color = color.copy(alpha = 0.12f),
		border = BorderStroke(
			width = TuIndiceSpacing.Hairline,
			color = color.copy(alpha = 0.9f)
		)
	) {
		Row(
			modifier = Modifier.padding(
				horizontal = TuIndiceSpacing.Medium,
				vertical = TuIndiceSpacing.XSmall
			),
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
}
