package com.gdavidpb.tuindice.data.source.functions.mappers

import com.gdavidpb.tuindice.base.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.base.utils.extensions.base64
import com.gdavidpb.tuindice.data.source.functions.responses.EnrollmentProofResponse

fun EnrollmentProofResponse.toEnrollmentProof() = EnrollmentProof(
	name = name,
	inputStream = content.base64().inputStream()
)