package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicRecordTable

@Entity(tableName = AcademicRecordTable.TABLE_NAME)
data class AcademicRecordEntity(
	@PrimaryKey
	@ColumnInfo(name = AcademicRecordTable.ID)
	val id: String,
	@ColumnInfo(name = AcademicRecordTable.REVISION)
	val revision: Long,
	@ColumnInfo(name = AcademicRecordTable.CURRICULUM_KEY)
	val curriculumKey: String,
	@ColumnInfo(name = AcademicRecordTable.UPDATED_AT)
	val updatedAt: Long
)
