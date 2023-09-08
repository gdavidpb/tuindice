package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import com.gdavidpb.tuindice.base.domain.model.subject.Subject

@Composable
@ReadOnlyComposable
fun String.annotatedQuarterValue(baselineShift: Float = 0.0f) = buildAnnotatedString {
	val before = substringBefore(" ")
	val after = substringAfter(" ")

	withStyle(
		SpanStyle(
			color = MaterialTheme.colorScheme.primary,
			fontWeight = FontWeight.Medium,
			baselineShift = BaselineShift(baselineShift)
		)
	) {
		append(before)
	}

	append(" $after")
}

@Composable
fun rememberSubjectsStates(subjects: List<Subject>) = remember {
	subjects
		.associate { subject -> subject.id to mutableIntStateOf(subject.grade) }
		.let(::HashMap)
}