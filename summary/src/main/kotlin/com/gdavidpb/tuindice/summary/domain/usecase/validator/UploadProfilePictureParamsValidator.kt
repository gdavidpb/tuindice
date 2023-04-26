package com.gdavidpb.tuindice.summary.domain.usecase.validator

import com.gdavidpb.tuindice.base.domain.usecase.base.ParamsValidator
import com.gdavidpb.tuindice.summary.domain.exception.ProfilePictureIllegalArgumentException
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureError

class UploadProfilePictureParamsValidator : ParamsValidator<String> {
	override fun validate(params: String) {
		require(params.isNotEmpty()) {
			throw ProfilePictureIllegalArgumentException(ProfilePictureError.InvalidSource)
		}
	}
}