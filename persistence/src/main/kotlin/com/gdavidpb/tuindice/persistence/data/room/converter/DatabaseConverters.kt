package com.gdavidpb.tuindice.persistence.data.room.converter

import androidx.room.TypeConverter
import com.gdavidpb.tuindice.base.domain.model.EvaluationType

class DatabaseConverters {
	@TypeConverter
	fun intToBoolean(value: Int): Boolean = value != 0

	@TypeConverter
	fun booleanToInt(value: Boolean): Int = value.compareTo(false)

	@TypeConverter
	fun evaluationTypeToInt(value: EvaluationType): Int = value.ordinal

	@TypeConverter
	fun intToEvaluationType(value: Int): EvaluationType = EvaluationType.entries[value]
}