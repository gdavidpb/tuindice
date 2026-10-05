package com.gdavidpb.tuindice.base.domain.model

// Why DST annulled the enrollment, from the codes seen so far. Any other code, or none, is [Other]
// and reads with the generic copy.
enum class EnrollmentAnnulmentCause {
	CreditLimit,
	AcademicIndex,
	WithdrawalRule,
	Probation,
	PermanenceRule,
	Other;

	companion object {
		fun fromCode(code: String?): EnrollmentAnnulmentCause {
			return when (code?.trim()) {
				"01" -> CreditLimit
				"06" -> AcademicIndex
				"10" -> WithdrawalRule
				"12" -> Probation
				"15" -> PermanenceRule
				else -> Other
			}
		}
	}
}
