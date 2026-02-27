package com.gdavidpb.tuindice.about.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.ExternalActionsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SendSupportEmailUseCase(
	private val externalActionsRepository: ExternalActionsRepository,
	private val configRepository: ConfigRepository
) : FlowUseCase<Unit, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		externalActionsRepository.sendEmail(
			email = configRepository.getContactEmail(),
			subject = configRepository.getContactSubject()
		)

		return flowOf(Unit)
	}
}
