package com.gdavidpb.tuindice.summary.data.source.api.mappers

import com.gdavidpb.tuindice.summary.data.source.api.responses.ProfilePictureResponse
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture

fun ProfilePictureResponse.toProfilePicture() = ProfilePicture(
	uid = uid,
	url = url
)