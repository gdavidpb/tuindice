package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicAttemptProjectionTable

@Entity(
	tableName = AcademicAttemptProjectionTable.TABLE_NAME,
	primaryKeys = [AcademicAttemptProjectionTable.VIEW_MODE, AcademicAttemptProjectionTable.ID],
	indices = [
		Index(value = [AcademicAttemptProjectionTable.RECORD_ID, AcademicAttemptProjectionTable.VIEW_MODE]),
		Index(value = [AcademicAttemptProjectionTable.VIEW_MODE, AcademicAttemptProjectionTable.TERM_ID])
	]
)
data class AcademicAttemptProjectionEntity(
	@ColumnInfo(name = AcademicAttemptProjectionTable.VIEW_MODE)
	val viewMode: String,
	@ColumnInfo(name = AcademicAttemptProjectionTable.ID)
	val id: String,
	@ColumnInfo(name = AcademicAttemptProjectionTable.RECORD_ID)
	val recordId: String,
	@ColumnInfo(name = AcademicAttemptProjectionTable.TERM_ID)
	val termId: String,
	@ColumnInfo(name = AcademicAttemptProjectionTable.SUBJECT_CODE)
	val subjectCode: String,
	@ColumnInfo(name = AcademicAttemptProjectionTable.SUBJECT_NAME)
	val subjectName: String,
	@ColumnInfo(name = AcademicAttemptProjectionTable.CREDITS)
	val credits: Int,
	@ColumnInfo(name = AcademicAttemptProjectionTable.SEQUENCE_IN_TERM)
	val sequenceInTerm: Int,
	@ColumnInfo(name = AcademicAttemptProjectionTable.GRADING_MODE)
	val gradingMode: String,
	@ColumnInfo(name = AcademicAttemptProjectionTable.RAW_GRADE_TOKEN)
	val rawGradeToken: String,
	@ColumnInfo(name = AcademicAttemptProjectionTable.RAW_OBSERVATION_TEXT)
	val rawObservationText: String,
	@ColumnInfo(name = AcademicAttemptProjectionTable.SCORE_KIND)
	val scoreKind: String,
	@ColumnInfo(name = AcademicAttemptProjectionTable.SCORE_NUMERIC_VALUE)
	val scoreNumericValue: Int? = null,
	@ColumnInfo(name = AcademicAttemptProjectionTable.SCORE_SYMBOLIC_VALUE)
	val scoreSymbolicValue: String? = null,
	@ColumnInfo(name = AcademicAttemptProjectionTable.OUTCOME)
	val outcome: String,
	@ColumnInfo(name = AcademicAttemptProjectionTable.BADGE)
	val badge: String,
	@ColumnInfo(name = AcademicAttemptProjectionTable.EDITABLE)
	val editable: Boolean,
	@ColumnInfo(name = AcademicAttemptProjectionTable.SYNTHETIC)
	val synthetic: Boolean,
	@ColumnInfo(name = AcademicAttemptProjectionTable.COUNTS_TOWARD_TERM_AVERAGE)
	val countsTowardTermAverage: Boolean,
	@ColumnInfo(name = AcademicAttemptProjectionTable.COUNTS_TOWARD_CUMULATIVE_AVERAGE)
	val countsTowardCumulativeAverage: Boolean
)
