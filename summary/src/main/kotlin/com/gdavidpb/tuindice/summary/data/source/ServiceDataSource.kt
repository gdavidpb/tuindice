package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.summary.domain.model.Profile
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture

interface ServiceDataSource {
	suspend fun getProfile(): Profile

	@Deprecated("This will be removed.")
	suspend fun getProfilePicture(uid: String): ProfilePicture
	suspend fun uploadProfilePicture(encodedPicture: String): ProfilePicture
	suspend fun removeProfilePicture()
}