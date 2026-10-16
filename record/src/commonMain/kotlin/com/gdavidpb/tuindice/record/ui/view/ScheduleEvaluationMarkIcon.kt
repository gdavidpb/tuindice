package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes

/**
 * The icon that says "an evaluation falls here" on the schedule: the table's cell, the week grid's
 * block and the legend all draw this one, the same assignment icon the Evaluations tab wears. It is
 * decorative: whoever draws it says in words what it means. [size] is the token's 16dp everywhere
 * but a week-grid block of a single block, whose 70dp leave no room for it beside the whole range of
 * real subject codes and which asks for the compact one.
 */
@Composable
fun ScheduleEvaluationMarkIcon(
	modifier: Modifier = Modifier,
	tint: Color = LocalContentColor.current,
	size: Dp = TuIndiceComponentSizes.IconSmall
) {
	Icon(
		modifier = modifier.size(size),
		imageVector = Icons.AutoMirrored.Outlined.Assignment,
		contentDescription = null,
		tint = tint
	)
}
