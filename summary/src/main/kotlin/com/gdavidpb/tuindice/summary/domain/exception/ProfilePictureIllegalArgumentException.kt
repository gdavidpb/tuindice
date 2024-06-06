package com.gdavidpb.tuindice.summary.domain.exception

import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError

class ProfilePictureIllegalArgumentException(
	val error: ProfilePictureUseCaseError
) : IllegalArgumentException()