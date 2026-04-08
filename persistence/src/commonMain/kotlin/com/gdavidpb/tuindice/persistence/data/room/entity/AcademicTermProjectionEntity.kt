package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicTermProjectionTable

@Entity(
	tableName = AcademicTermProjectionTable.TABLE_NAME,
	primaryKeys = [AcademicTermProjectionTable.VIEW_MODE, AcademicTermProjectionTable.ID],
	indices = [Index(value = [AcademicTermProjectionTable.RECORD_ID, AcademicTermProjectionTable.VIEW_MODE])]
)
data class AcademicTermProjectionEntity(
	@ColumnInfo(name = AcademicTermProjectionTable.VIEW_MODE)
	val viewMode: String,
	@ColumnInfo(name = AcademicTermProjectionTable.ID)
	val id: String,
	@ColumnInfo(name = AcademicTermProjectionTable.RECORD_ID)
	val recordId: String,
	@ColumnInfo(name = AcademicTermProjectionTable.LABEL)
	val label: String,
	@ColumnInfo(name = AcademicTermProjectionTable.START_AT)
	val startAt: Long,
	@ColumnInfo(name = AcademicTermProjectionTable.END_AT)
	val endAt: Long,
	@ColumnInfo(name = AcademicTermProjectionTable.ORDER)
	val order: Int,
	@ColumnInfo(name = AcademicTermProjectionTable.CURRENT)
	val current: Boolean = false,
	@ColumnInfo(name = AcademicTermProjectionTable.CLOSED)
	val closed: Boolean,
	@ColumnInfo(name = AcademicTermProjectionTable.EDITABLE)
	val editable: Boolean,
	@ColumnInfo(name = AcademicTermProjectionTable.SYNTHETIC)
	val synthetic: Boolean,
	@ColumnInfo(name = AcademicTermProjectionTable.GRADE)
	val grade: Double,
	@ColumnInfo(name = AcademicTermProjectionTable.GRADE_SUM)
	val gradeSum: Double,
	@ColumnInfo(name = AcademicTermProjectionTable.CREDITS)
	val credits: Int,
	@ColumnInfo(name = AcademicTermProjectionTable.CREDITS_SUM)
	val creditsSum: Int
)
