package com.gdavidpb.tuindice.summary.data.repository.account

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture

interface RemoteDataSource {
	suspend fun getAccount(): Account

	suspend fun uploadProfilePicture(encodedPicture: String): ProfilePicture
	suspend fun removeProfilePicture()
}