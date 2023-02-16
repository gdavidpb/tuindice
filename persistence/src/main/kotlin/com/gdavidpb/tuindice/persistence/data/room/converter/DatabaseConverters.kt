package com.gdavidpb.tuindice.persistence.data.room.converter

import androidx.room.TypeConverter
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.persistence.data.room.model.SyncEntityAction
import com.gdavidpb.tuindice.persistence.data.room.model.SyncEntityType
import java.util.*

class DatabaseConverters {
	@TypeConverter
	fun dateToLong(value: Date): Long = value.time

	@TypeConverter
	fun longToDate(value: Long): Date = Date(value)

	@TypeConverter
	fun intToBoolean(value: Int): Boolean = value != 0

	@TypeConverter
	fun booleanToInt(value: Boolean): Int = value.compareTo(false)

	@TypeConverter
	fun evaluationTypeToInt(value: EvaluationType): Int = value.ordinal

	@TypeConverter
	fun intToEvaluationType(value: Int): EvaluationType = EvaluationType.values()[value]

	@TypeConverter
	fun syncEntityActionToInt(value: SyncEntityAction): Int = value.ordinal

	@TypeConverter
	fun intToSyncEntityAction(value: Int): SyncEntityAction = SyncEntityAction.values()[value]

	@TypeConverter
	fun syncEntityTypeToInt(value: SyncEntityType): Int = value.ordinal

	@TypeConverter
	fun intToSyncEntityType(value: Int): SyncEntityType = SyncEntityType.values()[value]
}