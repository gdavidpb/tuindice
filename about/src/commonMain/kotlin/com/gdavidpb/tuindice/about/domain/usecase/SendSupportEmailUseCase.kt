package com.gdavidpb.tuindice.about.domain.usecase

import com.gdavidpb.tuindice.about.domain.repository.ExternalActionsRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SendSupportEmailUseCase(
	private val configRepository: ConfigRepository,
	private val externalActionsRepository: ExternalActionsRepository
) : FlowUseCase<Unit, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		externalActionsRepository.sendEmail(
			email = configRepository.getContactEmail(),
			subject = configRepository.getContactSubject(),
			text = ""
		)

		return flowOf(Unit)
	}
}
