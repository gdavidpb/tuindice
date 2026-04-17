package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.SubjectStatsSegmentTable

@Entity(
	tableName = SubjectStatsSegmentTable.TABLE_NAME,
	indices = [
		Index(value = [SubjectStatsSegmentTable.SUBJECT_CODE]),
		Index(value = [SubjectStatsSegmentTable.SUBJECT_CODE, SubjectStatsSegmentTable.SEGMENT_TYPE, SubjectStatsSegmentTable.SEGMENT_KEY], unique = true)
	]
)
data class SubjectStatsSegmentEntity(
	@PrimaryKey
	@ColumnInfo(name = SubjectStatsSegmentTable.ID) val id: String,
	@ColumnInfo(name = SubjectStatsSegmentTable.SUBJECT_CODE) val subjectCode: String,
	@ColumnInfo(name = SubjectStatsSegmentTable.SEGMENT_TYPE) val segmentType: String,
	@ColumnInfo(name = SubjectStatsSegmentTable.SEGMENT_KEY) val segmentKey: Int? = null,
	@ColumnInfo(name = SubjectStatsSegmentTable.STATUS) val status: String,
	@ColumnInfo(name = SubjectStatsSegmentTable.GENERATED_AT) val generatedAt: Long,
	@ColumnInfo(name = SubjectStatsSegmentTable.EXPIRES_AT) val expiresAt: Long,
	@ColumnInfo(name = SubjectStatsSegmentTable.SAMPLE_STUDENTS) val sampleStudents: Int,
	@ColumnInfo(name = SubjectStatsSegmentTable.CLOSED_ATTEMPTS) val closedAttempts: Int,
	@ColumnInfo(name = SubjectStatsSegmentTable.NUMERIC_LATEST_STUDENTS) val numericLatestStudents: Int,
	@ColumnInfo(name = SubjectStatsSegmentTable.LATEST_APPROVED_COUNT) val latestApprovedCount: Int,
	@ColumnInfo(name = SubjectStatsSegmentTable.LATEST_FAILED_COUNT) val latestFailedCount: Int,
	@ColumnInfo(name = SubjectStatsSegmentTable.LATEST_RETIRED_COUNT) val latestRetiredCount: Int,
	@ColumnInfo(name = SubjectStatsSegmentTable.LATEST_UNREPORTED_COUNT) val latestUnreportedCount: Int,
	@ColumnInfo(name = SubjectStatsSegmentTable.AVERAGE_GRADE) val averageGrade: Double? = null,
	@ColumnInfo(name = SubjectStatsSegmentTable.MEDIAN_GRADE) val medianGrade: Double? = null,
	@ColumnInfo(name = SubjectStatsSegmentTable.STDDEV_GRADE) val stddevGrade: Double? = null,
	@ColumnInfo(name = SubjectStatsSegmentTable.FIRST_ATTEMPT_PASS_RATE) val firstAttemptPassRate: Double? = null,
	@ColumnInfo(name = SubjectStatsSegmentTable.EVENTUAL_PASS_RATE) val eventualPassRate: Double? = null,
	@ColumnInfo(name = SubjectStatsSegmentTable.RETAKE_RATE) val retakeRate: Double? = null,
	@ColumnInfo(name = SubjectStatsSegmentTable.AVG_ATTEMPTS_TO_PASS) val avgAttemptsToPass: Double? = null,
	@ColumnInfo(name = SubjectStatsSegmentTable.MEDIAN_ATTEMPTS_TO_PASS) val medianAttemptsToPass: Double? = null,
	@ColumnInfo(name = SubjectStatsSegmentTable.FIRST_CLOSED_TERM_START_AT) val firstClosedTermStartAt: Long? = null,
	@ColumnInfo(name = SubjectStatsSegmentTable.LAST_CLOSED_TERM_START_AT) val lastClosedTermStartAt: Long? = null
)
