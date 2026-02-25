package com.gdavidpb.tuindice.base.domain.repository

interface ConfigGateway {
	suspend fun tryFetch()

	fun getTimeout(): Long
	fun getContactEmail(): String
	fun getContactSubject(): String
	fun getLoadingMessages(): List<String>
	fun getTimeUpdateStalenessDays(): Int
	fun getSyncsToSuggestReview(): Int
}
