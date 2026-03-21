package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.QuarterTable

@Entity(
	tableName = QuarterTable.TABLE_NAME,
	indices = [
		Index(value = [QuarterTable.START_DATE, QuarterTable.END_DATE], unique = true),
		Index(value = [QuarterTable.NAME], unique = true)
	]
)
data class QuarterEntity(
	@PrimaryKey @ColumnInfo(name = QuarterTable.ID) val id: String,
	@ColumnInfo(name = QuarterTable.NAME) val name: String,
	@ColumnInfo(name = QuarterTable.START_DATE) val startDate: Long,
	@ColumnInfo(name = QuarterTable.END_DATE) val endDate: Long,
	@ColumnInfo(name = QuarterTable.GRADE) val grade: Double,
	@ColumnInfo(name = QuarterTable.GRADE_SUM) val gradeSum: Double,
	@ColumnInfo(name = QuarterTable.CREDITS) val credits: Int,
	@ColumnInfo(name = QuarterTable.CREDITS_SUM) val creditsSum: Int,
	@ColumnInfo(name = QuarterTable.IS_CURRENT) val isCurrent: Boolean,
	@ColumnInfo(name = QuarterTable.IS_READ_ONLY) val isReadOnly: Boolean,
	@ColumnInfo(name = QuarterTable.REVISION) val revision: Long
)
