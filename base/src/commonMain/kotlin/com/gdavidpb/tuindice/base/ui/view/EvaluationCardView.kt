package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.EvaluationTone
import com.gdavidpb.tuindice.base.ui.model.EvaluationCardElementModifiers
import com.gdavidpb.tuindice.base.ui.model.EvaluationCardItem
import com.gdavidpb.tuindice.base.ui.model.EvaluationToneColors
import com.gdavidpb.tuindice.base.ui.model.SubjectCodeChipVariant
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// The card of one evaluation: its name with its subject, its date and place, its grade over the maximum, and its
// state. Every screen that lists evaluations draws it this way, so the Evaluations tab and the assistant read alike.
// [cardModifier] goes on the card, where a row carries its tap and its test tag; [elementModifiers] go on the parts
// it names.
@Composable
fun EvaluationCardView(
	item: EvaluationCardItem,
	modifier: Modifier = Modifier,
	cardModifier: Modifier = Modifier,
	elementModifiers: EvaluationCardElementModifiers = EvaluationCardElementModifiers()
) {
	Card(
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant,
			contentColor = MaterialTheme.colorScheme.onSurface
		),
		modifier = cardModifier
			.fillMaxWidth()
			.padding(
				horizontal = EvaluationCardHorizontalPadding,
				vertical = EvaluationCardVerticalPadding
			)
			// The caller's modifier goes last, inside the margin, so an arrival highlight frames the card
			// and not the space around it.
			.then(modifier)
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(EvaluationCardContentPadding)
		) {
			Column {
				EvaluationCardHeader(item = item, elementModifiers = elementModifiers)
				EvaluationCardFooter(item = item, elementModifiers = elementModifiers)
			}
		}
	}
}

// The name and the state. The state stays on the first line: both chips are as tall as each other, so the state lines
// up with the name when they share the line, and with the first line of the name when the subject goes under it.
@Composable
private fun EvaluationCardHeader(
	item: EvaluationCardItem,
	elementModifiers: EvaluationCardElementModifiers
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.Top
	) {
		Row(
			modifier = Modifier
				.weight(1f),
			verticalAlignment = Alignment.CenterVertically
		) {
			item.typeIcon?.let { icon ->
				Icon(
					modifier = elementModifiers.typeIcon.size(TypeIconSize),
					imageVector = icon,
					tint = MaterialTheme.colorScheme.onBackground,
					contentDescription = null
				)
			}

			// The name and the subject share a flow: the subject goes under the name only when it does not fit beside it,
			// and the name keeps the width that the icon and the state leave it.
			FlowRow(
				modifier = Modifier
					.weight(1f)
					// Only an icon before the name leaves the name indented from the edge of the row.
					.then(
						if (item.typeIcon != null) {
							Modifier.padding(start = TuIndiceSpacing.Medium)
						} else {
							Modifier
						}
					),
				horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
				verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall),
				itemVerticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = item.name,
					color = MaterialTheme.colorScheme.onBackground,
					style = MaterialTheme.typography.titleMedium,
				)

				SubjectCodeChip(
					subjectCode = item.subjectCode,
					variant = SubjectCodeChipVariant.Dense
				)
			}
		}

		item.statusText?.let { text ->
			EvaluationStatusBox(
				text = text,
				tone = item.statusTone,
				modifier = elementModifiers.status
			)
		}
	}
}

// The state of an evaluation, written in a box of its tone: the error container for an overdue one, and the surface the
// card's other parts sit in for the rest.
@Composable
private fun EvaluationStatusBox(
	text: String,
	tone: EvaluationTone,
	modifier: Modifier
) {
	val statusColors = evaluationToneColors(tone)

	Box(
		modifier = modifier
			.background(
				color = statusColors.container,
				shape = RoundedCornerShape(TuIndiceRadius.Small)
			)
			.padding(horizontal = StatusHorizontalPadding, vertical = StatusVerticalPadding)
	) {
		Text(
			text = text,
			color = statusColors.content,
			style = MaterialTheme.typography.labelLarge,
		)
	}
}

// The date and the place on the left, and the grade on the right. The date is always read with the calendar icon.
@Composable
private fun EvaluationCardFooter(
	item: EvaluationCardItem,
	elementModifiers: EvaluationCardElementModifiers
) {
	val metadataColor = when (item.highlightTone) {
		EvaluationTone.Error -> MaterialTheme.colorScheme.error
		else -> MaterialTheme.colorScheme.onSurfaceVariant
	}

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(top = EvaluationMetadataTopPadding),
		verticalAlignment = Alignment.CenterVertically
	) {
		// It takes only what the grade box leaves: the lines wrap inside it instead of squeezing the box, which keeps the
		// size it has on a row without a place.
		Column(
			modifier = Modifier.weight(1f),
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
		) {
			item.dateText?.let { text ->
				MetadataLine(
					icon = Icons.Outlined.CalendarToday,
					text = text,
					color = metadataColor
				)
			}

			item.place?.let { place ->
				ClassPlaceLines(
					modifier = elementModifiers.place,
					place = place
				)
			}
		}

		item.gradeText?.let { text ->
			Box(
				modifier = elementModifiers.grade
					.border(
						border = BorderStroke(TuIndiceSpacing.Hairline, MaterialTheme.colorScheme.outlineVariant),
						shape = RoundedCornerShape(TuIndiceRadius.Large)
					)
					.padding(
						horizontal = GradeHorizontalPadding,
						vertical = GradeVerticalPadding
					),
				contentAlignment = Alignment.Center
			) {
				Text(
					text = text,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold
				)
			}
		}
	}
}

// The colors of a tone. An error sits in the error container; a success and a neutral state share the surface the
// Evaluations tab has always drawn them in.
@Composable
fun evaluationToneColors(tone: EvaluationTone): EvaluationToneColors =
	when (tone) {
		EvaluationTone.Success, EvaluationTone.Neutral -> EvaluationToneColors(
			container = MaterialTheme.colorScheme.surfaceDim,
			content = MaterialTheme.colorScheme.onSurface
		)

		EvaluationTone.Error -> EvaluationToneColors(
			container = MaterialTheme.colorScheme.errorContainer,
			content = MaterialTheme.colorScheme.onErrorContainer
		)
	}

// The margin around the card is the gutter of the list, and the padding inside it is the card's own. The values the
// card has always used are kept, since a list of evaluations must look the same wherever it is shown.
private val EvaluationCardHorizontalPadding = TuIndiceSpacing.Section
private val EvaluationCardVerticalPadding = TuIndiceSpacing.Small
private val EvaluationCardContentPadding = 14.dp
private val EvaluationMetadataTopPadding = TuIndiceSpacing.Medium
private val GradeHorizontalPadding = 20.dp
private val GradeVerticalPadding = TuIndiceSpacing.Medium
private val StatusHorizontalPadding = TuIndiceSpacing.Large
private val StatusVerticalPadding = 5.dp
private val TypeIconSize = 18.dp
