package com.gdavidpb.tuindice.summary.domain.usecase.validator

import com.gdavidpb.tuindice.base.domain.usecase.base.ParamsValidator
import com.gdavidpb.tuindice.summary.domain.exception.ProfilePictureIllegalArgumentException
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.isDirectory
import io.github.vinceglb.filekit.name

class UploadProfilePictureParamsValidator : ParamsValidator<PlatformFile> {
	override fun validate(params: PlatformFile) {
		require(params.name.isNotBlank() && !params.isDirectory()) {
			throw ProfilePictureIllegalArgumentException(ProfilePictureUseCaseError.InvalidSource)
		}
	}
}
