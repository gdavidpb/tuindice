package com.gdavidpb.tuindice.about.domain.usecase

import com.gdavidpb.tuindice.about.domain.repository.ExternalActionsRepository
import com.gdavidpb.tuindice.about.domain.usecase.param.ShareTextParams
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ShareTextUseCase(
	private val externalActionsRepository: ExternalActionsRepository
) : FlowUseCase<ShareTextParams, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: ShareTextParams): Flow<Unit> {
		externalActionsRepository.shareText(
			subject = params.subject,
			text = params.text
		)

		return flowOf(Unit)
	}
}
