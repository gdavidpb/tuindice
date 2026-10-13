package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes

/**
 * The icon that says "an evaluation falls here" on the schedule: the table's cell, the week grid's
 * block and the legend all draw this one, the same assignment icon the Evaluations tab wears. It is
 * decorative: whoever draws it says in words what it means.
 */
@Composable
fun ScheduleEvaluationMarkIcon(
	modifier: Modifier = Modifier,
	tint: Color = LocalContentColor.current
) {
	Icon(
		modifier = modifier.size(TuIndiceComponentSizes.IconSmall),
		imageVector = Icons.AutoMirrored.Outlined.Assignment,
		contentDescription = null,
		tint = tint
	)
}
