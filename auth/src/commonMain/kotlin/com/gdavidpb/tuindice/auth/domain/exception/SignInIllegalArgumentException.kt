package com.gdavidpb.tuindice.auth.domain.exception

import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError

class SignInIllegalArgumentException(
	val error: SignInUseCaseError
) : IllegalArgumentException()