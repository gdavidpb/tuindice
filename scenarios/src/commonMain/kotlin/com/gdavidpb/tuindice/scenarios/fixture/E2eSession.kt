package com.gdavidpb.tuindice.scenarios.fixture

/** The session values a seeded launch hands the app; they equal the exchange response of the account's mock. */
data class E2eSession(
	val sessionId: String,
	val accessToken: String,
	val refreshToken: String
)
