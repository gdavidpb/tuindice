package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.SubjectDetailTable

@Entity(tableName = SubjectDetailTable.TABLE_NAME)
data class SubjectDetailEntity(
	@PrimaryKey
	@ColumnInfo(name = SubjectDetailTable.SUBJECT_CODE) val subjectCode: String,
	@ColumnInfo(name = SubjectDetailTable.NAME) val name: String? = null,
	@ColumnInfo(name = SubjectDetailTable.CREDITS) val credits: Int? = null,
	@ColumnInfo(name = SubjectDetailTable.GRADING_MODE) val gradingMode: String? = null,
	@ColumnInfo(name = SubjectDetailTable.CACHE_STATUS) val cacheStatus: String,
	@ColumnInfo(name = SubjectDetailTable.GENERATED_AT) val generatedAt: Long,
	@ColumnInfo(name = SubjectDetailTable.EXPIRES_AT) val expiresAt: Long
)
