package com.gdavidpb.tuindice.assistant.ui.component.horario

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.model.NextClassItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardChipView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.toCardFigure
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_next_day
import tuindice.assistant.generated.resources.assistant_card_a_next_in_progress
import tuindice.assistant.generated.resources.assistant_card_a_next_remaining
import tuindice.assistant.generated.resources.assistant_card_a_next_room

// horario.proxima/1: the hours of the next class, or of the one in progress, with its subject, its room, its day
// and how long is left until it starts.
@Composable
fun NextClassComponentView(
	item: NextClassItem,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Schedule.nextClass(item.id))) {
		CardLabelView(text = item.labelText.asString())
		CardFiguresView(primary = item.classFigure.toCardFigure())
		CardRowView(
			code = item.subjectCode,
			name = item.subjectName,
			showsDivider = false,
			trailing = {
				if (item.inProgress) {
					CardChipView(text = stringResource(Res.string.assistant_card_a_next_in_progress))
				}
			}
		)
		CardRowView(
			name = stringResource(Res.string.assistant_card_a_next_day),
			trailing = { CardValueView(text = item.dayText.asString()) }
		)
		item.roomText?.let { room ->
			CardRowView(
				name = stringResource(Res.string.assistant_card_a_next_room),
				trailing = { CardValueView(text = room) }
			)
		}
		item.remainingText?.let { left ->
			CardRowView(
				name = stringResource(Res.string.assistant_card_a_next_remaining),
				trailing = { CardValueView(text = left.asString()) }
			)
		}
	}
}
