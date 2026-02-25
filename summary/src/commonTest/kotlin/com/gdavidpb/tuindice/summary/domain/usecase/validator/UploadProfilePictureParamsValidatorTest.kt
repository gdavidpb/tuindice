package com.gdavidpb.tuindice.summary.domain.usecase.validator

import com.gdavidpb.tuindice.base.domain.model.PlatformUri
import com.gdavidpb.tuindice.summary.domain.exception.ProfilePictureIllegalArgumentException
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UploadProfilePictureParamsValidatorTest {
	@Test
	fun validate_whenUriIsNotEmpty_doesNotThrow() {
		UploadProfilePictureParamsValidator().validate(
			PlatformUri("content://profile-picture")
		)
	}

	@Test
	fun validate_whenUriIsEmpty_returnsInvalidSourceError() {
		val error = assertFailsWith<ProfilePictureIllegalArgumentException> {
			UploadProfilePictureParamsValidator().validate(PlatformUri(""))
		}

		assertEquals(ProfilePictureUseCaseError.InvalidSource, error.error)
	}
}
