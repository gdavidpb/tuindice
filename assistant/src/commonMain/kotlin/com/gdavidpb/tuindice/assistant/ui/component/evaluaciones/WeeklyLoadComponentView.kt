package com.gdavidpb.tuindice.assistant.ui.component.evaluaciones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.mapper.chatCardCountText
import com.gdavidpb.tuindice.assistant.presentation.mapper.chatCardPointsText
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardChipView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFigure
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRecordLinkView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.chatcontract.component.WeeklyLoadComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_b_chip_heavy
import tuindice.assistant.generated.resources.assistant_card_b_count_evaluation_many
import tuindice.assistant.generated.resources.assistant_card_b_count_evaluation_one
import tuindice.assistant.generated.resources.assistant_card_b_joined
import tuindice.assistant.generated.resources.assistant_card_b_load_caption
import tuindice.assistant.generated.resources.assistant_card_b_load_description
import tuindice.assistant.generated.resources.assistant_card_b_load_label
import tuindice.assistant.generated.resources.assistant_card_b_week

// evaluaciones.carga/1: how many weeks are heavy, then a row per week: its number, its evaluations and its points, with
// a chip on a heavy week. The card links to the record it names, if it names one.
@Composable
fun WeeklyLoadComponentView(
	component: WeeklyLoadComponent,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val heavy = component.weeks.count { week -> week.heavy }.toString()

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Evaluations.load(component.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_b_load_label))
		CardFiguresView(
			primary = CardFigure(
				value = heavy,
				caption = stringResource(Res.string.assistant_card_b_load_caption),
				contentDescription = stringResource(Res.string.assistant_card_b_load_description, heavy)
			)
		)
		component.weeks.forEachIndexed { index, week ->
			val count = chatCardCountText(
				count = week.count,
				one = Res.string.assistant_card_b_count_evaluation_one,
				many = Res.string.assistant_card_b_count_evaluation_many
			).asString()
			val points = chatCardPointsText(week.points).asString()

			CardRowView(
				name = stringResource(Res.string.assistant_card_b_week, week.week),
				showsDivider = index > 0,
				trailing = {
					Row(
						horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small),
						verticalAlignment = Alignment.CenterVertically
					) {
						CardValueView(text = stringResource(Res.string.assistant_card_b_joined, count, points))
						if (week.heavy) {
							CardChipView(text = stringResource(Res.string.assistant_card_b_chip_heavy))
						}
					}
				}
			)
		}
		CardRecordLinkView(entityRefs = component.entityRefs, onAction = onAction)
	}
}
