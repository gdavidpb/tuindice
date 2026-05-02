package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationHighlightTone
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags

@Composable
fun EvaluationItemView(
	modifier: Modifier = Modifier,
	item: EvaluationItem,
	onGradeClick: () -> Unit = {},
	onCardClick: () -> Unit = {}
) {
	val metadataColor = when (item.highlightTone) {
		EvaluationHighlightTone.Error -> MaterialTheme.colorScheme.error
		else -> MaterialTheme.colorScheme.onSurfaceVariant
	}
	val gradeButtonColors = if (item.isOverdue) {
		ButtonDefaults.filledTonalButtonColors(
			containerColor = Color(0xFFFFDAD6),
			contentColor = Color(0xFF410002)
		)
	} else {
		ButtonDefaults.filledTonalButtonColors(
			containerColor = MaterialTheme.colorScheme.primaryContainer,
			contentColor = MaterialTheme.colorScheme.onPrimaryContainer
		)
	}

	ElevatedCard(
		modifier = modifier
			.testTag(EvaluationsUiTags.evaluationItemCard(item.evaluationId))
			.clickable(onClick = onCardClick)
			.fillMaxWidth()
			.padding(
				horizontal = 16.dp,
				vertical = 8.dp
			)
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp)
		) {
			Column {
				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.Top
				) {
					Box(
						modifier = Modifier
							.testTag(EvaluationsUiTags.EvaluationTypeLeadingIcon)
							.size(44.dp)
							.background(
								color = MaterialTheme.colorScheme.secondaryContainer,
								shape = RoundedCornerShape(12.dp)
							),
						contentAlignment = Alignment.Center
					) {
						Icon(
							modifier = Modifier.size(24.dp),
							imageVector = item.typeIcon,
							tint = MaterialTheme.colorScheme.onSecondaryContainer,
							contentDescription = null
						)
					}

					Spacer(modifier = Modifier.width(12.dp))

					Column(
						modifier = Modifier.weight(1f)
					) {
						Text(
							modifier = Modifier.fillMaxWidth(),
							text = item.nameText,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis,
							color = MaterialTheme.colorScheme.onBackground,
							style = MaterialTheme.typography.titleMedium,
							fontWeight = FontWeight.SemiBold
						)

						Row(
							modifier = Modifier
								.fillMaxWidth()
								.padding(top = 8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							SubjectCodeChip(
								subjectCode = item.subjectCodeText,
								containerColor = item.subjectCodeContainerColor,
								contentColor = item.subjectCodeColor
							)
						}
					}

					if (item.showsGradeAction) {
						EvaluationGradeActionButton(
							modifier = Modifier.testTag(EvaluationsUiTags.EvaluationGradeActionButton),
							text = item.gradeActionText,
							colors = gradeButtonColors,
							onClick = onGradeClick
						)
					}
				}

				Box(
					modifier = Modifier.padding(start = 56.dp)
				) {
					EvaluationMetadataRow(
						modifier = Modifier.padding(top = 12.dp),
						dateIcon = item.dateIcon,
						dateText = item.dateText,
						typeText = item.typeText,
						color = metadataColor
					)
				}
			}
		}
	}
}

@Composable
private fun EvaluationGradeActionButton(
	modifier: Modifier = Modifier,
	text: String,
	colors: ButtonColors,
	onClick: () -> Unit
) {
	FilledTonalButton(
		modifier = modifier,
		onClick = onClick,
		colors = colors,
		contentPadding = ButtonDefaults.TextButtonContentPadding
	) {
		Text(text = text)
	}
}

@Composable
private fun EvaluationMetadataRow(
	modifier: Modifier = Modifier,
	dateIcon: ImageVector,
	dateText: String,
	typeText: String,
	color: Color
) {
	Row(
		modifier = modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			modifier = Modifier.size(16.dp),
			imageVector = dateIcon,
			tint = color,
			contentDescription = null
		)

		Text(
			modifier = Modifier.padding(start = 6.dp),
			text = dateText,
			maxLines = 1,
			softWrap = false,
			color = color,
			style = MaterialTheme.typography.bodyMedium
		)

		Text(
			modifier = Modifier.padding(horizontal = 6.dp),
			text = "•",
			color = color,
			style = MaterialTheme.typography.bodyMedium
		)

		Text(
			text = typeText,
			maxLines = 1,
			softWrap = false,
			color = color,
			style = MaterialTheme.typography.bodyMedium
		)
	}
}

@Composable
private fun SubjectCodeChip(
	modifier: Modifier = Modifier,
	subjectCode: String,
	containerColor: Color,
	contentColor: Color
) {
	Text(
		modifier = modifier
			.heightIn(min = 28.dp)
			.background(
				color = containerColor,
				shape = RoundedCornerShape(8.dp)
			)
			.padding(vertical = 5.dp, horizontal = 10.dp),
		text = subjectCode,
		color = contentColor,
		fontWeight = FontWeight.SemiBold,
		style = MaterialTheme.typography.labelLarge
	)
}
