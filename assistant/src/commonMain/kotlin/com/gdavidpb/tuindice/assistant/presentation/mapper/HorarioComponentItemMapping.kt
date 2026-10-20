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
import com.gdavidpb.tuindice.base.presentation.mapper.classBlocksText
import com.gdavidpb.tuindice.base.presentation.mapper.classPlaceItem
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.mapper.toClockRangeText
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.ClashesComponent
import com.gdavidpb.tuindice.chatcontract.component.NextClassComponent
import com.gdavidpb.tuindice.chatcontract.component.WeekScheduleComponent
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_card_a_clash_block_many
import tuindice.assistant.generated.resources.assistant_card_a_clash_block_one
import tuindice.assistant.generated.resources.assistant_card_a_next_caption_many
import tuindice.assistant.generated.resources.assistant_card_a_next_caption_one
import tuindice.assistant.generated.resources.assistant_card_a_next_description_many
import tuindice.assistant.generated.resources.assistant_card_a_next_description_one
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
							val clock = block.startMinute..block.endMinute

							ScheduleBlockItem(
								code = block.subjectCode,
								room = block.room.orEmpty(),
								evaluationChip = if (block.evaluationTitles.isNotEmpty()) {
									chatCardChip(Res.string.assistant_card_a_week_evaluation, CardChipTone.Neutral)
								} else {
									null
								},
								scheduleText = classPlaceItem(
									startBlock = block.startBlock,
									endBlock = block.endBlock,
									classroom = "",
									clockRange = clock
								).scheduleText,
								hoursText = clock.toClockRangeText()
							)
						}
					)
				}
		}
	)
}

// [nowMillis] is the moment the card is read. The class is in progress while that moment falls within its hours, and
// what is left until it starts is counted from that moment too. The server's reading of the class, made when the turn
// ran, is not used: a card read later says where the class stands now.
internal fun NextClassComponent.toNextClassItem(nowMillis: Long): NextClassItem {
	val start = dateMillis + startMinute * MILLIS_PER_MINUTE
	val end = dateMillis + endMinute * MILLIS_PER_MINUTE
	val inProgress = nowMillis in start until end
	val minutesLeft = (start - nowMillis) / MILLIS_PER_MINUTE
	val blocks = classBlocksText(startBlock, endBlock)
	val hours = (startMinute..endMinute).toClockRangeText()
	val singleBlock = startBlock == endBlock
	val captionText = if (singleBlock) {
		Res.string.assistant_card_a_next_caption_one
	} else {
		Res.string.assistant_card_a_next_caption_many
	}
	val descriptionText = if (singleBlock) {
		Res.string.assistant_card_a_next_description_one
	} else {
		Res.string.assistant_card_a_next_description_many
	}

	return NextClassItem(
		id = id,
		opensSheet = opensSheet(),
		labelText = UiText.Resource(
			if (inProgress) Res.string.assistant_card_a_next_in_progress_label else Res.string.assistant_card_a_next_label
		),
		// The figure is the blocks of the class; its caption says the hours they fall in.
		classFigure = CardFigureItem(
			value = UiText.Raw(blocks),
			caption = UiText.Resource(captionText, listOf(hours)),
			contentDescription = UiText.Resource(descriptionText, listOf(blocks, hours))
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
			val day = scheduleDayText(clash.dayOfWeek)
			val blocks = clashBlocksText(clash.startBlock, clash.endBlock)

			ClashRowItem(
				codes = clash.subjectCodes
					.map<String, UiText> { code -> UiText.Raw(code) }
					.reduceOrNull { first, next -> UiText.Resource(Res.string.assistant_card_b_joined, listOf(first, next)) }
					?: UiText.Empty,
				dayAndBlocksText = if (day != null) {
					UiText.Resource(Res.string.assistant_card_b_joined, listOf(day, blocks))
				} else {
					blocks
				}
			)
		},
		seeAllText = UiText.Resource(Res.string.assistant_card_a_see_all_clashes, listOf(clashes.size))
	)
}

// "bloque 3" for a single block and "bloques 3-4" for several, with the blocks as the app writes them.
private fun clashBlocksText(startBlock: Int, endBlock: Int): UiText {
	val resource = if (startBlock == endBlock) {
		Res.string.assistant_card_a_clash_block_one
	} else {
		Res.string.assistant_card_a_clash_block_many
	}

	return UiText.Resource(resource, listOf(classBlocksText(startBlock, endBlock)))
}
