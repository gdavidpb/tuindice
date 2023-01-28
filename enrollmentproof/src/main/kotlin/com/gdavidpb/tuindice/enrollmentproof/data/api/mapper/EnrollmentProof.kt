package com.gdavidpb.tuindice.enrollmentproof.data.api.mapper

import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.enrollmentproof.data.api.response.EnrollmentProofResponse

fun EnrollmentProofResponse.toEnrollmentProof() = EnrollmentProof(
	source = name,
	content = content
)