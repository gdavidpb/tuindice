package com.gdavidpb.tuindice.data.source.room.utils

import androidx.room.TypeConverter
import java.util.*

class DatabaseConverters {
	@TypeConverter
	fun dateToLong(date: Date): Long = date.time

	@TypeConverter
	fun longToDate(long: Long): Date = Date(long)

	@TypeConverter
	fun intToBoolean(int: Int): Boolean = int != 0

	@TypeConverter
	fun booleanToInt(boolean: Boolean): Int = boolean.compareTo(false)
}