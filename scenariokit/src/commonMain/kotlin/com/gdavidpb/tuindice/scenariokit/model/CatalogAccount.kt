package com.gdavidpb.tuindice.scenariokit.model

import kotlinx.serialization.Serializable

/**
 * Credentials of a fixture account as the catalog carries them. Session values are
 * null for accounts that only sign in through the UI.
 */
@Serializable
data class CatalogAccount(
	val id: String,
	val usbId: String,
	val password: String,
	val sessionId: String?,
	val accessToken: String?,
	val refreshToken: String?,
	val mockScenario: String?
)
