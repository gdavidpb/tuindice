package com.gdavidpb.tuindice.summary.domain.exception

import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureError

class ProfilePictureIllegalArgumentException(
	val error: ProfilePictureError
) : IllegalArgumentException()