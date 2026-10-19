package com.gdavidpb.tuindice.assistant.ui.component.horario

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardChipView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardLabelView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardRowView
import com.gdavidpb.tuindice.assistant.ui.component.common.CardValueView
import com.gdavidpb.tuindice.assistant.ui.model.CardFigure
import com.gdavidpb.tuindice.assistant.ui.component.common.CardFiguresView
import com.gdavidpb.tuindice.assistant.ui.view.AssistantEmbeddedCardView
import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.mapper.toClockRangeText
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceClock
import com.gdavidpb.tuindice.chatcontract.component.NextClassComponent
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_next_caption
import tuindice.assistant.generated.resources.assistant_card_a_next_day
import tuindice.assistant.generated.resources.assistant_card_a_next_description
import tuindice.assistant.generated.resources.assistant_card_a_next_in_progress
import tuindice.assistant.generated.resources.assistant_card_a_next_in_progress_label
import tuindice.assistant.generated.resources.assistant_card_a_next_label
import tuindice.assistant.generated.resources.assistant_card_a_next_remaining
import tuindice.assistant.generated.resources.assistant_card_a_next_room
import tuindice.assistant.generated.resources.assistant_card_a_remaining_day
import tuindice.assistant.generated.resources.assistant_card_a_remaining_days
import tuindice.assistant.generated.resources.assistant_card_a_remaining_hours
import tuindice.assistant.generated.resources.assistant_card_a_remaining_minutes
import kotlin.time.ExperimentalTime

private const val MINUTES_PER_HOUR = 60
private const val MINUTES_PER_DAY = 1_440
private const val MILLIS_PER_MINUTE = 60_000L

// horario.proxima/1: the hours of the next class, or of the one in progress, with its subject, its room, its day
// and how long is left until it starts. dateMillis is the start of the day of the class, so the class starts that
// many minutes after it.
@OptIn(ExperimentalTime::class)
@Composable
fun NextClassComponentView(
	component: NextClassComponent,
	modifier: Modifier = Modifier
) {
	val now = LocalTuIndiceClock.current.now().toEpochMilliseconds()
	val start = component.dateMillis + component.startMinute * MILLIS_PER_MINUTE
	val minutesLeft = (start - now) / MILLIS_PER_MINUTE
	val classHours = (component.startMinute..component.endMinute).toClockRangeText().asString()
	val day = component.dateMillis.formatDate(DateTextStyle.SHORT_WEEKDAY_NUMERIC_DATE).asString()
	val remaining = remainingText(minutesLeft = minutesLeft, inProgress = component.inProgress)
	val label = if (component.inProgress) {
		Res.string.assistant_card_a_next_in_progress_label
	} else {
		Res.string.assistant_card_a_next_label
	}

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Schedule.nextClass(component.id))) {
		CardLabelView(text = stringResource(label))
		CardFiguresView(
			primary = CardFigure(
				value = classHours,
				caption = stringResource(Res.string.assistant_card_a_next_caption),
				contentDescription = stringResource(Res.string.assistant_card_a_next_description, classHours)
			)
		)
		CardRowView(
			code = component.subjectCode,
			name = component.subjectName,
			showsDivider = false,
			trailing = {
				if (component.inProgress) {
					CardChipView(text = stringResource(Res.string.assistant_card_a_next_in_progress))
				}
			}
		)
		CardRowView(
			name = stringResource(Res.string.assistant_card_a_next_day),
			trailing = { CardValueView(text = day) }
		)
		component.room?.takeIf { room -> room.isNotBlank() }?.let { room ->
			CardRowView(
				name = stringResource(Res.string.assistant_card_a_next_room),
				trailing = { CardValueView(text = room) }
			)
		}
		remaining?.let { left ->
			CardRowView(
				name = stringResource(Res.string.assistant_card_a_next_remaining),
				trailing = { CardValueView(text = left.asString()) }
			)
		}
	}
}

// What is left until a class starts, in the largest unit that reads well: minutes, then hours, then days. A class in
// progress, or one that has already started, leaves nothing to count down.
private fun remainingText(minutesLeft: Long, inProgress: Boolean): UiText? =
	when {
		inProgress || minutesLeft <= 0 -> null
		minutesLeft < MINUTES_PER_HOUR -> UiText.Resource(
			Res.string.assistant_card_a_remaining_minutes,
			listOf(minutesLeft)
		)
		minutesLeft < MINUTES_PER_DAY -> UiText.Resource(
			Res.string.assistant_card_a_remaining_hours,
			listOf(minutesLeft / MINUTES_PER_HOUR)
		)
		minutesLeft < 2 * MINUTES_PER_DAY -> UiText.Resource(Res.string.assistant_card_a_remaining_day)
		else -> UiText.Resource(
			Res.string.assistant_card_a_remaining_days,
			listOf(minutesLeft / MINUTES_PER_DAY)
		)
	}
