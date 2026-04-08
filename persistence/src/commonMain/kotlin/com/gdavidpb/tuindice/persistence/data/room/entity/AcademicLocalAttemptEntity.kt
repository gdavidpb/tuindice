package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicLocalAttemptTable

@Entity(
	tableName = AcademicLocalAttemptTable.TABLE_NAME,
	indices = [
		Index(value = [AcademicLocalAttemptTable.RECORD_ID, AcademicLocalAttemptTable.TERM_ID]),
		Index(value = [AcademicLocalAttemptTable.RECORD_ID, AcademicLocalAttemptTable.SUBJECT_CODE])
	]
)
data class AcademicLocalAttemptEntity(
	@PrimaryKey
	@ColumnInfo(name = AcademicLocalAttemptTable.ID)
	val id: String,
	@ColumnInfo(name = AcademicLocalAttemptTable.RECORD_ID)
	val recordId: String,
	@ColumnInfo(name = AcademicLocalAttemptTable.TERM_ID)
	val termId: String,
	@ColumnInfo(name = AcademicLocalAttemptTable.SUBJECT_CODE)
	val subjectCode: String,
	@ColumnInfo(name = AcademicLocalAttemptTable.SUBJECT_NAME)
	val subjectName: String,
	@ColumnInfo(name = AcademicLocalAttemptTable.CREDITS)
	val credits: Int,
	@ColumnInfo(name = AcademicLocalAttemptTable.SEQUENCE_IN_TERM)
	val sequenceInTerm: Int,
	@ColumnInfo(name = AcademicLocalAttemptTable.GRADING_MODE)
	val gradingMode: String,
	@ColumnInfo(name = AcademicLocalAttemptTable.RAW_GRADE_TOKEN)
	val rawGradeToken: String,
	@ColumnInfo(name = AcademicLocalAttemptTable.RAW_OBSERVATION_TEXT)
	val rawObservationText: String,
	@ColumnInfo(name = AcademicLocalAttemptTable.SCORE_KIND)
	val scoreKind: String,
	@ColumnInfo(name = AcademicLocalAttemptTable.SCORE_NUMERIC_VALUE)
	val scoreNumericValue: Int? = null,
	@ColumnInfo(name = AcademicLocalAttemptTable.SCORE_SYMBOLIC_VALUE)
	val scoreSymbolicValue: String? = null,
	@ColumnInfo(name = AcademicLocalAttemptTable.OFFICIAL_OUTCOME)
	val officialOutcome: String,
	@ColumnInfo(name = AcademicLocalAttemptTable.OFFICIAL_BADGE)
	val officialBadge: String,
	@ColumnInfo(name = AcademicLocalAttemptTable.SOURCE)
	val source: String
)
