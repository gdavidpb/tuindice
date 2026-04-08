package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.AcademicSummaryTable

@Entity(tableName = AcademicSummaryTable.TABLE_NAME)
data class AcademicSummaryEntity(
	@PrimaryKey
	@ColumnInfo(name = AcademicSummaryTable.RECORD_ID)
	val recordId: String,
	@ColumnInfo(name = AcademicSummaryTable.OFFICIAL_GRADE)
	val officialGrade: Double,
	@ColumnInfo(name = AcademicSummaryTable.OFFICIAL_ENROLLED_SUBJECTS)
	val officialEnrolledSubjects: Int,
	@ColumnInfo(name = AcademicSummaryTable.OFFICIAL_ENROLLED_CREDITS)
	val officialEnrolledCredits: Int,
	@ColumnInfo(name = AcademicSummaryTable.OFFICIAL_APPROVED_SUBJECTS)
	val officialApprovedSubjects: Int,
	@ColumnInfo(name = AcademicSummaryTable.OFFICIAL_APPROVED_CREDITS)
	val officialApprovedCredits: Int,
	@ColumnInfo(name = AcademicSummaryTable.OFFICIAL_APPROVED_RELATION)
	val officialApprovedRelation: Double,
	@ColumnInfo(name = AcademicSummaryTable.OFFICIAL_RETIRED_SUBJECTS)
	val officialRetiredSubjects: Int,
	@ColumnInfo(name = AcademicSummaryTable.OFFICIAL_RETIRED_CREDITS)
	val officialRetiredCredits: Int,
	@ColumnInfo(name = AcademicSummaryTable.OFFICIAL_RETIRED_RELATION)
	val officialRetiredRelation: Double,
	@ColumnInfo(name = AcademicSummaryTable.OFFICIAL_FAILED_SUBJECTS)
	val officialFailedSubjects: Int,
	@ColumnInfo(name = AcademicSummaryTable.OFFICIAL_FAILED_CREDITS)
	val officialFailedCredits: Int,
	@ColumnInfo(name = AcademicSummaryTable.OFFICIAL_FAILED_RELATION)
	val officialFailedRelation: Double,
	@ColumnInfo(name = AcademicSummaryTable.OFFICIAL_WITHOUT_EFFECT_ATTEMPTS)
	val officialWithoutEffectAttempts: Int,
	@ColumnInfo(name = AcademicSummaryTable.SIMULATION_GRADE)
	val simulationGrade: Double,
	@ColumnInfo(name = AcademicSummaryTable.SIMULATION_ENROLLED_SUBJECTS)
	val simulationEnrolledSubjects: Int,
	@ColumnInfo(name = AcademicSummaryTable.SIMULATION_ENROLLED_CREDITS)
	val simulationEnrolledCredits: Int,
	@ColumnInfo(name = AcademicSummaryTable.SIMULATION_APPROVED_SUBJECTS)
	val simulationApprovedSubjects: Int,
	@ColumnInfo(name = AcademicSummaryTable.SIMULATION_APPROVED_CREDITS)
	val simulationApprovedCredits: Int,
	@ColumnInfo(name = AcademicSummaryTable.SIMULATION_APPROVED_RELATION)
	val simulationApprovedRelation: Double,
	@ColumnInfo(name = AcademicSummaryTable.SIMULATION_RETIRED_SUBJECTS)
	val simulationRetiredSubjects: Int,
	@ColumnInfo(name = AcademicSummaryTable.SIMULATION_RETIRED_CREDITS)
	val simulationRetiredCredits: Int,
	@ColumnInfo(name = AcademicSummaryTable.SIMULATION_RETIRED_RELATION)
	val simulationRetiredRelation: Double,
	@ColumnInfo(name = AcademicSummaryTable.SIMULATION_FAILED_SUBJECTS)
	val simulationFailedSubjects: Int,
	@ColumnInfo(name = AcademicSummaryTable.SIMULATION_FAILED_CREDITS)
	val simulationFailedCredits: Int,
	@ColumnInfo(name = AcademicSummaryTable.SIMULATION_FAILED_RELATION)
	val simulationFailedRelation: Double,
	@ColumnInfo(name = AcademicSummaryTable.SIMULATION_WITHOUT_EFFECT_ATTEMPTS)
	val simulationWithoutEffectAttempts: Int
)
