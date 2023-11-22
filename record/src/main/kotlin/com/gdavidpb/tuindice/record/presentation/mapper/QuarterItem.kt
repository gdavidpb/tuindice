package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.record.R
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem

@Composable
fun List<Quarter>.toQuarterItemList() =
	map { quarter -> quarter.toQuarterItem() }

@Composable
fun Quarter.toQuarterItem(): QuarterItem {
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
		gradeText = stringResource(id = R.string.quarter_grade_diff, animatedGrade.value)
			.annotatedQuarterValue(),
		gradeSumText = stringResource(id = R.string.quarter_grade_sum, animatedGradeSum.value)
			.annotatedQuarterValue(),
		creditsText = stringResource(id = R.string.quarter_credits, animatedCredits.value)
			.annotatedQuarterValue(),
		subjects = subjects.map { subject -> subject.toSubjectItem() },
		states = rememberSubjectsStates(subjects)
	)
}

@Composable
fun rememberSubjectsStates(subjects: List<Subject>) = remember {
	subjects
		.associate { subject -> subject.id to mutableIntStateOf(subject.grade) }
		.let(::HashMap)
}

@Composable
@ReadOnlyComposable
fun String.annotatedQuarterValue() = buildAnnotatedString {
	val before = substringBefore(" ")
	val after = substringAfter(" ")

	withStyle(
		SpanStyle(
			color = MaterialTheme.colorScheme.primary,
			fontWeight = FontWeight.Medium
		)
	) {
		append(before)
	}

	append(" $after")
}