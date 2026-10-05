package com.gdavidpb.tuindice.record.ui.model

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
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
import tuindice.record.generated.resources.schedule_day_tuesday
import tuindice.record.generated.resources.schedule_day_tuesday_short
import tuindice.record.generated.resources.schedule_day_wednesday
import tuindice.record.generated.resources.schedule_day_wednesday_short

/** The column header ("Lun") or, for screen readers, the spoken name ("lunes") of a day. */
@Composable
fun ScheduleDay.label(isShort: Boolean): String =
	stringResource(if (isShort) shortResource() else fullResource())

private fun ScheduleDay.shortResource(): StringResource = when (this) {
	ScheduleDay.Monday -> Res.string.schedule_day_monday_short
	ScheduleDay.Tuesday -> Res.string.schedule_day_tuesday_short
	ScheduleDay.Wednesday -> Res.string.schedule_day_wednesday_short
	ScheduleDay.Thursday -> Res.string.schedule_day_thursday_short
	ScheduleDay.Friday -> Res.string.schedule_day_friday_short
	ScheduleDay.Saturday -> Res.string.schedule_day_saturday_short
	ScheduleDay.Sunday -> Res.string.schedule_day_sunday_short
}

private fun ScheduleDay.fullResource(): StringResource = when (this) {
	ScheduleDay.Monday -> Res.string.schedule_day_monday
	ScheduleDay.Tuesday -> Res.string.schedule_day_tuesday
	ScheduleDay.Wednesday -> Res.string.schedule_day_wednesday
	ScheduleDay.Thursday -> Res.string.schedule_day_thursday
	ScheduleDay.Friday -> Res.string.schedule_day_friday
	ScheduleDay.Saturday -> Res.string.schedule_day_saturday
	ScheduleDay.Sunday -> Res.string.schedule_day_sunday
}
