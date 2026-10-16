package com.gdavidpb.tuindice.base.ui.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * One segment of a progress bar: the [fraction] of the bar it takes, between nothing and all of it, its [color],
 * and the [tag] that names its drawn piece for a test, when it has one.
 */
@Immutable
data class TuIndiceProgressSegment(
	val fraction: Float,
	val color: Color,
	val tag: String? = null
)
