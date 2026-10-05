package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_day_friday
import tuindice.record.generated.resources.schedule_day_friday_short
import tuindice.record.generated.resources.schedule_day_monday
import tuindice.record.generated.resources.schedule_day_monday_short
import tuindice.record.generated.resources.schedule_day_saturday
import tuindice.record.generated.resources.schedule_day_saturday_short
import tuindice.record.generated.resources.schedule_day_sunday
import tuindice.record.generated.resources.schedule_day_sunday_short
import tuindice.record.generated.resources.schedule_day_thursday
import tuindice.record.generated.resources.schedule_day_thursday_short
import tuindice.record.generated.resources.schedule_day_today
import tuindice.record.generated.resources.schedule_day_tuesday
import tuindice.record.generated.resources.schedule_day_tuesday_short
import tuindice.record.generated.resources.schedule_day_wednesday
import tuindice.record.generated.resources.schedule_day_wednesday_short

/** The column header of a day: "Lun". */
internal fun ScheduleDay.toShortNameText(): UiText = UiText.Resource(
	when (this) {
		ScheduleDay.Monday -> Res.string.schedule_day_monday_short
		ScheduleDay.Tuesday -> Res.string.schedule_day_tuesday_short
		ScheduleDay.Wednesday -> Res.string.schedule_day_wednesday_short
		ScheduleDay.Thursday -> Res.string.schedule_day_thursday_short
		ScheduleDay.Friday -> Res.string.schedule_day_friday_short
		ScheduleDay.Saturday -> Res.string.schedule_day_saturday_short
		ScheduleDay.Sunday -> Res.string.schedule_day_sunday_short
	}
)

/** What a screen reader says of today's column header: "lunes, hoy". */
internal fun ScheduleDay.toTodayText(): UiText {
	return UiText.Resource(Res.string.schedule_day_today, listOf(toNameText()))
}

/** The name of a day as a screen reader says it: "lunes". */
internal fun ScheduleDay.toNameText(): UiText = UiText.Resource(
	when (this) {
		ScheduleDay.Monday -> Res.string.schedule_day_monday
		ScheduleDay.Tuesday -> Res.string.schedule_day_tuesday
		ScheduleDay.Wednesday -> Res.string.schedule_day_wednesday
		ScheduleDay.Thursday -> Res.string.schedule_day_thursday
		ScheduleDay.Friday -> Res.string.schedule_day_friday
		ScheduleDay.Saturday -> Res.string.schedule_day_saturday
		ScheduleDay.Sunday -> Res.string.schedule_day_sunday
	}
)
