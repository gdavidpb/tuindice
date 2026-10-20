package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CardChipTone
import com.gdavidpb.tuindice.assistant.presentation.model.CardFigureItem
import com.gdavidpb.tuindice.assistant.presentation.model.ClashRowItem
import com.gdavidpb.tuindice.assistant.presentation.model.ClashesItem
import com.gdavidpb.tuindice.assistant.presentation.model.NextClassItem
import com.gdavidpb.tuindice.assistant.presentation.model.ScheduleBlockItem
import com.gdavidpb.tuindice.assistant.presentation.model.ScheduleDayItem
import com.gdavidpb.tuindice.assistant.presentation.model.WeekScheduleItem
import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.mapper.toClockRangeText
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.ClashesComponent
import com.gdavidpb.tuindice.chatcontract.component.NextClassComponent
import com.gdavidpb.tuindice.chatcontract.component.WeekScheduleComponent
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_next_caption
import tuindice.assistant.generated.resources.assistant_card_a_next_description
import tuindice.assistant.generated.resources.assistant_card_a_next_in_progress_label
import tuindice.assistant.generated.resources.assistant_card_a_next_label
import tuindice.assistant.generated.resources.assistant_card_a_outcome_pending
import tuindice.assistant.generated.resources.assistant_card_a_remaining_day
import tuindice.assistant.generated.resources.assistant_card_a_remaining_days
import tuindice.assistant.generated.resources.assistant_card_a_remaining_hours
import tuindice.assistant.generated.resources.assistant_card_a_remaining_minutes
import tuindice.assistant.generated.resources.assistant_card_a_see_all_clashes
import tuindice.assistant.generated.resources.assistant_card_a_week_evaluation
import tuindice.assistant.generated.resources.assistant_card_b_joined

private const val MINUTES_PER_HOUR = 60
private const val MINUTES_PER_DAY = 1_440
private const val MILLIS_PER_MINUTE = 60_000L

internal fun WeekScheduleComponent.toWeekScheduleItem(): WeekScheduleItem {
	return WeekScheduleItem(
		id = id,
		opensSheet = opensSheet(),
		// A day outside the week has no name, and a day with no classes has nothing to show.
		days = days.mapNotNull { day ->
			scheduleDayText(day.dayOfWeek)
				?.takeIf { day.blocks.isNotEmpty() }
				?.let { name ->
					ScheduleDayItem(
						name = name,
						blocks = day.blocks.map { block ->
							ScheduleBlockItem(
								code = block.subjectCode,
								room = block.room.orEmpty(),
								evaluationChip = if (block.evaluationTitles.isNotEmpty()) {
									chatCardChip(Res.string.assistant_card_a_week_evaluation, CardChipTone.Neutral)
								} else {
									null
								},
								hoursText = (block.startMinute..block.endMinute).toClockRangeText()
							)
						}
					)
				}
		}
	)
}

// [nowMillis] is the moment the card is read: what is left until the class starts is counted from it.
internal fun NextClassComponent.toNextClassItem(nowMillis: Long): NextClassItem {
	val start = dateMillis + startMinute * MILLIS_PER_MINUTE
	val minutesLeft = (start - nowMillis) / MILLIS_PER_MINUTE
	val classHours = (startMinute..endMinute).toClockRangeText()

	return NextClassItem(
		id = id,
		opensSheet = opensSheet(),
		labelText = UiText.Resource(
			if (inProgress) Res.string.assistant_card_a_next_in_progress_label else Res.string.assistant_card_a_next_label
		),
		classFigure = CardFigureItem(
			value = classHours,
			caption = UiText.Resource(Res.string.assistant_card_a_next_caption),
			contentDescription = UiText.Resource(Res.string.assistant_card_a_next_description, listOf(classHours))
		),
		subjectCode = subjectCode,
		subjectName = subjectName,
		inProgress = inProgress,
		statusChip = if (inProgress) {
			chatCardChip(Res.string.assistant_card_a_outcome_pending, CardChipTone.Current)
		} else {
			null
		},
		dayText = dateMillis.formatDate(DateTextStyle.SHORT_WEEKDAY_NUMERIC_DATE),
		roomText = room?.takeIf { text -> text.isNotBlank() },
		remainingText = remainingText(minutesLeft = minutesLeft, inProgress = inProgress)
	)
}

// What is left until a class starts, in the largest unit that reads well: minutes, then hours, then days.
private fun remainingText(minutesLeft: Long, inProgress: Boolean): UiText? {
	return when {
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
}

internal fun ClashesComponent.toClashesItem(): ClashesItem {
	return ClashesItem(
		id = id,
		opensSheet = opensSheet(),
		rows = clashes.map { clash ->
			val hours = (clash.startMinute..clash.endMinute).toClockRangeText()
			val day = scheduleDayText(clash.dayOfWeek)

			ClashRowItem(
				codes = clash.subjectCodes
					.map<String, UiText> { code -> UiText.Raw(code) }
					.reduceOrNull { first, next -> UiText.Resource(Res.string.assistant_card_b_joined, listOf(first, next)) }
					?: UiText.Empty,
				dayAndHoursText = if (day != null) {
					UiText.Resource(Res.string.assistant_card_b_joined, listOf(day, hours))
				} else {
					hours
				}
			)
		},
		seeAllText = UiText.Resource(Res.string.assistant_card_a_see_all_clashes, listOf(clashes.size))
	)
}
