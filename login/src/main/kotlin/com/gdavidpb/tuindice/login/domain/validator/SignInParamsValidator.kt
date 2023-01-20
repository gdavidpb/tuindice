package com.gdavidpb.tuindice.login.domain.validator

import com.gdavidpb.tuindice.base.domain.validator.Validator
import com.gdavidpb.tuindice.base.utils.extensions.isUsbId
import com.gdavidpb.tuindice.login.domain.error.SignInError
import com.gdavidpb.tuindice.login.domain.exception.SignInIllegalArgumentException
import com.gdavidpb.tuindice.login.domain.param.SignInParams

class SignInParamsValidator : Validator<SignInParams> {
	override fun validate(params: SignInParams) {
		require(params.usbId.isNotEmpty()) {
			throw SignInIllegalArgumentException(SignInError.EmptyUsbId)
		}

		require(params.usbId.isUsbId()) {
			throw SignInIllegalArgumentException(SignInError.InvalidUsbId)
		}

		require(params.password.isNotEmpty()) {
			throw SignInIllegalArgumentException(SignInError.EmptyPassword)
		}
	}
}