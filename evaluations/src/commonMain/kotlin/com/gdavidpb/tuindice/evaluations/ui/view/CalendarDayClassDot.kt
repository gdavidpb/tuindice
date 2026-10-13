package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import kotlinx.datetime.LocalDate

/**
 * The dot under a calendar day on which the chosen subject has class. A day without class keeps the
 * same room, drawn as nothing, so the rows of the calendar keep their height either way.
 */
@Composable
fun CalendarDayClassDot(
	date: LocalDate?,
	hasClass: Boolean,
	isSelectable: Boolean
) {
	val dotModifier = if (date != null && hasClass) {
		Modifier
			.testTag(EvaluationsUiTags.calendarDayClassDot(date.day))
			.background(
				color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
					alpha = if (isSelectable) 1f else TuIndiceAlpha.Disabled
				),
				shape = CircleShape
			)
	} else {
		Modifier
	}

	Box(
		modifier = Modifier
			.padding(top = ClassDotTopPadding)
			.size(ClassDotSize)
			.then(dotModifier)
	)
}

private val ClassDotSize = 4.dp
private val ClassDotTopPadding = 2.dp
