package com.gdavidpb.tuindice.about.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.ProfileIdentity
import com.gdavidpb.tuindice.base.domain.repository.ProfileIdentityRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow

class ObserveProfileIdentityUseCase(
	private val profileIdentityRepository: ProfileIdentityRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, ProfileIdentity?, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<ProfileIdentity?> {
		return profileIdentityRepository.observeProfileIdentity()
	}
}
