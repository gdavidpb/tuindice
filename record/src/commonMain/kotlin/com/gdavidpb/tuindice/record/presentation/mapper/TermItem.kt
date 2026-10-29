package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.gdavidpb.tuindice.academiccore.domain.model.TermProjection
import com.gdavidpb.tuindice.academiccore.domain.model.isCurrent
import com.gdavidpb.tuindice.academiccore.domain.model.isHistorical
import com.gdavidpb.tuindice.academiccore.domain.model.isSynthetic
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.model.RecordNotice
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.presentation.model.TermItemKind
import com.gdavidpb.tuindice.record.presentation.model.TermMetricDelta
import com.gdavidpb.tuindice.record.presentation.model.TermMetricDeltaTone
import kotlin.math.abs

private const val TERM_DELTA_DECIMALS = 4
private const val ZERO_DELTA_TEXT = "0.0000"
private const val UP_DELTA_SYMBOL = "▲"
private const val DOWN_DELTA_SYMBOL = "▼"

fun List<TermProjection>.toTermItemList(
	viewMode: RecordViewMode,
	texts: RecordMapperTexts,
	notice: RecordNotice? = null,
	arrivalAttemptId: String? = null
) = mapIndexed { index, term ->
	term.toTermItem(
		viewMode = viewMode,
		texts = texts,
		previousTerm = getOrNull(index + 1),
		notice = notice
	).copy(
		// The arrival belongs to the term that lists its attempt, and to no other.
		arrivalAttemptId = arrivalAttemptId?.takeIf { attemptId ->
			term.attempts.any { attempt -> attempt.id == attemptId }
		}
	)
}

fun TermProjection.toTermItem(
	viewMode: RecordViewMode,
	texts: RecordMapperTexts,
	previousTerm: TermProjection? = null,
	notice: RecordNotice? = null
): TermItem {
	val isCurrent = isCurrentTerm()
	val canDelete = canDeleteTerm()
	val shouldShowDeltas = attempts.isNotEmpty()

	return TermItem(
		termId = id,
		periodYear = periodYear,
		termOrder = termOrder,
		shortNameText = toShortNameText(),
		kind = toTermItemKind(),
		gradeTarget = periodAverage.toFloat(),
		gradeSumTarget = cumulativeAverage.toFloat(),
		creditsTarget = periodCredits,
		previousGradeTarget = previousTerm?.takeIf { shouldShowDeltas }?.periodAverage?.toFloat(),
		previousGradeSumTarget = previousTerm?.takeIf { shouldShowDeltas }?.cumulativeAverage?.toFloat(),
		previousCreditsTarget = previousTerm?.takeIf { shouldShowDeltas }?.periodCredits,
		isCurrent = isCurrent,
		canDelete = canDelete,
		canEdit = canEditTerm(viewMode),
		attempts = attempts.map { attempt ->
			attempt.toAttemptItem(
				isReadOnly = isAttemptReadOnly(viewMode),
				texts = texts,
				termLabel = periodLabel
			)
		},
		// A provisional annulment and stale data describe the current term, so only its page
		// carries them.
		notice = notice?.takeIfOnTermPage(isCurrentTerm = isCurrent)
	)
}

internal fun TermProjection.toShortNameText(): String = "${periodCode.shortLabel} $periodYear"

private fun TermProjection.toTermItemKind(): TermItemKind = when {
	kind.isSynthetic -> TermItemKind.SYNTHETIC
	kind.isCurrent -> TermItemKind.CURRENT
	else -> TermItemKind.HISTORICAL
}

internal fun TermProjection.isCurrentTerm(): Boolean {
	return kind.isCurrent
}

internal fun TermProjection.canDeleteTerm(): Boolean {
	return kind.isSynthetic
}

internal fun TermProjection.canEditTerm(viewMode: RecordViewMode): Boolean {
	return viewMode == RecordViewMode.Projection && kind.isSynthetic
}

internal fun TermProjection.isAttemptReadOnly(viewMode: RecordViewMode): Boolean {
	return (viewMode == RecordViewMode.Historical) || kind.isHistorical
}

// The change of a grade against the older term's. The views that animate a grade build their change with it too.
internal fun Float.toTermMetricDelta(): TermMetricDelta {
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

// The change of a credits count against the older term's. The views that animate the credits build it with this too.
internal fun Int.toCreditsTermMetricDelta(): TermMetricDelta {
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
