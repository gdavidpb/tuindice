package com.gdavidpb.tuindice.assistant.ui.component.informe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.mapper.toCardIndexText
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardChipView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.model.toCardIndexFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.chatcontract.component.IndexGoalComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_goal_caption
import tuindice.assistant.generated.resources.assistant_card_a_goal_credits
import tuindice.assistant.generated.resources.assistant_card_a_goal_description
import tuindice.assistant.generated.resources.assistant_card_a_goal_feasible
import tuindice.assistant.generated.resources.assistant_card_a_goal_label
import tuindice.assistant.generated.resources.assistant_card_a_goal_not_feasible
import tuindice.assistant.generated.resources.assistant_card_a_goal_target

// informe.meta/1: the term index the goal needs as the figure, then the cumulative goal, with a chip for whether it can
// be reached, and the credits the goal counts.
@Composable
fun IndexGoalComponentView(
	component: IndexGoalComponent,
	modifier: Modifier = Modifier
) {
	val required = component.requiredTermIndex?.toCardIndexFigure(
		caption = Res.string.assistant_card_a_goal_caption,
		description = Res.string.assistant_card_a_goal_description
	)
	val feasibility = if (component.feasible) {
		Res.string.assistant_card_a_goal_feasible
	} else {
		Res.string.assistant_card_a_goal_not_feasible
	}

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Report.indexGoal(component.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_a_goal_label))
		required?.let { figure -> CardFiguresView(primary = figure) }
		CardRowView(
			name = stringResource(Res.string.assistant_card_a_goal_target),
			showsDivider = false,
			trailing = {
				Row(
					horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small),
					verticalAlignment = Alignment.CenterVertically
				) {
					CardChipView(text = stringResource(feasibility))
					CardValueView(text = component.targetCumulative.toCardIndexText())
				}
			}
		)
		CardRowView(
			name = stringResource(Res.string.assistant_card_a_goal_credits),
			trailing = { CardValueView(text = component.credits.toString()) }
		)
	}
}
