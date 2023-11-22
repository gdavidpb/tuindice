package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem

@Composable
fun EvaluationItemView(
	modifier: Modifier = Modifier,
	item: EvaluationItem,
) {
	ElevatedCard(
		modifier = modifier
			.fillMaxWidth()
			.padding(
				horizontal = dimensionResource(id = R.dimen.dp_16),
				vertical = dimensionResource(id = R.dimen.dp_8)
			)
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(dimensionResource(id = R.dimen.dp_16))
		) {
			if (item.isOverdue)
				Box(
					modifier = Modifier
						.align(Alignment.TopEnd)
						.size(dimensionResource(id = R.dimen.dp_8))
						.background(
							color = MaterialTheme.colorScheme.error,
							shape = CircleShape
						)
				)

			Column {
				Text(
					modifier = Modifier
						.padding(bottom = dimensionResource(id = R.dimen.dp_6))
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
					Icon(
						modifier = Modifier
							.size(dimensionResource(id = R.dimen.dp_16)),
						imageVector = item.typeIcon,
						tint = MaterialTheme.colorScheme.outline,
						contentDescription = null
					)

					Text(
						modifier = Modifier
							.padding(start = dimensionResource(id = R.dimen.dp_12)),
						text = item.typeAndSubjectCodeText,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						style = MaterialTheme.typography.bodyMedium
					)
				}

				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(top = dimensionResource(R.dimen.dp_16)),
					verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						modifier = Modifier
							.size(dimensionResource(id = R.dimen.dp_16)),
						imageVector = item.dateIcon,
						tint = item.highlightIconColor,
						contentDescription = null
					)

					Text(
						modifier = Modifier
							.padding(start = dimensionResource(id = R.dimen.dp_12)),
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
						.padding(vertical = dimensionResource(id = R.dimen.dp_4)),
					verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						modifier = Modifier
							.size(dimensionResource(id = R.dimen.dp_16)),
						imageVector = item.gradesIcon,
						tint = item.highlightIconColor,
						contentDescription = null
					)

					Text(
						modifier = Modifier
							.padding(start = dimensionResource(id = R.dimen.dp_12)),
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