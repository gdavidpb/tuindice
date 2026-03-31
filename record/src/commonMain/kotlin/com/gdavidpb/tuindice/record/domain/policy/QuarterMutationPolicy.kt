package com.gdavidpb.tuindice.record.domain.policy

import com.gdavidpb.tuindice.record.data.mutation.RecordMutation

enum class QuarterMutationType {
	AddQuarter,
	SetSubjectGrade,
	RemoveQuarter
}

object QuarterMutationPolicy {
	fun isCurrent(isCurrent: Boolean): Boolean = isCurrent

	fun canEditGrades(isReadOnly: Boolean): Boolean = !isReadOnly

	fun canDelete(
		isCurrent: Boolean,
		isReadOnly: Boolean
	): Boolean {
		return canEditGrades(isReadOnly) && !isCurrent
	}

	fun canApplyPendingMutation(
		isCurrent: Boolean,
		isReadOnly: Boolean,
		mutationType: QuarterMutationType
	): Boolean {
		return when (mutationType) {
			QuarterMutationType.AddQuarter -> canEditGrades(isReadOnly)
			QuarterMutationType.SetSubjectGrade -> canEditGrades(isReadOnly)
			QuarterMutationType.RemoveQuarter -> canDelete(
				isCurrent = isCurrent,
				isReadOnly = isReadOnly
			)
		}
	}
}

fun RecordMutation.toQuarterMutationType(): QuarterMutationType {
	return when (this) {
		is RecordMutation.AddQuarter -> QuarterMutationType.AddQuarter
		is RecordMutation.SetSubjectGrade -> QuarterMutationType.SetSubjectGrade
		is RecordMutation.RemoveQuarter -> QuarterMutationType.RemoveQuarter
	}
}
