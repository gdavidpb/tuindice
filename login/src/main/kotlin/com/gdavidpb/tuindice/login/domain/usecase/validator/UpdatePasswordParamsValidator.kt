package com.gdavidpb.tuindice.login.domain.usecase.validator

import com.gdavidpb.tuindice.base.domain.usecase.base.ParamsValidator
import com.gdavidpb.tuindice.login.domain.exception.SignInIllegalArgumentException
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError

class UpdatePasswordParamsValidator : ParamsValidator<String> {
	override fun validate(params: String) {
		require(params.isNotEmpty()) {
			throw SignInIllegalArgumentException(SignInError.EmptyPassword)
		}
	}
}