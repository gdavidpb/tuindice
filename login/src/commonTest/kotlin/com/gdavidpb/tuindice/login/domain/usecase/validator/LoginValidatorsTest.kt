package com.gdavidpb.tuindice.login.domain.usecase.validator

import com.gdavidpb.tuindice.login.domain.exception.SignInIllegalArgumentException
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.login.domain.usecase.param.SignInParams
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LoginValidatorsTest {
	@Test
	fun signInParamsValidator_whenValidParams_doesNotThrow() {
		SignInParamsValidator().validate(
			SignInParams(
				usbId = "20-32000",
				password = "secret"
			)
		)
	}

	@Test
	fun signInParamsValidator_whenUsbIdIsEmpty_returnsEmptyUsbIdError() {
		val error = assertFailsWith<SignInIllegalArgumentException> {
			SignInParamsValidator().validate(
				SignInParams(
					usbId = "",
					password = "secret"
				)
			)
		}

		assertEquals(SignInUseCaseError.EmptyUsbId, error.error)
	}

	@Test
	fun signInParamsValidator_whenUsbIdFormatIsInvalid_returnsInvalidUsbIdError() {
		val error = assertFailsWith<SignInIllegalArgumentException> {
			SignInParamsValidator().validate(
				SignInParams(
					usbId = "2032000",
					password = "secret"
				)
			)
		}

		assertEquals(SignInUseCaseError.InvalidUsbId, error.error)
	}

	@Test
	fun signInParamsValidator_whenPasswordIsEmpty_returnsEmptyPasswordError() {
		val error = assertFailsWith<SignInIllegalArgumentException> {
			SignInParamsValidator().validate(
				SignInParams(
					usbId = "20-32000",
					password = ""
				)
			)
		}

		assertEquals(SignInUseCaseError.EmptyPassword, error.error)
	}

	@Test
	fun updatePasswordParamsValidator_whenPasswordIsNotEmpty_doesNotThrow() {
		UpdatePasswordParamsValidator().validate("new-password")
	}

	@Test
	fun updatePasswordParamsValidator_whenPasswordIsEmpty_returnsEmptyPasswordError() {
		val error = assertFailsWith<SignInIllegalArgumentException> {
			UpdatePasswordParamsValidator().validate("")
		}

		assertEquals(SignInUseCaseError.EmptyPassword, error.error)
	}
}
