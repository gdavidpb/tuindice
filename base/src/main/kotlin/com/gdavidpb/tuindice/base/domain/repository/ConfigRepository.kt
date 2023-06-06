package com.gdavidpb.tuindice.base.domain.repository

import com.gdavidpb.tuindice.base.presentation.navigation.Destination

interface ConfigRepository {
	suspend fun tryFetch()

	fun getTimeout(key: String): Long
	fun getContactEmail(): String
	fun getContactSubject(): String
	fun getLoadingMessages(): List<String>
	fun getTimeUpdateStalenessDays(): Int
	fun getSyncsToSuggestReview(): Int
	fun getConnectionTimeout(): Long
	fun getDestinations(): Map<String, Destination>
}