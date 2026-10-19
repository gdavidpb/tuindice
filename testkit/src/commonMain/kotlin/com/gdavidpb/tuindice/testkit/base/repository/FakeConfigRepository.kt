package com.gdavidpb.tuindice.testkit.base.repository

import com.gdavidpb.tuindice.base.domain.model.AppAvailabilityNotice
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository

class FakeConfigRepository(
	private val email: String = "support@tuindice.app",
	private val subject: String = "Support TuIndice",
	private val appAvailabilityNotice: AppAvailabilityNotice = AppAvailabilityNotice(
		enabled = false,
		title = "",
		message = ""
	)
) : ConfigRepository {
	override suspend fun tryFetch() = Unit

	override fun getTimeout(): Long = 30_000L

	override fun getContactEmail(): String = email

	override fun getContactSubject(): String = subject

	override fun getLoadingMessages(): List<String> = listOf("Cargando")

	override fun getTimeUpdateStalenessDays(): Int = 7

	override fun getSyncsToSuggestReview(): Int = 3

	override fun getAttestationAndroidEnforcementEnabled(): Boolean = false

	override fun getAttestationIosEnforcementEnabled(): Boolean = true

	override fun getAppAvailabilityNotice(): AppAvailabilityNotice = appAvailabilityNotice
}
