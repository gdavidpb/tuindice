package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import com.gdavidpb.tuindice.base.presentation.model.EvaluationTone
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.model.EvaluationCardElementModifiers
import com.gdavidpb.tuindice.base.ui.model.EvaluationCardItem
import com.gdavidpb.tuindice.base.ui.view.EvaluationCardView
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationHighlightTone
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationItem
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.evaluations.ui.model.EvaluationItemActions
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.a11y_evaluation_register_grade
import tuindice.evaluations.generated.resources.a11y_evaluation_show_actions
import tuindice.evaluations.generated.resources.label_evaluation_swipe_ask
import tuindice.evaluations.generated.resources.label_evaluation_swipe_delete
import tuindice.evaluations.generated.resources.label_evaluation_swipe_edit

// The row of one evaluation in the Evaluations tab: the card that every list of evaluations draws, with the tags, the
// swipe actions and the grade button of this tab.
@Composable
fun EvaluationItemView(
	modifier: Modifier = Modifier,
	item: EvaluationItem,
	actions: EvaluationItemActions
) {
	val showActionsLabel = stringResource(Res.string.a11y_evaluation_show_actions)
	val editActionLabel = stringResource(Res.string.label_evaluation_swipe_edit)
	val deleteActionLabel = stringResource(Res.string.label_evaluation_swipe_delete)
	val askActionLabel = stringResource(Res.string.label_evaluation_swipe_ask)
	val registerGradeLabel = stringResource(Res.string.a11y_evaluation_register_grade)

	EvaluationCardView(
		item = EvaluationCardItem(
			name = item.typeNameText,
			subjectCode = item.subjectCodeText,
			dateText = item.dateText.asString(),
			highlightTone = item.highlightTone.toEvaluationTone(),
			typeIcon = item.typeIcon,
			place = item.place,
			gradeText = if (item.showsGradeAction) item.gradeText else null,
			statusText = item.statusText,
			statusTone = item.statusTone.toEvaluationTone()
		),
		modifier = modifier,
		cardModifier = Modifier
			.testTag(EvaluationsUiTags.evaluationItemCard(item.evaluationId))
			.clickable(
				onClickLabel = showActionsLabel,
				onClick = actions.onCardClick
			)
			// The swipe gesture is invisible to screen readers, so the row actions
			// are also exposed as custom accessibility actions.
			.semantics {
				customActions = listOfNotNull(
					actions.onAskAssistant?.let { ask ->
						CustomAccessibilityAction(askActionLabel) {
							ask()
							true
						}
					},
					actions.onEditAction?.let { action ->
						CustomAccessibilityAction(editActionLabel) {
							action()
							true
						}
					},
					actions.onDeleteAction?.let { action ->
						CustomAccessibilityAction(deleteActionLabel) {
							action()
							true
						}
					}
				)
			},
		elementModifiers = EvaluationCardElementModifiers(
			typeIcon = Modifier.testTag(EvaluationsUiTags.EvaluationTypeInlineIcon),
			place = Modifier.testTag(EvaluationsUiTags.evaluationPlace(item.evaluationId)),
			grade = Modifier
				.testTag(EvaluationsUiTags.evaluationGradeActionButton(item.evaluationId))
				.clickable(
					onClickLabel = registerGradeLabel,
					role = Role.Button,
					onClick = actions.onGradeClick
				),
			status = Modifier.testTag(EvaluationsUiTags.EvaluationStatusChip)
		)
	)
}

private fun EvaluationHighlightTone.toEvaluationTone(): EvaluationTone =
	when (this) {
		EvaluationHighlightTone.Success -> EvaluationTone.Success
		EvaluationHighlightTone.Error -> EvaluationTone.Error
		EvaluationHighlightTone.Neutral -> EvaluationTone.Neutral
	}
