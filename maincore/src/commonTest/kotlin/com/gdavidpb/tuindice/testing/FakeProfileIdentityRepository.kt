package com.gdavidpb.tuindice.testing

import com.gdavidpb.tuindice.base.domain.model.ProfileIdentity
import com.gdavidpb.tuindice.base.domain.repository.ProfileIdentityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

val ANA_PROFILE_IDENTITY = ProfileIdentity(
	name = "Ana Diaz",
	careerName = "Ingenieria Civil Informatica",
	carnet = "20261234",
	pictureUrl = "",
	pictureVersion = 0
)

// The identity the device holds, as a test sets it: a person by default, or nobody when no user is stored.
class FakeProfileIdentityRepository(
	identity: ProfileIdentity? = ANA_PROFILE_IDENTITY
) : ProfileIdentityRepository {
	val state = MutableStateFlow(identity)

	override fun observeProfileIdentity(): Flow<ProfileIdentity?> = state
}
