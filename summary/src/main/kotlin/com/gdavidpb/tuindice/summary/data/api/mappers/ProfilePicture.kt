package com.gdavidpb.tuindice.summary.data.api.mappers

import com.gdavidpb.tuindice.summary.data.api.responses.ProfilePictureResponse
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture

fun ProfilePictureResponse.toProfilePicture() = ProfilePicture(
	uid = uid,
	url = url
)