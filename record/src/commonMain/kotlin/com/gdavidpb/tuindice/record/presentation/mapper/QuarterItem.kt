package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.record.domain.policy.QuarterMutationPolicy
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem
import com.gdavidpb.tuindice.record.presentation.model.RecordMapperTexts

@Composable
fun List<Quarter>.toQuarterItemList(
	texts: RecordMapperTexts,
	highlightColor: Color
) = map { quarter ->
	quarter.toQuarterItem(
		texts = texts,
		highlightColor = highlightColor
	)
}

@Composable
fun Quarter.toQuarterItem(
	texts: RecordMapperTexts,
	highlightColor: Color
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
		nameText = name,
		gradeText = texts
			.quarterGradeDiff(animatedGrade.value)
			.annotatedQuarterValue(highlightColor),
		gradeSumText = texts
			.quarterGradeSum(animatedGradeSum.value)
			.annotatedQuarterValue(highlightColor),
		creditsText = texts
			.quarterCredits(animatedCredits.value)
			.annotatedQuarterValue(highlightColor),
		canDelete = QuarterMutationPolicy.canDelete(
			isCurrent = isCurrent,
			isReadOnly = isReadOnly
		),
		subjects = subjects.map { subject ->
			subject.toSubjectItem(
				isReadOnly = !QuarterMutationPolicy.canEditGrades(isReadOnly),
				texts = texts
			)
		},
		states = rememberSubjectsStates(subjects)
	)
}

@Composable
fun rememberSubjectsStates(subjects: List<Subject>) = remember {
	HashMap(
		subjects
			.associate { subject -> subject.id to mutableIntStateOf(subject.grade) }
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
			fontWeight = FontWeight.Medium
		)
	) {
		append(before)
	}

	append(" $after")
}
