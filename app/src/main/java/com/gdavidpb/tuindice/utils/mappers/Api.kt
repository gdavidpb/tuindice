package com.gdavidpb.tuindice.utils.mappers

import com.gdavidpb.tuindice.data.source.functions.responses.EnrollmentProofResponse
import com.gdavidpb.tuindice.data.source.functions.responses.SignInResponse
import com.gdavidpb.tuindice.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.domain.model.SignIn
import com.gdavidpb.tuindice.utils.extensions.base64

fun SignInResponse.toSignIn() = SignIn(
	token = token
)

fun EnrollmentProofResponse.toEnrollmentProof() = EnrollmentProof(
	name = name,
	inputStream = content.base64().inputStream()
)