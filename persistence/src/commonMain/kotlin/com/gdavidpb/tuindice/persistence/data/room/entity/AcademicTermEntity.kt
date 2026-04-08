package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicTermTable

@Entity(
	tableName = AcademicTermTable.TABLE_NAME,
	indices = [Index(value = [AcademicTermTable.RECORD_ID, AcademicTermTable.START_AT])]
)
data class AcademicTermEntity(
	@PrimaryKey
	@ColumnInfo(name = AcademicTermTable.ID)
	val id: String,
	@ColumnInfo(name = AcademicTermTable.RECORD_ID)
	val recordId: String,
	@ColumnInfo(name = AcademicTermTable.LABEL)
	val label: String,
	@ColumnInfo(name = AcademicTermTable.START_AT)
	val startAt: Long,
	@ColumnInfo(name = AcademicTermTable.END_AT)
	val endAt: Long,
	@ColumnInfo(name = AcademicTermTable.ORDER)
	val order: Int,
	@ColumnInfo(name = AcademicTermTable.CURRENT)
	val current: Boolean = false,
	@ColumnInfo(name = AcademicTermTable.CLOSED)
	val closed: Boolean,
	@ColumnInfo(name = AcademicTermTable.EDITABLE)
	val editable: Boolean,
	@ColumnInfo(name = AcademicTermTable.SYNTHETIC)
	val synthetic: Boolean,
	@ColumnInfo(name = AcademicTermTable.SOURCE_REPORTED_PERIOD_AVERAGE)
	val sourceReportedPeriodAverage: Double? = null,
	@ColumnInfo(name = AcademicTermTable.SOURCE_REPORTED_CUMULATIVE_AVERAGE)
	val sourceReportedCumulativeAverage: Double? = null
)
