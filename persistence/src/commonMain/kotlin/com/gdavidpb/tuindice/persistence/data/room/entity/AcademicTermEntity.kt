package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicTermTable

@Entity(
	tableName = AcademicTermTable.TABLE_NAME,
	indices = [
		Index(value = [AcademicTermTable.TERM_ORDER]),
		Index(value = [AcademicTermTable.TERM_KEY], unique = true)
	]
)
data class AcademicTermEntity(
	@PrimaryKey
	@ColumnInfo(name = AcademicTermTable.ID) val id: String,
	@ColumnInfo(name = AcademicTermTable.PERIOD_YEAR) val periodYear: Int,
	@ColumnInfo(name = AcademicTermTable.PERIOD_CODE) val periodCode: String,
	@ColumnInfo(name = AcademicTermTable.TERM_KEY) val termKey: String,
	@ColumnInfo(name = AcademicTermTable.TERM_ORDER) val termOrder: Int,
	@ColumnInfo(name = AcademicTermTable.PERIOD_LABEL) val periodLabel: String,
	@ColumnInfo(name = AcademicTermTable.TERM_KIND) val kind: String,
	// Nullable on purpose: rows written before the anchor existed, and every non-HISTORICAL term,
	// carry no official average and must keep falling back to what the engine computes.
	@ColumnInfo(name = AcademicTermTable.OFFICIAL_PERIOD_AVERAGE) val officialPeriodAverage: Double? = null,
	@ColumnInfo(name = AcademicTermTable.OFFICIAL_CUMULATIVE_AVERAGE) val officialCumulativeAverage: Double? = null
)
