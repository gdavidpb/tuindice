package com.gdavidpb.tuindice.auth.domain.usecase.validator

import com.gdavidpb.tuindice.base.domain.usecase.base.ParamsValidator
import com.gdavidpb.tuindice.auth.domain.model.SignInIdentifierMode
import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.auth.domain.exception.SignInIllegalArgumentException
import com.gdavidpb.tuindice.auth.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.auth.utils.extension.isUsbEmail
import com.gdavidpb.tuindice.auth.utils.extension.isUsbId

class SignInParamsValidator : ParamsValidator<SignInParams> {
	override fun validate(params: SignInParams) {
		require(params.usbId.isNotEmpty()) {
			throw SignInIllegalArgumentException(SignInUseCaseError.EmptyUsbId)
		}

		val isValidIdentifier = when (params.identifierMode) {
			SignInIdentifierMode.UsbId -> params.usbId.isUsbId()
			SignInIdentifierMode.UsbEmail -> params.usbId.isUsbEmail()
		}

		require(isValidIdentifier) {
			throw SignInIllegalArgumentException(SignInUseCaseError.InvalidUsbId)
		}

		require(params.password.isNotEmpty()) {
			throw SignInIllegalArgumentException(SignInUseCaseError.EmptyPassword)
		}
	}
}
