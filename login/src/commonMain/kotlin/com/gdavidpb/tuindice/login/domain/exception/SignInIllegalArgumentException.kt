package com.gdavidpb.tuindice.login.domain.exception

import com.gdavidpb.tuindice.login.domain.usecase.error.SignInUseCaseError

class SignInIllegalArgumentException(
	val error: SignInUseCaseError
) : IllegalArgumentException()