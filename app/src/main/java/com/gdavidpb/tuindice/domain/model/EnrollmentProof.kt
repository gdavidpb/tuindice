package com.gdavidpb.tuindice.domain.model

import java.io.InputStream

data class EnrollmentProof(
	val name: String,
	val inputStream: InputStream
)