package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import com.gdavidpb.tuindice.record.presentation.mapper.annotatedTermValue
import com.gdavidpb.tuindice.record.presentation.mapper.toCreditsTermMetricDelta
import com.gdavidpb.tuindice.record.presentation.mapper.toTermMetricDelta
import com.gdavidpb.tuindice.record.presentation.model.TermMetricDelta

// A grade of a term, animated toward [target]. [content] is handed what it shows: the value, highlighted, and the
// change from [previousTarget] when one is shown. Only this composable reads the animated value, so a frame of the
// animation recomposes it and nothing above it.
@Composable
fun AnimatedTermGradeMetric(
	target: Float,
	previousTarget: Float? = null,
	format: (Float) -> String,
	label: String,
	content: @Composable (value: AnnotatedString, delta: TermMetricDelta?) -> Unit
) {
	val grade = animateFloatAsState(
		targetValue = target,
		label = label
	).value

	content(
		format(grade).annotatedTermValue(MaterialTheme.colorScheme.primary),
		previousTarget?.let { older -> (grade - older).toTermMetricDelta() }
	)
}

// The credits of a term, animated toward [target]: as [AnimatedTermGradeMetric], for whole credits.
@Composable
fun AnimatedTermCreditsMetric(
	target: Int,
	previousTarget: Int? = null,
	format: (Int) -> String,
	label: String,
	content: @Composable (value: AnnotatedString, delta: TermMetricDelta?) -> Unit
) {
	val credits = animateIntAsState(
		targetValue = target,
		label = label
	).value

	content(
		format(credits).annotatedTermValue(MaterialTheme.colorScheme.primary),
		previousTarget?.let { older -> (credits - older).toCreditsTermMetricDelta() }
	)
}
