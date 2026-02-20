package com.gdavidpb.tuindice.summary.data.repository.user.source.api.mapper

import com.gdavidpb.tuindice.summary.data.repository.user.source.api.response.ProfilePictureResponse
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture

fun ProfilePictureResponse.toProfilePicture() = ProfilePicture(
	url = url
)