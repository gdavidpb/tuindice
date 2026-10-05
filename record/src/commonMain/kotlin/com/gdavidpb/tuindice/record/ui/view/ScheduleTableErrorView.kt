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
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_clash

/**
 * "Choque de horario", as a line under the row of a subject that overlaps another. The label is the
 * app's own and names no subject: the university flags only one side of a clash, in its own words,
 * and those stay on the subject's card in the record.
 */
@Composable
fun ScheduleTableErrorView(
	modifier: Modifier = Modifier,
	attemptId: String
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
			text = stringResource(Res.string.schedule_clash),
			style = MaterialTheme.typography.labelMedium,
			color = color,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)
	}
}
