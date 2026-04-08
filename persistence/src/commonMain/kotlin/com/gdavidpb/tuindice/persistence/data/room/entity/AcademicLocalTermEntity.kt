package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicLocalTermTable

@Entity(
	tableName = AcademicLocalTermTable.TABLE_NAME,
	indices = [Index(value = [AcademicLocalTermTable.RECORD_ID, AcademicLocalTermTable.START_AT])]
)
data class AcademicLocalTermEntity(
	@PrimaryKey
	@ColumnInfo(name = AcademicLocalTermTable.ID)
	val id: String,
	@ColumnInfo(name = AcademicLocalTermTable.RECORD_ID)
	val recordId: String,
	@ColumnInfo(name = AcademicLocalTermTable.LABEL)
	val label: String,
	@ColumnInfo(name = AcademicLocalTermTable.START_AT)
	val startAt: Long,
	@ColumnInfo(name = AcademicLocalTermTable.END_AT)
	val endAt: Long,
	@ColumnInfo(name = AcademicLocalTermTable.ORDER)
	val order: Int,
	@ColumnInfo(name = AcademicLocalTermTable.CLOSED)
	val closed: Boolean,
	@ColumnInfo(name = AcademicLocalTermTable.EDITABLE)
	val editable: Boolean,
	@ColumnInfo(name = AcademicLocalTermTable.SYNTHETIC)
	val synthetic: Boolean
)
