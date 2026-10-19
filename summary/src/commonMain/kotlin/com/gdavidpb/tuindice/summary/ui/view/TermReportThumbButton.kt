package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.a11y_term_report_not_useful
import tuindice.summary.generated.resources.a11y_term_report_useful

// One thumb. The one the student chose is filled and in the accent, and the semantics say it is selected.
@Composable
fun TermReportThumbButton(
	useful: Boolean,
	isSelected: Boolean,
	onClick: () -> Unit
) {
	val tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

	IconButton(
		modifier = Modifier
			.size(TermReportThumbSizes.ButtonSize)
			.testTag(TermReportUiTags.termReportThumb(useful))
			.semantics { selected = isSelected },
		onClick = onClick
	) {
		Icon(
			modifier = Modifier.size(TermReportThumbSizes.IconSize),
			imageVector = thumbIconOf(useful = useful, isSelected = isSelected),
			contentDescription = stringResource(
				if (useful) Res.string.a11y_term_report_useful else Res.string.a11y_term_report_not_useful
			),
			tint = tint
		)
	}
}

private fun thumbIconOf(useful: Boolean, isSelected: Boolean): ImageVector {
	return when {
		useful && isSelected -> Icons.Filled.ThumbUp
		useful -> Icons.Outlined.ThumbUp
		isSelected -> Icons.Filled.ThumbDown
		else -> Icons.Outlined.ThumbDown
	}
}
