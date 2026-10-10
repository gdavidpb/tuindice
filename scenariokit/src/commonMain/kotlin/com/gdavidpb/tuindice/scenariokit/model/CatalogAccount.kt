package com.gdavidpb.tuindice.scenariokit.model

import kotlinx.serialization.Serializable

/**
 * Credentials of a fixture account as the catalog carries them. Session values are
 * null for accounts that only sign in through the UI. [backendIdentifier] is the identifier the backend receives
 * in `Authorization: Basic` for this account (the USB id without the `@usb.ve` suffix the app drops), exported
 * so a reader of the catalog does not have to reimplement that rule; null in a catalog that predates it.
 */
@Serializable
data class CatalogAccount(
	val id: String,
	val usbId: String,
	val password: String,
	val sessionId: String?,
	val accessToken: String?,
	val refreshToken: String?,
	val mockScenario: String?,
	val backendIdentifier: String? = null
)
