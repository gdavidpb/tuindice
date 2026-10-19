package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import kotlinx.coroutines.flow.Flow

interface AssistantConfigRepository {
	fun observeAssistantMode(): Flow<AssistantMode>
}
