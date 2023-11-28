package com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.source.api.mapper

import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.enrollmentproof.data.repository.enrollmentproof.source.api.response.EnrollmentProofResponse

fun EnrollmentProofResponse.toEnrollmentProof() = EnrollmentProof(
	source = name,
	content = content
)