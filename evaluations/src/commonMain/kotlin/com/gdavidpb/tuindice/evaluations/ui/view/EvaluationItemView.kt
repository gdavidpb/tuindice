package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
	val statusColors = statusColors(item.statusTone)

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
				.background(MaterialTheme.colorScheme.surfaceVariant)
				.padding(16.dp)
		) {
			Column {
				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically
				) {
					Row(
						modifier = Modifier
							.weight(1f),
						verticalAlignment = Alignment.CenterVertically
					) {
						Icon(
							modifier = Modifier
								.testTag(EvaluationsUiTags.EvaluationTypeInlineIcon)
								.size(18.dp),
							imageVector = item.typeIcon,
							tint = MaterialTheme.colorScheme.onBackground,
							contentDescription = null
						)

						Text(
							modifier = Modifier
								.weight(1f)
								.padding(start = 8.dp),
							text = item.typeNameText,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis,
							color = MaterialTheme.colorScheme.onBackground,
							style = MaterialTheme.typography.titleMedium,
							fontWeight = FontWeight.SemiBold
						)
					}

					Box(
						modifier = Modifier
							.testTag(EvaluationsUiTags.EvaluationStatusChip)
							.background(
								color = statusColors.container,
								shape = RoundedCornerShape(8.dp)
							)
							.padding(horizontal = 10.dp, vertical = 5.dp)
					) {
						Text(
							text = item.statusText,
							color = statusColors.content,
							style = MaterialTheme.typography.labelMedium,
							fontWeight = FontWeight.SemiBold
						)
					}
				}

				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(top = 10.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					SubjectCodeChip(
						subjectCode = item.subjectCodeText,
						containerColor = item.subjectCodeContainerColor,
						contentColor = item.subjectCodeColor
					)
				}

				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(top = 16.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						modifier = Modifier.size(18.dp),
						imageVector = item.dateIcon,
						tint = metadataColor,
						contentDescription = null
					)

					Text(
						modifier = Modifier.padding(start = 8.dp),
						text = item.dateText,
						maxLines = 1,
						softWrap = false,
						color = metadataColor,
						style = MaterialTheme.typography.bodyMedium
					)

					Spacer(modifier = Modifier.weight(1f))

					if (item.showsGradeAction) {
						Box(
							modifier = Modifier
								.testTag(EvaluationsUiTags.EvaluationGradeActionButton)
								.clickable(onClick = onGradeClick)
								.border(
									border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
									shape = RoundedCornerShape(16.dp)
								)
								.padding(horizontal = 22.dp, vertical = 10.dp),
							contentAlignment = Alignment.Center
						) {
							Text(
								text = item.gradeText,
								color = MaterialTheme.colorScheme.onSurfaceVariant,
								style = MaterialTheme.typography.titleMedium,
								fontWeight = FontWeight.Bold
							)
						}
					}
				}
			}
		}
	}
}

private data class StatusColors(
	val container: Color,
	val content: Color
)

@Composable
private fun statusColors(tone: EvaluationHighlightTone): StatusColors {
	return when (tone) {
		EvaluationHighlightTone.Success -> StatusColors(
			container = Color(0xFF314D20),
			content = Color(0xFFC7F28E)
		)

		EvaluationHighlightTone.Error -> StatusColors(
			container = Color(0xFF6E2D32),
			content = Color(0xFFFFC5C9)
		)

		EvaluationHighlightTone.Neutral -> StatusColors(
			container = MaterialTheme.colorScheme.primaryContainer,
			content = MaterialTheme.colorScheme.onPrimaryContainer
		)
	}
}
