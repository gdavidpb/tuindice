package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantTermInsight
import kotlinx.coroutines.flow.Flow

interface AssistantInsightsRepository {
	fun observeInsightsFlow(): Flow<List<AssistantTermInsight>>

	suspend fun refreshInsights()
}
