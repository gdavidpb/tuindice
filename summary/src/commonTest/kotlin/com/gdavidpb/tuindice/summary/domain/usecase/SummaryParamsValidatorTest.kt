package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.PlatformUri
import com.gdavidpb.tuindice.summary.domain.exception.ProfilePictureIllegalArgumentException
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.validator.UploadProfilePictureParamsValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SummaryParamsValidatorTest {
	@Test
	fun uploadProfilePictureParamsValidator_acceptsNonEmptyUri() {
		UploadProfilePictureParamsValidator().validate(PlatformUri("content://gallery/picture.jpg"))
	}

	@Test
	fun uploadProfilePictureParamsValidator_rejectsEmptyUri() {
		val exception = assertFailsWith<ProfilePictureIllegalArgumentException> {
			UploadProfilePictureParamsValidator().validate(PlatformUri(""))
		}

		assertEquals(ProfilePictureUseCaseError.InvalidSource, exception.error)
	}
}
