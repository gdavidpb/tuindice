package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.view.ClassPlaceLine
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags

/**
 * What the date dialog says under its calendar about the chosen day: the date and, when the subject
 * has class that day, where ("En clase · Bloques 1-2 · MYS-116") with its hours after, lighter. On a
 * day without class it is only the date.
 */
@Composable
fun EvaluationDateSelectionSummary(
	modifier: Modifier = Modifier,
	dateText: UiText,
	classPlace: ClassPlaceItem?
) {
	Column(
		modifier = modifier
			.testTag(EvaluationsUiTags.EvaluationDateDialogSelection)
			.fillMaxWidth()
			.background(
				color = MaterialTheme.colorScheme.surfaceVariant,
				shape = RoundedCornerShape(TuIndiceRadius.Medium)
			)
			.padding(12.dp),
		verticalArrangement = Arrangement.spacedBy(2.dp)
	) {
		Text(
			text = dateText.asString(),
			style = MaterialTheme.typography.bodyLarge,
			fontWeight = FontWeight.SemiBold,
			color = MaterialTheme.colorScheme.onSurface
		)

		classPlace?.let { place ->
			ClassPlaceLine(
				modifier = Modifier.testTag(EvaluationsUiTags.EvaluationDateDialogSelectionPlace),
				place = place
			)
		}
	}
}
