package com.gdavidpb.tuindice.data.source.database.dao.converter

import androidx.room.TypeConverter
import com.gdavidpb.tuindice.domain.model.QuarterStatus
import com.gdavidpb.tuindice.domain.model.SubjectStatus
import java.util.*

class DatabaseConverters {
    @TypeConverter
    fun subjectStatusToInt(status: SubjectStatus): Int = status.value

    @TypeConverter
    fun intToSubjectStatus(int: Int): SubjectStatus = SubjectStatus.fromValue(int)

    @TypeConverter
    fun dateToLong(date: Date): Long = date.time

    @TypeConverter
    fun longToDate(long: Long): Date = Date(long)

    @TypeConverter
    fun intToBoolean(int: Int): Boolean = int != 0

    @TypeConverter
    fun booleanToInt(boolean: Boolean): Int = boolean.compareTo(false)

    @TypeConverter
    fun quarterTypeToInt(status: QuarterStatus): Int = status.value

    @TypeConverter
    fun intToQuarterType(int: Int): QuarterStatus = QuarterStatus.fromValue(int)
}