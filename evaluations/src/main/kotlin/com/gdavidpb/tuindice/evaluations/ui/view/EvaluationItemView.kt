package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.gdavidpb.tuindice.evaluations.R

@Composable
fun EvaluationItemView(
	modifier: Modifier = Modifier,
	name: String,
	subjectCode: String,
	date: String,
	type: String,
	icon: ImageVector,
	grade: Double,
	maxGrade: Double,
	isContinuous: Boolean,
	isNotGraded: Boolean,
	isCompleted: Boolean,
	isCompletedChanged: (isCompleted: Boolean) -> Unit
) {
	val isDone = (isCompleted || isContinuous || isNotGraded)

	ElevatedCard(
		modifier = modifier
			.fillMaxWidth()
			.padding(
				horizontal = dimensionResource(id = R.dimen.dp_16),
				vertical = dimensionResource(id = R.dimen.dp_8)
			)
	) {
		ConstraintLayout(
			modifier = Modifier
				.fillMaxWidth()
				.padding(dimensionResource(id = R.dimen.dp_16))
		) {
			val (
				textName,
				textSubjectCode,
				textDate,
				textGrade,
				dotNotification
			) = createRefs()

			Text(
				modifier = Modifier
					.constrainAs(textName) {
						start.linkTo(parent.start)
						top.linkTo(parent.top)

						width = Dimension.fillToConstraints
					},
				text = name,
				color = MaterialTheme.colorScheme.onBackground,
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Bold
			)

			if (isNotGraded)
				Box(
					modifier = Modifier
						.constrainAs(dotNotification) {
							end.linkTo(parent.end)
							top.linkTo(parent.top)
						}
						.size(dimensionResource(id = R.dimen.dp_8))
						.background(
							color = MaterialTheme.colorScheme.error,
							shape = CircleShape
						)
				)

			Row(
				modifier = Modifier
					.constrainAs(textSubjectCode) {
						start.linkTo(textName.start)
						top.linkTo(textName.bottom)

						width = Dimension.fillToConstraints
					},
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					modifier = Modifier
						.size(dimensionResource(id = R.dimen.dp_16)),
					imageVector = icon,
					tint = MaterialTheme.colorScheme.outline,
					contentDescription = null
				)

				Text(
					modifier = Modifier
						.padding(start = dimensionResource(id = R.dimen.dp_12)),
					text = stringResource(id = R.string.evaluation_title, type, subjectCode),
					style = MaterialTheme.typography.bodyMedium
				)
			}

			Row(
				modifier = Modifier
					.constrainAs(textDate) {
						start.linkTo(textSubjectCode.start)
						top.linkTo(textSubjectCode.bottom)

						width = Dimension.fillToConstraints
					}
					.padding(
						top = dimensionResource(
							id = if (isDone) R.dimen.dp_16 else R.dimen.dp_6
						)
					),
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					modifier = Modifier
						.size(dimensionResource(id = R.dimen.dp_16)),
					imageVector = if (isCompleted)
						Icons.Outlined.Done
					else
						Icons.Outlined.CalendarToday,
					tint = when {
						isCompleted -> MaterialTheme.colorScheme.primary
						isNotGraded -> MaterialTheme.colorScheme.error
						else -> MaterialTheme.colorScheme.outline
					},
					contentDescription = null
				)

				Text(
					modifier = Modifier
						.padding(start = dimensionResource(id = R.dimen.dp_12)),
					text = date,
					color = if (!isNotGraded)
						Color.Unspecified
					else
						MaterialTheme.colorScheme.error,
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Light
				)
			}

			if (isDone)
				Row(
					modifier = Modifier
						.constrainAs(textGrade) {
							start.linkTo(textDate.start)
							top.linkTo(textDate.bottom)

							width = Dimension.fillToConstraints
						}
						.padding(vertical = dimensionResource(id = R.dimen.dp_4)),
					verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						modifier = Modifier
							.size(dimensionResource(id = R.dimen.dp_16)),
						imageVector = Icons.Outlined.BookmarkBorder,
						tint = when {
							isCompleted -> MaterialTheme.colorScheme.primary
							isNotGraded -> MaterialTheme.colorScheme.error
							else -> MaterialTheme.colorScheme.outline
						},
						contentDescription = null
					)

					Text(
						modifier = Modifier
							.padding(start = dimensionResource(id = R.dimen.dp_12)),
						text = if (!isNotGraded)
							stringResource(id = R.string.evaluation_grade, grade, maxGrade)
						else
							stringResource(id = R.string.evaluation_not_completed),
						color = if (!isNotGraded)
							Color.Unspecified
						else
							MaterialTheme.colorScheme.error,
						style = MaterialTheme.typography.bodyMedium
					)
				}
		}
	}
}