package com.gdavidpb.tuindice.assistant.ui.component.horario

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.mapper.scheduleDayText
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.model.CardFigure
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.mapper.toClockRangeText
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.chatcontract.component.ClashesComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_clashes_caption
import tuindice.assistant.generated.resources.assistant_card_a_clashes_description
import tuindice.assistant.generated.resources.assistant_card_a_clashes_label
import tuindice.assistant.generated.resources.assistant_card_a_joined

// horario.choques/1: how many clashes the schedule has, then one row per clash with the subjects that meet at the same
// time, and the day and the hours of the clash.
@Composable
fun ClashesComponentView(
	component: ClashesComponent,
	modifier: Modifier = Modifier
) {
	val count = component.clashes.size.toString()

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Schedule.clashes(component.id))) {
		CardLabelView(text = stringResource(Res.string.assistant_card_a_clashes_label))
		CardFiguresView(
			primary = CardFigure(
				value = count,
				caption = stringResource(Res.string.assistant_card_a_clashes_caption),
				contentDescription = stringResource(Res.string.assistant_card_a_clashes_description, count)
			)
		)
		component.clashes.forEachIndexed { index, clash ->
			val codes = clash.subjectCodes.reduceOrNull { first, next ->
				stringResource(Res.string.assistant_card_a_joined, first, next)
			}.orEmpty()
			val hours: UiText = (clash.startMinute..clash.endMinute).toClockRangeText()
			val day = scheduleDayText(clash.dayOfWeek)
			val dayAndHours = if (day != null) {
				UiText.Resource(Res.string.assistant_card_a_joined, listOf(day, hours))
			} else {
				hours
			}

			CardRowView(
				code = codes,
				name = dayAndHours.asString(),
				showsDivider = index > 0
			)
		}
	}
}
