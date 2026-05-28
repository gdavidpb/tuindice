package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.SubjectStatsAttemptBinTable

@Entity(
	tableName = SubjectStatsAttemptBinTable.TABLE_NAME,
	indices = [
		Index(value = [SubjectStatsAttemptBinTable.SUBJECT_CODE]),
		Index(value = [SubjectStatsAttemptBinTable.SUBJECT_CODE, SubjectStatsAttemptBinTable.SEGMENT_TYPE, SubjectStatsAttemptBinTable.SEGMENT_KEY, SubjectStatsAttemptBinTable.BUCKET], unique = true)
	]
)
data class SubjectStatsAttemptBinEntity(
	@PrimaryKey
	@ColumnInfo(name = SubjectStatsAttemptBinTable.ID) val id: String,
	@ColumnInfo(name = SubjectStatsAttemptBinTable.SUBJECT_CODE) val subjectCode: String,
	@ColumnInfo(name = SubjectStatsAttemptBinTable.SEGMENT_TYPE) val segmentType: String,
	@ColumnInfo(name = SubjectStatsAttemptBinTable.SEGMENT_KEY) val segmentKey: Int? = null,
	@ColumnInfo(name = SubjectStatsAttemptBinTable.BUCKET) val bucket: String,
	@ColumnInfo(name = SubjectStatsAttemptBinTable.COUNT) val count: Int,
	@ColumnInfo(name = SubjectStatsAttemptBinTable.GENERATED_AT) val generatedAt: Long
)
