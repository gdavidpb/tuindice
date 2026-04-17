package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.SubjectStatsGradeBinTable

@Entity(
	tableName = SubjectStatsGradeBinTable.TABLE_NAME,
	indices = [
		Index(value = [SubjectStatsGradeBinTable.SUBJECT_CODE]),
		Index(value = [SubjectStatsGradeBinTable.SUBJECT_CODE, SubjectStatsGradeBinTable.SEGMENT_TYPE, SubjectStatsGradeBinTable.SEGMENT_KEY, SubjectStatsGradeBinTable.SERIES, SubjectStatsGradeBinTable.GRADE], unique = true)
	]
)
data class SubjectStatsGradeBinEntity(
	@PrimaryKey
	@ColumnInfo(name = SubjectStatsGradeBinTable.ID) val id: String,
	@ColumnInfo(name = SubjectStatsGradeBinTable.SUBJECT_CODE) val subjectCode: String,
	@ColumnInfo(name = SubjectStatsGradeBinTable.SEGMENT_TYPE) val segmentType: String,
	@ColumnInfo(name = SubjectStatsGradeBinTable.SEGMENT_KEY) val segmentKey: Int? = null,
	@ColumnInfo(name = SubjectStatsGradeBinTable.SERIES) val series: String,
	@ColumnInfo(name = SubjectStatsGradeBinTable.GRADE) val grade: Int,
	@ColumnInfo(name = SubjectStatsGradeBinTable.COUNT) val count: Int,
	@ColumnInfo(name = SubjectStatsGradeBinTable.GENERATED_AT) val generatedAt: Long
)
