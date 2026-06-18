package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.domain.model.AppAvailabilityNotice

interface ConfigRepository {
	suspend fun tryFetch()

	fun getTimeout(): Long
	fun getContactEmail(): String
	fun getContactSubject(): String
	fun getLoadingMessages(): List<String>
	fun getTimeUpdateStalenessDays(): Int
	fun getSyncsToSuggestReview(): Int
	fun getAppAvailabilityNotice(): AppAvailabilityNotice
}
