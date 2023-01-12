package com.gdavidpb.tuindice.enrollmentproof.data.api.mappers

import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.enrollmentproof.data.api.responses.EnrollmentProofResponse

fun EnrollmentProofResponse.toEnrollmentProof() = EnrollmentProof(
	source = name,
	content = content
)