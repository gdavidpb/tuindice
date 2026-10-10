package com.gdavidpb.tuindice.enrollmentproof.domain.exception

import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProofNotFoundReason

class EnrollmentProofNotFoundException(
	val reason: EnrollmentProofNotFoundReason = EnrollmentProofNotFoundReason.Unknown,
	cause: Throwable? = null
) : IllegalStateException(cause)
