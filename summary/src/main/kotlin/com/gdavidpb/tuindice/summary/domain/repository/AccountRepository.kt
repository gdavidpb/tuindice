package com.gdavidpb.tuindice.summary.domain.repository

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

interface AccountRepository {
	suspend fun getAccountFlow(): Flow<Account>

	suspend fun uploadProfilePicture(inputStream: InputStream): ProfilePicture
	suspend fun removeProfilePicture()
}