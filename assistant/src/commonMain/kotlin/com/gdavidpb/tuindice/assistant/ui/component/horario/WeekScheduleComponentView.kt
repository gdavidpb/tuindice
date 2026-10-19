package com.gdavidpb.tuindice.assistant.ui.component.horario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.model.WeekScheduleItem
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
import tuindice.assistant.generated.resources.assistant_card_a_week_evaluation
import tuindice.assistant.generated.resources.assistant_card_a_week_label

// horario.semana/1: the classes of the week as the figure, then each day with its class blocks: the code and the
// room of each, its hours, and a chip where an evaluation falls.
@Composable
fun WeekScheduleComponentView(
	item: WeekScheduleItem,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Schedule.week(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_a_week_label))
		CardFiguresView(primary = item.classesFigure.toCardFigure())
		item.days.forEach { day ->
			CardLabelView(text = day.name.asString())
			day.blocks.forEachIndexed { index, block ->
				CardRowView(
					code = block.code,
					name = block.room,
					showsDivider = index > 0,
					trailing = {
						Row(
							horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small),
							verticalAlignment = Alignment.CenterVertically
						) {
							if (block.hasEvaluation) {
								CardChipView(text = stringResource(Res.string.assistant_card_a_week_evaluation))
							}
							CardValueView(text = block.hoursText.asString())
						}
					}
				)
			}
		}
	}
}
