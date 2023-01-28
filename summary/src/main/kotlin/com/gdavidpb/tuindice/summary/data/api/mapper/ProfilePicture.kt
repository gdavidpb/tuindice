package com.gdavidpb.tuindice.summary.data.api.mapper

import com.gdavidpb.tuindice.summary.data.api.response.ProfilePictureResponse
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture

fun ProfilePictureResponse.toProfilePicture() = ProfilePicture(
	uid = uid,
	url = url
)