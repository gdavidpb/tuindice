package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.gdavidpb.tuindice.academiccore.domain.model.TermProjection
import com.gdavidpb.tuindice.academiccore.domain.model.isOfficialCurrent
import com.gdavidpb.tuindice.academiccore.domain.model.isOfficialHistorical
import com.gdavidpb.tuindice.academiccore.domain.model.isSynthetic
import com.gdavidpb.tuindice.base.utils.extension.academicTermShortDisplayName
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.presentation.model.TermMetricDelta
import com.gdavidpb.tuindice.record.presentation.model.TermMetricDeltaTone
import kotlin.math.abs

private const val TERM_DELTA_DECIMALS = 4
private const val ZERO_DELTA_TEXT = "0.0000"
private const val UP_DELTA_SYMBOL = "▲"
private const val DOWN_DELTA_SYMBOL = "▼"

@Composable
fun List<TermProjection>.toTermItemList(
	viewMode: RecordViewMode,
	texts: RecordMapperTexts,
	highlightColor: Color
) = mapIndexed { index, term ->
	term.toTermItem(
		viewMode = viewMode,
		texts = texts,
		highlightColor = highlightColor,
		previousTerm = getOrNull(index + 1)
	)
}

@Composable
fun TermProjection.toTermItem(
	viewMode: RecordViewMode,
	texts: RecordMapperTexts,
	highlightColor: Color,
	previousTerm: TermProjection? = null
): TermItem {
	val animatedGrade = animateFloatAsState(
		targetValue = periodAverage.toFloat(),
		label = "animatedTermGrade"
	)
	val animatedGradeSum = animateFloatAsState(
		targetValue = cumulativeAverage.toFloat(),
		label = "animatedTermGradeSum"
	)
	val animatedCredits = animateIntAsState(
		targetValue = periodCredits,
		label = "animatedTermCredits"
	)
	val isCurrent = isCurrentTerm()
	val canDelete = canDeleteTerm()
	val shouldShowDeltas = attempts.isNotEmpty()

	return TermItem(
		termId = id,
		shortNameText = academicTermShortDisplayName(
			startAtMillis = startAtMillis,
			endAtMillis = endAtMillis
		),
		gradeText = texts
			.termGrade(animatedGrade.value)
			.annotatedTermValue(highlightColor),
		gradeDelta = previousTerm?.takeIf { shouldShowDeltas }?.let { older ->
			(animatedGrade.value - older.periodAverage.toFloat()).toTermMetricDelta()
		},
		gradeSumText = texts
			.termGradeSum(animatedGradeSum.value)
			.annotatedTermValue(highlightColor),
		gradeSumDelta = previousTerm?.takeIf { shouldShowDeltas }?.let { older ->
			(animatedGradeSum.value - older.cumulativeAverage.toFloat()).toTermMetricDelta()
		},
		creditsText = texts
			.termCredits(animatedCredits.value)
			.annotatedTermValue(highlightColor),
		creditsDelta = previousTerm?.takeIf { shouldShowDeltas }?.let { older ->
			(animatedCredits.value - older.periodCredits).toCreditsTermMetricDelta()
		},
		isCurrent = isCurrent,
		canDelete = canDelete,
		attempts = attempts.map { attempt ->
			attempt.toAttemptItem(
				isReadOnly = isAttemptReadOnly(viewMode),
				texts = texts
			)
		}
	)
}

internal fun TermProjection.isCurrentTerm(): Boolean {
	return kind.isOfficialCurrent
}

internal fun TermProjection.canDeleteTerm(): Boolean {
	return kind.isSynthetic
}

internal fun TermProjection.isAttemptReadOnly(viewMode: RecordViewMode): Boolean {
	return (viewMode == RecordViewMode.Official) || kind.isOfficialHistorical
}

private fun Float.toTermMetricDelta(): TermMetricDelta {
	val magnitudeText = abs(toDouble()).formatGrade(decimals = TERM_DELTA_DECIMALS)
	return buildTermMetricDelta(
		magnitudeText = magnitudeText,
		tone = when {
			magnitudeText == ZERO_DELTA_TEXT -> TermMetricDeltaTone.Neutral
			this > 0f -> TermMetricDeltaTone.Positive
			else -> TermMetricDeltaTone.Negative
		},
		isPositive = this > 0f,
		isZero = magnitudeText == ZERO_DELTA_TEXT
	)
}

private fun Int.toCreditsTermMetricDelta(): TermMetricDelta {
	return buildTermMetricDelta(
		magnitudeText = abs(this).toString(),
		tone = TermMetricDeltaTone.Informational,
		isPositive = this > 0,
		isZero = this == 0
	)
}

private fun buildTermMetricDelta(
	magnitudeText: String,
	tone: TermMetricDeltaTone,
	isPositive: Boolean,
	isZero: Boolean
): TermMetricDelta {
	val prefix = when {
		isZero -> ""
		isPositive -> UP_DELTA_SYMBOL
		else -> DOWN_DELTA_SYMBOL
	}
	val text = if (prefix.isEmpty()) magnitudeText else "$prefix $magnitudeText"
	return TermMetricDelta(text = text, tone = tone)
}

@Composable
@ReadOnlyComposable
fun String.annotatedTermValue(highlightColor: Color) = buildAnnotatedString {
	val before = substringBefore(" ")
	val after = substringAfter(" ")

	withStyle(
		SpanStyle(
			color = highlightColor,
			fontWeight = FontWeight.Bold
		)
	) {
		append(before)
	}

	append(" $after")
}
