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
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.record.domain.policy.QuarterMutationPolicy
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem
import com.gdavidpb.tuindice.record.presentation.model.QuarterMetricDelta
import com.gdavidpb.tuindice.record.presentation.model.QuarterMetricDeltaTone
import kotlin.math.abs

private const val QUARTER_DELTA_DECIMALS = 2
private const val ZERO_DELTA_TEXT = "0.00"

@Composable
fun List<Quarter>.toQuarterItemList(
	texts: RecordMapperTexts,
	highlightColor: Color
) = mapIndexed { index, quarter ->
	quarter.toQuarterItem(
		texts = texts,
		highlightColor = highlightColor,
		previousQuarter = getOrNull(index + 1)
	)
}

@Composable
fun Quarter.toQuarterItem(
	texts: RecordMapperTexts,
	highlightColor: Color,
	previousQuarter: Quarter? = null
): QuarterItem {
	val animatedGrade = animateFloatAsState(
		targetValue = grade.toFloat(),
		label = "animatedGrade_animateFloatAsState"
	)

	val animatedGradeSum = animateFloatAsState(
		targetValue = gradeSum.toFloat(),
		label = "animatedGradeSum_animateFloatAsState"
	)

	val animatedCredits = animateIntAsState(
		targetValue = credits,
		label = "animatedCredits_animateIntAsState"
	)

	return QuarterItem(
		quarterId = id,
		shortNameText = name.toQuarterShortName(),
		gradeText = texts
			.quarterGradeDiff(animatedGrade.value)
			.annotatedQuarterValue(highlightColor),
		gradeDelta = previousQuarter?.let { quarter ->
			(animatedGrade.value - quarter.grade.toFloat()).toQuarterMetricDelta()
		},
		gradeSumText = texts
			.quarterGradeSum(animatedGradeSum.value)
			.annotatedQuarterValue(highlightColor),
		gradeSumDelta = previousQuarter?.let { quarter ->
			(animatedGradeSum.value - quarter.gradeSum.toFloat()).toQuarterMetricDelta()
		},
		creditsText = texts
			.quarterCredits(animatedCredits.value)
			.annotatedQuarterValue(highlightColor),
		isCurrent = isCurrent,
		canDelete = QuarterMutationPolicy.canDelete(
			isCurrent = isCurrent,
			isReadOnly = isReadOnly
		),
		subjects = subjects.map { subject ->
			subject.toSubjectItem(
				isReadOnly = !QuarterMutationPolicy.canEditGrades(isReadOnly),
				texts = texts
			)
		}
	)
}

private fun Float.toQuarterMetricDelta(): QuarterMetricDelta {
	val magnitudeText = abs(toDouble()).formatGrade(decimals = QUARTER_DELTA_DECIMALS)
	val tone = when {
		magnitudeText == ZERO_DELTA_TEXT -> QuarterMetricDeltaTone.Neutral
		this > 0f -> QuarterMetricDeltaTone.Positive
		else -> QuarterMetricDeltaTone.Negative
	}
	val prefix = when (tone) {
		QuarterMetricDeltaTone.Positive -> "+"
		QuarterMetricDeltaTone.Negative -> "-"
		QuarterMetricDeltaTone.Neutral -> ""
	}

	return QuarterMetricDelta(
		text = "$prefix$magnitudeText",
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
