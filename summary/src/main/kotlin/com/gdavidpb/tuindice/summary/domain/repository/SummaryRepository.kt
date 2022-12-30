package com.gdavidpb.tuindice.summary.domain.repository

import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture

interface SummaryRepository {
	suspend fun getProfilePicture(uid: String): ProfilePicture
	suspend fun uploadProfilePicture(path: String): ProfilePicture
	suspend fun removeProfilePicture()
}