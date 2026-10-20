package com.gdavidpb.tuindice.assistant.ui.component.evaluaciones

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.RequiredGradeItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLinkItemView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardTrailingView
import com.gdavidpb.tuindice.assistant.ui.model.toCardFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_required_label

// evaluaciones.meta/1: the points the target grade still asks for are the figure, and the points left to evaluate sit
// beside them. Without the points the target asks for, the points left to evaluate are the figure. The row names the
// subject, its target and whether the target is reachable.
@Composable
fun RequiredGradeComponentView(
	item: RequiredGradeItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Evaluations.requiredGrade(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_required_label))
		CardFiguresView(
			primary = item.primaryFigure.toCardFigure(),
			secondary = item.secondaryFigure?.toCardFigure()
		)
		CardRowView(
			code = item.subjectCode,
			name = item.subjectName,
			trailing = {
				CardTrailingView(value = item.targetText.asString(), chips = listOf(item.feasibleChip))
			}
		)
		item.recordLink?.let { link -> CardLinkItemView(link = link, onAction = onAction) }
	}
}
