package com.gdavidpb.tuindice.login.domain.exception

import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError

class SignInIllegalArgumentException(
	val error: SignInError
) : IllegalArgumentException()