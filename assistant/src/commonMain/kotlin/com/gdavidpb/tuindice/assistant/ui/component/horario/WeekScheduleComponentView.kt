package com.gdavidpb.tuindice.assistant.ui.component.horario

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.model.WeekScheduleItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardBodyTextView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardSectionLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardTrailingView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_week_empty
import tuindice.assistant.generated.resources.assistant_card_a_week_label

// horario.semana/1: each day with its class blocks: the code and the room of each, its hours, and the chip where an
// evaluation falls. With no class this week, a line says so.
@Composable
fun WeekScheduleComponentView(
	item: WeekScheduleItem,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Schedule.week(item.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_a_week_label))
		if (item.days.isEmpty()) {
			CardBodyTextView(text = stringResource(Res.string.assistant_card_a_week_empty), muted = true)
		}
		item.days.forEach { day ->
			CardSectionLabelView(text = day.name.asString())
			day.blocks.forEachIndexed { index, block ->
				CardRowView(
					code = block.code,
					name = block.room,
					showsDivider = index > 0,
					trailing = {
						CardTrailingView(
							value = block.hoursText.asString(),
							chips = listOfNotNull(block.evaluationChip)
						)
					}
				)
			}
		}
	}
}
