package com.gdavidpb.tuindice.summary.ui.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The head of an empty block that names what it lacks: the [icon] of the screen that has it, beside its [title].
 */
@Immutable
data class SummaryStatisticsEmptyHeading(
	val title: String,
	val icon: ImageVector
)
