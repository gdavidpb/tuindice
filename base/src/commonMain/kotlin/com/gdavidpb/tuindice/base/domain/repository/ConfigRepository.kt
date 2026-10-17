package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.AppAvailabilityNotice
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface ConfigRepository {
	suspend fun tryFetch()

	fun getTimeout(): Long
	fun getContactEmail(): String
	fun getContactSubject(): String
	fun getLoadingMessages(): List<String>
	fun getTimeUpdateStalenessDays(): Int
	fun getSyncsToSuggestReview(): Int
	fun getAttestationAndroidEnforcementEnabled(): Boolean
	fun getAttestationIosEnforcementEnabled(): Boolean
	fun getAppAvailabilityNotice(): AppAvailabilityNotice
	fun observeAssistantMode(): Flow<AssistantMode> = emptyFlow()
	fun getAssistantAppealEnabled(): Boolean = false
}
