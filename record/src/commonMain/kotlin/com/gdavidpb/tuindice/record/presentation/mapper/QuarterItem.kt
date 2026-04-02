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
import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.policy.QuarterMutationPolicy
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem
import com.gdavidpb.tuindice.record.presentation.model.QuarterMetricDelta
import com.gdavidpb.tuindice.record.presentation.model.QuarterMetricDeltaTone
import kotlin.math.abs

private const val QUARTER_DELTA_DECIMALS = 4
private const val ZERO_DELTA_TEXT = "0.0000"
private const val UP_DELTA_SYMBOL = "▲"
private const val DOWN_DELTA_SYMBOL = "▼"

@Composable
fun List<Quarter>.toQuarterItemList(
	viewMode: RecordViewMode,
	texts: RecordMapperTexts,
	highlightColor: Color
) = mapIndexed { index, quarter ->
	quarter.toQuarterItem(
		viewMode = viewMode,
		texts = texts,
		highlightColor = highlightColor,
		previousQuarter = getOrNull(index + 1)
	)
}

@Composable
fun Quarter.toQuarterItem(
	viewMode: RecordViewMode,
	texts: RecordMapperTexts,
	highlightColor: Color,
	previousQuarter: Quarter? = null
): QuarterItem {
	val resolvedGradeValue = when (viewMode) {
		RecordViewMode.Official -> grade
		RecordViewMode.Simulation -> if (isReadOnly) grade else simulationGrade ?: grade
	}
	val resolvedGradeSumValue = when (viewMode) {
		RecordViewMode.Official -> gradeSum
		RecordViewMode.Simulation -> if (isReadOnly) gradeSum else simulationGradeSum ?: gradeSum
	}
	val resolvedCreditsValue = when (viewMode) {
		RecordViewMode.Official -> credits
		RecordViewMode.Simulation -> if (isReadOnly) credits else simulationCredits ?: credits
	}
	val shouldShowDeltas = subjects.isNotEmpty()
	val animatedGrade = animateFloatAsState(
		targetValue = resolvedGradeValue.toFloat(),
		label = "animatedGrade_animateFloatAsState"
	)

	val animatedGradeSum = animateFloatAsState(
		targetValue = resolvedGradeSumValue.toFloat(),
		label = "animatedGradeSum_animateFloatAsState"
	)

	val animatedCredits = animateIntAsState(
		targetValue = resolvedCreditsValue,
		label = "animatedCredits_animateIntAsState"
	)

	return QuarterItem(
		quarterId = id,
		shortNameText = name.toQuarterShortName(),
		gradeText = texts
			.quarterGradeDiff(animatedGrade.value)
			.annotatedQuarterValue(highlightColor),
		gradeDelta = previousQuarter?.takeIf { shouldShowDeltas }?.let { quarter ->
			(animatedGrade.value - quarter.resolvedGrade(viewMode).toFloat()).toQuarterMetricDelta()
		},
		gradeSumText = texts
			.quarterGradeSum(animatedGradeSum.value)
			.annotatedQuarterValue(highlightColor),
		gradeSumDelta = previousQuarter?.takeIf { shouldShowDeltas }?.let { quarter ->
			(animatedGradeSum.value - quarter.resolvedGradeSum(viewMode).toFloat()).toQuarterMetricDelta()
		},
		creditsText = texts
			.quarterCredits(animatedCredits.value)
			.annotatedQuarterValue(highlightColor),
		creditsDelta = previousQuarter?.takeIf { shouldShowDeltas }?.let { quarter ->
			(animatedCredits.value - quarter.resolvedCredits(viewMode)).toCreditsQuarterMetricDelta()
		},
		isCurrent = isCurrent,
		canDelete = (viewMode == RecordViewMode.Simulation) && QuarterMutationPolicy.canDelete(
			isCurrent = isCurrent,
			isReadOnly = isReadOnly
		),
		subjects = subjects.map { subject ->
			subject.toSubjectItem(
				isReadOnly = (viewMode == RecordViewMode.Official) ||
					!QuarterMutationPolicy.canEditGrades(isReadOnly),
				resolvedStatus = subject.resolvedStatus(viewMode),
				texts = texts
			)
		}
	)
}

private fun Quarter.resolvedGrade(viewMode: RecordViewMode): Double {
	return when (viewMode) {
		RecordViewMode.Official -> grade
		RecordViewMode.Simulation -> if (isReadOnly) grade else simulationGrade ?: grade
	}
}

private fun Quarter.resolvedGradeSum(viewMode: RecordViewMode): Double {
	return when (viewMode) {
		RecordViewMode.Official -> gradeSum
		RecordViewMode.Simulation -> if (isReadOnly) gradeSum else simulationGradeSum ?: gradeSum
	}
}

private fun Quarter.resolvedCredits(viewMode: RecordViewMode): Int {
	return when (viewMode) {
		RecordViewMode.Official -> credits
		RecordViewMode.Simulation -> if (isReadOnly) credits else simulationCredits ?: credits
	}
}

private fun com.gdavidpb.tuindice.base.domain.model.subject.Subject.resolvedStatus(
	viewMode: RecordViewMode
): SubjectStatus? {
	return when (viewMode) {
		RecordViewMode.Official -> status
		RecordViewMode.Simulation -> simulationStatus ?: status
	}
}

private fun Float.toQuarterMetricDelta(): QuarterMetricDelta {
	val magnitudeText = abs(toDouble()).formatGrade(decimals = QUARTER_DELTA_DECIMALS)
	return buildQuarterMetricDelta(
		magnitudeText = magnitudeText,
		tone = when {
			magnitudeText == ZERO_DELTA_TEXT -> QuarterMetricDeltaTone.Neutral
			this > 0f -> QuarterMetricDeltaTone.Positive
			else -> QuarterMetricDeltaTone.Negative
		},
		isPositive = this > 0f,
		isZero = magnitudeText == ZERO_DELTA_TEXT
	)
}

private fun Int.toCreditsQuarterMetricDelta(): QuarterMetricDelta {
	val magnitude = abs(this)

	return buildQuarterMetricDelta(
		magnitudeText = magnitude.toString(),
		tone = QuarterMetricDeltaTone.Informational,
		isPositive = this > 0,
		isZero = magnitude == 0
	)
}

private fun buildQuarterMetricDelta(
	magnitudeText: String,
	tone: QuarterMetricDeltaTone,
	isPositive: Boolean,
	isZero: Boolean
): QuarterMetricDelta {
	val prefix = when {
		isZero -> ""
		isPositive -> UP_DELTA_SYMBOL
		else -> DOWN_DELTA_SYMBOL
	}
	val text = if (prefix.isEmpty()) {
		magnitudeText
	} else {
		"$prefix $magnitudeText"
	}

	return QuarterMetricDelta(
		text = text,
		tone = tone
	)
}

@Composable
@ReadOnlyComposable
fun String.annotatedQuarterValue(highlightColor: Color) = buildAnnotatedString {
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

private fun String.toQuarterShortName(): String {
	return listOf(
		"Enero" to "Ene.",
		"Febrero" to "Feb.",
		"Marzo" to "Mar.",
		"Abril" to "Abr.",
		"Mayo" to "May.",
		"Junio" to "Jun.",
		"Julio" to "Jul.",
		"Agosto" to "Ago.",
		"Septiembre" to "Sep.",
		"Octubre" to "Oct.",
		"Noviembre" to "Nov.",
		"Diciembre" to "Dic."
	).fold(this) { label, (fullMonth, shortMonth) ->
		label.replace(fullMonth, shortMonth)
	}
}
