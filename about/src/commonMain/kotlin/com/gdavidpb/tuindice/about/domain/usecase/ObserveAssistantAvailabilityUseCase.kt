package com.gdavidpb.tuindice.about.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AssistantAvailabilityRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class ObserveAssistantAvailabilityUseCase(
	private val assistantAvailabilityRepository: AssistantAvailabilityRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Boolean, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Boolean> {
		// A change of mode that leaves the row visible is not a new showing of it: the consent is read again
		// only when the row appears.
		return assistantAvailabilityRepository.observeAvailability()
			.map { availability -> availability.isVisible }
			.distinctUntilChanged()
	}
}
