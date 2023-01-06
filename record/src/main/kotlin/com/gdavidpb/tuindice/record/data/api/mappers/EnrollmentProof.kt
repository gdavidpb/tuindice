package com.gdavidpb.tuindice.record.data.api.mappers

import com.gdavidpb.tuindice.record.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.record.data.api.responses.EnrollmentProofResponse

fun EnrollmentProofResponse.toEnrollmentProof() = EnrollmentProof(
	source = name,
	content = content
)