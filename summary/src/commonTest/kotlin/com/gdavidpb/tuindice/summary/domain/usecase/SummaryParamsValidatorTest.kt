package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.summary.domain.exception.ProfilePictureIllegalArgumentException
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.validator.UploadProfilePictureParamsValidator
import io.github.vinceglb.filekit.PlatformFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SummaryParamsValidatorTest {
	@Test
	fun uploadProfilePictureParamsValidator_acceptsValidFile() {
		UploadProfilePictureParamsValidator().validate(PlatformFile("/tmp/picture.jpg"))
	}

	@Test
	fun uploadProfilePictureParamsValidator_rejectsInvalidFileSource() {
		val exception = assertFailsWith<ProfilePictureIllegalArgumentException> {
			UploadProfilePictureParamsValidator().validate(PlatformFile("/tmp"))
		}

		assertEquals(ProfilePictureUseCaseError.InvalidSource, exception.error)
	}
}
