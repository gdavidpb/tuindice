package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.base.domain.model.ProfileIdentity
import com.gdavidpb.tuindice.base.domain.repository.ProfileIdentityRepository
import com.gdavidpb.tuindice.summary.data.mapper.toProfileIdentity
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

// Who the student is, read from the same stored user the summary observes: the shell and the Settings screen show the
// picture and the name the app already has, without a request of their own.
class ProfileIdentityDataSource(
	private val userRepository: UserRepository
) : ProfileIdentityRepository {
	override fun observeProfileIdentity(): Flow<ProfileIdentity> = flow {
		emitAll(userRepository.observeUserFlow().map { user -> user.toProfileIdentity() })
	}
}
