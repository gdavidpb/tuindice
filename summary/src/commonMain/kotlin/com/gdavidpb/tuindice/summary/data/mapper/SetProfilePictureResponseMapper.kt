package com.gdavidpb.tuindice.summary.data.mapper

import com.gdavidpb.tuindice.summary.data.model.SetProfilePictureResponse
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture

fun SetProfilePictureResponse.toProfilePicture() = ProfilePicture(
	url = url
)
