package com.gdavidpb.tuindice.assistant.ui.component.informe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.model.IndexGoalItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardChipView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.toCardFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_goal_credits
import tuindice.assistant.generated.resources.assistant_card_a_goal_label
import tuindice.assistant.generated.resources.assistant_card_a_goal_target

// informe.meta/1: the term index the goal needs as the figure, then the cumulative goal, with a chip for whether it can
// be reached, and the credits the goal counts.
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
			showsDivider = false,
			trailing = {
				Row(
					horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small),
					verticalAlignment = Alignment.CenterVertically
				) {
					CardChipView(text = item.feasibilityText.asString())
					CardValueView(text = item.targetText)
				}
			}
		)
		CardRowView(
			name = stringResource(Res.string.assistant_card_a_goal_credits),
			trailing = { CardValueView(text = item.creditsText) }
		)
	}
}
