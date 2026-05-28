package com.gdavidpb.tuindice.persistence.data.room.converter

import androidx.room.TypeConverter
import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode

class DatabaseConverters {
	@TypeConverter
	fun intToBoolean(value: Int): Boolean = value != 0

	@TypeConverter
	fun booleanToInt(value: Boolean): Int = value.compareTo(false)

	@TypeConverter
	fun evaluationScheduleModeToString(value: EvaluationScheduleMode): String = value.name

	@TypeConverter
	fun stringToEvaluationScheduleMode(value: String): EvaluationScheduleMode =
		EvaluationScheduleMode.valueOf(value)
}
