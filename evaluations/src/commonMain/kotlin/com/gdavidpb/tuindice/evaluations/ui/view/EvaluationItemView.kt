package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import com.gdavidpb.tuindice.base.ui.model.EvaluationCardElementModifiers
import com.gdavidpb.tuindice.base.ui.view.EvaluationCardView
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationCardItem
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
		item = item.toEvaluationCardItem(),
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
