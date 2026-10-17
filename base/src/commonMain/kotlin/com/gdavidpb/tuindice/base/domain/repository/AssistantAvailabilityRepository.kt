package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantAvailability
import kotlinx.coroutines.flow.Flow

interface AssistantAvailabilityRepository {
	fun observeAvailability(): Flow<AssistantAvailability>

	fun currentAvailability(): AssistantAvailability
}
