package com.gdavidpb.tuindice.debug

import com.gdavidpb.tuindice.base.domain.model.MainSection

/**
 * Session values a debug launch asks the app to start authenticated with.
 *
 * The app holds no account table: the driver sends the values.
 */
data class DebugSessionSeed(
	val sessionId: String,
	val accessToken: String,
	val refreshToken: String,
	val usbId: String,
	val password: String,
	val coachmarksSeen: Boolean,
	val mainSection: MainSection
) {
	companion object {
		/** What the legacy `SEED_STATE` launch argument stands for. */
		val Canonical = DebugSessionSeed(
			sessionId = "auth-session-initial",
			accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.exchange.mock.access",
			refreshToken = "refresh.mock.token.value",
			usbId = "11-11111",
			password = "123456",
			coachmarksSeen = true,
			mainSection = MainSection.SUMMARY
		)
	}
}
