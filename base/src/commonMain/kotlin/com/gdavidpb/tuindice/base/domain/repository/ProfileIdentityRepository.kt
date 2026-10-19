package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.ProfileIdentity
import kotlinx.coroutines.flow.Flow

interface ProfileIdentityRepository {
	// The identity kept on the device, so it makes no request: it speaks again when the stored user or its picture change.
	fun observeProfileIdentity(): Flow<ProfileIdentity>
}
