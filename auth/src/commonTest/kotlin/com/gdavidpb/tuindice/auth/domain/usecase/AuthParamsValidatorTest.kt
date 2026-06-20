package com.gdavidpb.tuindice.auth.domain.usecase

import com.gdavidpb.tuindice.auth.domain.exception.SignInIllegalArgumentException
import com.gdavidpb.tuindice.auth.domain.model.SignInIdentifierMode
import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.auth.domain.usecase.param.SignInParams
import com.gdavidpb.tuindice.auth.domain.usecase.validator.SignInParamsValidator
import com.gdavidpb.tuindice.auth.domain.usecase.validator.UpdatePasswordParamsValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AuthParamsValidatorTest {
	@Test
	fun signInParamsValidator_acceptsValidParams() {
		SignInParamsValidator().validate(
			SignInParams(
				usbId = "20-26123",
				password = "secret123"
			)
		)
	}

	@Test
	fun signInParamsValidator_acceptsUsbEmailParams() {
		SignInParamsValidator().validate(
			SignInParams(
				usbId = "mail@usb.ve",
				password = "secret123",
				identifierMode = SignInIdentifierMode.UsbEmail
			)
		)

		SignInParamsValidator().validate(
			SignInParams(
				usbId = "mail",
				password = "secret123",
				identifierMode = SignInIdentifierMode.UsbEmail
			)
		)
	}

	@Test
	fun signInParamsValidator_rejectsEmptyUsbId() {
		val exception = assertFailsWith<SignInIllegalArgumentException> {
			SignInParamsValidator().validate(SignInParams(usbId = "", password = "secret123"))
		}

		assertEquals(SignInUseCaseError.EmptyUsbId, exception.error)
	}

	@Test
	fun signInParamsValidator_rejectsInvalidUsbId() {
		val exception = assertFailsWith<SignInIllegalArgumentException> {
			SignInParamsValidator().validate(SignInParams(usbId = "usb-id", password = "secret123"))
		}

		assertEquals(SignInUseCaseError.InvalidUsbId, exception.error)
	}

	@Test
	fun signInParamsValidator_rejectsNonUsbEmailDomain() {
		val exception = assertFailsWith<SignInIllegalArgumentException> {
			SignInParamsValidator().validate(
				SignInParams(
					usbId = "mail@example.com",
					password = "secret123",
					identifierMode = SignInIdentifierMode.UsbEmail
				)
			)
		}

		assertEquals(SignInUseCaseError.InvalidUsbId, exception.error)
	}

	@Test
	fun signInParamsValidator_rejectsEmptyPassword() {
		val exception = assertFailsWith<SignInIllegalArgumentException> {
			SignInParamsValidator().validate(SignInParams(usbId = "20-26123", password = ""))
		}

		assertEquals(SignInUseCaseError.EmptyPassword, exception.error)
	}

	@Test
	fun updatePasswordParamsValidator_rejectsEmptyPassword() {
		val exception = assertFailsWith<SignInIllegalArgumentException> {
			UpdatePasswordParamsValidator().validate("")
		}

		assertEquals(SignInUseCaseError.EmptyPassword, exception.error)
	}
}
