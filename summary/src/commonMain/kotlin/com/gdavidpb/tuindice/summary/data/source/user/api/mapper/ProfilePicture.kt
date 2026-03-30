package com.gdavidpb.tuindice.summary.data.source.user.api.mapper

import com.gdavidpb.tuindice.summary.data.source.user.api.response.ProfilePictureResponse
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture

fun ProfilePictureResponse.toProfilePicture() = ProfilePicture(
	url = url
)