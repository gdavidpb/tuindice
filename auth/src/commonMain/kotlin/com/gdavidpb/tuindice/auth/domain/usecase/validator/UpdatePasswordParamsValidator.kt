package com.gdavidpb.tuindice.auth.domain.usecase.validator

import com.gdavidpb.tuindice.base.domain.usecase.base.ParamsValidator
import com.gdavidpb.tuindice.auth.domain.exception.SignInIllegalArgumentException
import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError

class UpdatePasswordParamsValidator : ParamsValidator<String> {
	override fun validate(params: String) {
		require(params.isNotEmpty()) {
			throw SignInIllegalArgumentException(SignInUseCaseError.EmptyPassword)
		}
	}
}