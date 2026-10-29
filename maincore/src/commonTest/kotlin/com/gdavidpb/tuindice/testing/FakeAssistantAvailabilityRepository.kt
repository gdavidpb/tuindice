package com.gdavidpb.tuindice.testing

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantAvailability
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import com.gdavidpb.tuindice.base.domain.repository.AssistantAvailabilityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// Off unless a test says otherwise: the answer the data source gives before its configuration arrives.
class FakeAssistantAvailabilityRepository(
	availability: AssistantAvailability = AssistantAvailability(mode = AssistantMode.OFF, serverDisabled = false)
) : AssistantAvailabilityRepository {
	private val state = MutableStateFlow(availability)

	override fun observeAvailability(): Flow<AssistantAvailability> = state

	override fun currentAvailability(): AssistantAvailability = state.value
}
