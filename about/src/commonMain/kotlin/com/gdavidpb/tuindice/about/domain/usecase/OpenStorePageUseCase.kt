package com.gdavidpb.tuindice.about.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ExternalActionsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class OpenStorePageUseCase(
	private val externalActionsRepository: ExternalActionsRepository
) : FlowUseCase<Unit, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		externalActionsRepository.openStorePage()

		return flowOf(Unit)
	}
}
