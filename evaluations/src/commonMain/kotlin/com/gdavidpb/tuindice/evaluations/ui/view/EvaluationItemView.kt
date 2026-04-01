package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags

@Composable
fun EvaluationItemView(
	modifier: Modifier = Modifier,
	item: EvaluationItem,
) {
	ElevatedCard(
		modifier = modifier
			.testTag(EvaluationsUiTags.evaluationItemCard(item.evaluationId))
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
			if (item.isOverdue)
				Box(
					modifier = Modifier
						.testTag(EvaluationsUiTags.EvaluationOverdueIndicator)
						.align(Alignment.TopEnd)
						.size(8.dp)
						.background(
							color = MaterialTheme.colorScheme.error,
							shape = CircleShape
						)
				)

			Column {
				Text(
					modifier = Modifier
						.padding(bottom = 6.dp)
						.fillMaxWidth(),
					text = item.nameText,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					color = MaterialTheme.colorScheme.onBackground,
					style = MaterialTheme.typography.titleLarge,
					fontWeight = FontWeight.Bold
				)

				Row(
					modifier = Modifier
						.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically
				) {
					SubjectCodeChip(
						subjectCode = item.subjectCodeText,
						containerColor = item.subjectCodeContainerColor,
						contentColor = item.subjectCodeColor
					)

					Icon(
						modifier = Modifier
							.padding(start = 12.dp)
							.size(16.dp),
						imageVector = item.typeIcon,
						tint = MaterialTheme.colorScheme.outline,
						contentDescription = null
					)

					Text(
						modifier = Modifier
							.padding(start = 6.dp)
							.weight(1f),
						text = item.typeText,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						style = MaterialTheme.typography.bodyMedium
					)
				}

				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(top = 16.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						modifier = Modifier
							.size(16.dp),
						imageVector = item.dateIcon,
						tint = item.highlightIconColor,
						contentDescription = null
					)

					Text(
						modifier = Modifier
							.padding(start = 12.dp),
						text = item.dateText,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						color = item.highlightTextColor,
						style = MaterialTheme.typography.bodyMedium
					)
				}

				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(vertical = 4.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						modifier = Modifier
							.size(16.dp),
						imageVector = item.gradesIcon,
						tint = item.highlightIconColor,
						contentDescription = null
					)

					Text(
						modifier = Modifier
							.padding(start = 12.dp),
						text = item.gradesText,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						color = item.highlightTextColor,
						style = MaterialTheme.typography.bodyMedium
					)
				}
			}
		}
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
