package com.gdavidpb.tuindice.summary.data.mapper

import com.gdavidpb.tuindice.summary.data.model.ProfilePictureResponse
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture

fun ProfilePictureResponse.toProfilePicture() = ProfilePicture(
	url = url
)
