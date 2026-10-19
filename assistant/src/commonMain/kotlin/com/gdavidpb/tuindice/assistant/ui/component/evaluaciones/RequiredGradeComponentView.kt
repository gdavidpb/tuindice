package com.gdavidpb.tuindice.assistant.ui.component.evaluaciones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.toCardCompactNumberText
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardChipView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRecordLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.CardFigure
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.chatcontract.component.RequiredGradeComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_feasible
import tuindice.assistant.generated.resources.assistant_card_b_not_feasible
import tuindice.assistant.generated.resources.assistant_card_b_required_caption
import tuindice.assistant.generated.resources.assistant_card_b_required_description
import tuindice.assistant.generated.resources.assistant_card_b_required_label
import tuindice.assistant.generated.resources.assistant_card_b_required_remaining_caption
import tuindice.assistant.generated.resources.assistant_card_b_required_remaining_description
import tuindice.assistant.generated.resources.assistant_card_b_required_target

// evaluaciones.meta/1: the points the target grade still asks for are the figure, and the points left to evaluate sit
// beside them. Without the points the target asks for, the points left to evaluate are the figure. The row names the
// subject, its target and whether the target is reachable.
@Composable
fun RequiredGradeComponentView(
	component: RequiredGradeComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val remaining = component.remainingPoints.toCardCompactNumberText()
	val remainingFigure = CardFigure(
		value = remaining,
		caption = stringResource(Res.string.assistant_card_b_required_remaining_caption),
		contentDescription = stringResource(Res.string.assistant_card_b_required_remaining_description, remaining)
	)
	val required = component.requiredPoints?.toCardCompactNumberText()
	val primary = required?.let { value ->
		CardFigure(
			value = value,
			caption = stringResource(Res.string.assistant_card_b_required_caption),
			contentDescription = stringResource(Res.string.assistant_card_b_required_description, value)
		)
	} ?: remainingFigure

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Evaluations.requiredGrade(component.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_required_label))
		CardFiguresView(primary = primary, secondary = if (required != null) remainingFigure else null)
		CardRowView(
			code = component.subjectCode,
			name = component.subjectName,
			trailing = {
				Row(
					horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small),
					verticalAlignment = Alignment.CenterVertically
				) {
					CardValueView(text = stringResource(Res.string.assistant_card_b_required_target, component.targetGrade))
					CardChipView(
						text = stringResource(
							if (component.feasible) Res.string.assistant_card_b_feasible else Res.string.assistant_card_b_not_feasible
						)
					)
				}
			}
		)
		CardRecordLinkView(entityRefs = component.entityRefs, onAction = onAction)
	}
}
