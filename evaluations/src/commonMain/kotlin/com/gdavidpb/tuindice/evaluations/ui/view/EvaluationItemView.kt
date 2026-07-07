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
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.model.SubjectCodeChipVariant
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.view.SubjectCodeChip
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationHighlightTone
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.a11y_evaluation_register_grade
import tuindice.evaluations.generated.resources.a11y_evaluation_show_actions
import tuindice.evaluations.generated.resources.label_evaluation_swipe_delete
import tuindice.evaluations.generated.resources.label_evaluation_swipe_edit

@Composable
fun EvaluationItemView(
	modifier: Modifier = Modifier,
	item: EvaluationItem,
	onGradeClick: () -> Unit = {},
	onCardClick: () -> Unit = {},
	onEditAction: (() -> Unit)? = null,
	onDeleteAction: (() -> Unit)? = null
) {
	val metadataColor = when (item.highlightTone) {
		EvaluationHighlightTone.Error -> MaterialTheme.colorScheme.error
		else -> MaterialTheme.colorScheme.onSurfaceVariant
	}
	val statusColors = statusColors(item.statusTone)
	val showActionsLabel = stringResource(Res.string.a11y_evaluation_show_actions)
	val editActionLabel = stringResource(Res.string.label_evaluation_swipe_edit)
	val deleteActionLabel = stringResource(Res.string.label_evaluation_swipe_delete)

	ElevatedCard(
		modifier = modifier
			.testTag(EvaluationsUiTags.evaluationItemCard(item.evaluationId))
			.clickable(
				onClickLabel = showActionsLabel,
				onClick = onCardClick
			)
			// The swipe gesture is invisible to screen readers, so the row actions
			// are also exposed as custom accessibility actions.
			.semantics {
				customActions = listOfNotNull(
					onEditAction?.let { action ->
						CustomAccessibilityAction(editActionLabel) {
							action()
							true
						}
					},
					onDeleteAction?.let { action ->
						CustomAccessibilityAction(deleteActionLabel) {
							action()
							true
						}
					}
				)
			}
			.fillMaxWidth()
			.padding(
				horizontal = EvaluationCardHorizontalPadding,
				vertical = EvaluationCardVerticalPadding
			)
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(EvaluationCardContentPadding)
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
						)
					}

					Box(
						modifier = Modifier
							.testTag(EvaluationsUiTags.EvaluationStatusChip)
							.background(
								color = statusColors.container,
								shape = RoundedCornerShape(TuIndiceRadius.Small)
							)
							.padding(horizontal = 10.dp, vertical = 5.dp)
					) {
						Text(
							text = item.statusText,
							color = statusColors.content,
							style = MaterialTheme.typography.labelLarge,
						)
					}
				}

				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(top = EvaluationSubjectTopPadding),
					verticalAlignment = Alignment.CenterVertically
				) {
					SubjectCodeChip(
						subjectCode = item.subjectCodeText,
						variant = SubjectCodeChipVariant.Dense,
						containerColor = item.subjectCodeContainerColor,
						contentColor = item.subjectCodeColor
					)
				}

				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(top = EvaluationMetadataTopPadding),
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
								.testTag(EvaluationsUiTags.evaluationGradeActionButton(item.evaluationId))
								.clickable(
									onClickLabel = stringResource(Res.string.a11y_evaluation_register_grade),
									role = Role.Button,
									onClick = onGradeClick
								)
								.border(
									border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
									shape = RoundedCornerShape(TuIndiceRadius.Large)
								)
								.padding(
									horizontal = EvaluationGradeHorizontalPadding,
									vertical = EvaluationGradeVerticalPadding
								),
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

private val EvaluationCardHorizontalPadding = 16.dp
private val EvaluationCardVerticalPadding = 6.dp
private val EvaluationCardContentPadding = 14.dp
private val EvaluationSubjectTopPadding = 8.dp
private val EvaluationMetadataTopPadding = 12.dp
private val EvaluationGradeHorizontalPadding = 20.dp
private val EvaluationGradeVerticalPadding = 8.dp

private data class StatusColors(
	val container: Color,
	val content: Color
)

@Composable
private fun statusColors(tone: EvaluationHighlightTone): StatusColors {
	return when (tone) {
		EvaluationHighlightTone.Success -> StatusColors(
			container = MaterialTheme.colorScheme.surfaceContainerHighest,
			content = MaterialTheme.colorScheme.onSurface
		)

		EvaluationHighlightTone.Error -> StatusColors(
			container = MaterialTheme.colorScheme.errorContainer,
			content = MaterialTheme.colorScheme.onErrorContainer
		)

		EvaluationHighlightTone.Neutral -> StatusColors(
			container = MaterialTheme.colorScheme.surfaceContainerHighest,
			content = MaterialTheme.colorScheme.onSurface
		)
	}
}
