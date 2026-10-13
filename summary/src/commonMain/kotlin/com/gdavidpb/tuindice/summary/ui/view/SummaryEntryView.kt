package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.ui.model.SubjectCodeChipVariant
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.ClassPlaceLine
import com.gdavidpb.tuindice.base.ui.view.SubjectCodeChip

/**
 * What a row of the week is about: the title, when it has one, with the chip of the subject after
 * it, and under them where it takes place (blocks and room, and the hour if the place carries it).
 * The chip goes down to its own line before it would be squeezed.
 */
@Composable
fun SummaryEntryView(
	titleText: String?,
	subjectCode: String,
	place: ClassPlaceItem?,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier,
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Two)
	) {
		FlowRow(
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall),
			itemVerticalAlignment = Alignment.CenterVertically
		) {
			if (titleText != null) {
				Text(
					text = titleText,
					style = MaterialTheme.typography.bodyLarge,
					fontWeight = FontWeight.Medium
				)
			}

			SubjectCodeChip(
				subjectCode = subjectCode,
				variant = SubjectCodeChipVariant.Dense
			)
		}

		if (place != null) {
			ClassPlaceLine(place = place)
		}
	}
}
