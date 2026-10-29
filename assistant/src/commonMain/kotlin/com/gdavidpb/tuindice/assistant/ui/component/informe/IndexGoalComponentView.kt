package com.gdavidpb.tuindice.assistant.ui.component.informe

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.model.IndexGoalItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardTrailingView
import com.gdavidpb.tuindice.assistant.ui.model.toCardFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_goal_label
import tuindice.assistant.generated.resources.assistant_card_a_goal_target
import tuindice.assistant.generated.resources.assistant_card_a_goal_term_credits

// informe.meta/1: the term index the goal needs as the figure, then the cumulative goal with its chip, and the
// credits of the term the goal counts.
@Composable
fun IndexGoalComponentView(
	item: IndexGoalItem,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Report.indexGoal(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_a_goal_label))
		item.requiredFigure?.let { figure -> CardFiguresView(primary = figure.toCardFigure()) }
		CardRowView(
			name = stringResource(Res.string.assistant_card_a_goal_target),
			trailing = { CardTrailingView(value = item.targetText, chips = listOf(item.feasibilityChip)) }
		)
		CardRowView(
			name = stringResource(Res.string.assistant_card_a_goal_term_credits),
			showsDivider = true,
			trailing = { CardTrailingView(value = item.creditsText.asString(), chips = emptyList()) }
		)
	}
}
