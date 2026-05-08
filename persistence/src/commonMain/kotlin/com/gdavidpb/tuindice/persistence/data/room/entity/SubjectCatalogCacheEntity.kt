package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.SubjectCatalogCacheTable

@Entity(
	tableName = SubjectCatalogCacheTable.TABLE_NAME,
	indices = [
		Index(value = [SubjectCatalogCacheTable.NORMALIZED_CODE]),
		Index(value = [SubjectCatalogCacheTable.NORMALIZED_NAME])
	]
)
data class SubjectCatalogCacheEntity(
	@PrimaryKey
	@ColumnInfo(name = SubjectCatalogCacheTable.SUBJECT_CODE) val subjectCode: String,
	@ColumnInfo(name = SubjectCatalogCacheTable.NAME) val name: String,
	@ColumnInfo(name = SubjectCatalogCacheTable.CREDITS) val credits: Int,
	@ColumnInfo(name = SubjectCatalogCacheTable.GRADING_MODE) val gradingMode: String? = null,
	@ColumnInfo(name = SubjectCatalogCacheTable.NORMALIZED_CODE) val normalizedCode: String,
	@ColumnInfo(name = SubjectCatalogCacheTable.NORMALIZED_NAME) val normalizedName: String,
	@ColumnInfo(name = SubjectCatalogCacheTable.UPDATED_AT) val updatedAt: Long
)
