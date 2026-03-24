package com.gdavidpb.tuindice.record.domain.policy

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.record.data.repository.mutation.RecordMutation
import com.gdavidpb.tuindice.record.data.repository.quarter.model.LocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter

enum class QuarterMutationType {
	AddQuarter,
	SetSubjectGrade,
	RemoveQuarter
}

object QuarterMutationPolicy {
	fun canEditGrades(quarter: Quarter): Boolean = canEditGrades(quarter.isReadOnly)

	fun canEditGrades(quarter: LocalQuarter): Boolean = canEditGrades(quarter.isReadOnly)

	fun canEditGrades(quarter: RemoteQuarter): Boolean = canEditGrades(quarter.isReadOnly)

	fun canDelete(quarter: Quarter): Boolean = canDelete(
		isCurrent = quarter.isCurrent,
		isReadOnly = quarter.isReadOnly
	)

	fun canDelete(quarter: LocalQuarter): Boolean = canDelete(
		isCurrent = quarter.isCurrent,
		isReadOnly = quarter.isReadOnly
	)

	fun canDelete(quarter: RemoteQuarter): Boolean = canDelete(
		isCurrent = quarter.isCurrent,
		isReadOnly = quarter.isReadOnly
	)

	fun canApplyPendingMutation(
		quarter: LocalQuarter,
		mutationType: QuarterMutationType
	): Boolean {
		return when (mutationType) {
			QuarterMutationType.AddQuarter -> canEditGrades(quarter)
			QuarterMutationType.SetSubjectGrade -> canEditGrades(quarter)
			QuarterMutationType.RemoveQuarter -> canDelete(quarter)
		}
	}

	fun isInstitutionalCurrentQuarter(quarter: Quarter): Boolean = quarter.isCurrent

	fun isInstitutionalCurrentQuarter(quarter: LocalQuarter): Boolean = quarter.isCurrent

	fun isInstitutionalCurrentQuarter(quarter: RemoteQuarter): Boolean = quarter.isCurrent

	private fun canEditGrades(isReadOnly: Boolean): Boolean = !isReadOnly

	private fun canDelete(
		isCurrent: Boolean,
		isReadOnly: Boolean
	): Boolean {
		return !isReadOnly && !isCurrent
	}
}

fun RecordMutation.toQuarterMutationType(): QuarterMutationType {
	return when (this) {
		is RecordMutation.AddQuarter -> QuarterMutationType.AddQuarter
		is RecordMutation.SetSubjectGrade -> QuarterMutationType.SetSubjectGrade
		is RecordMutation.RemoveQuarter -> QuarterMutationType.RemoveQuarter
	}
}
