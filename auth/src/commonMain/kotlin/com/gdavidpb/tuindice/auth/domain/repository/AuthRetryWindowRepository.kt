package com.gdavidpb.tuindice.auth.domain.repository

/**
 * Memory of the waits the identity service asked for. A call whose key is inside its wait is not
 * sent; sign-in and the background session recovery share it so neither hammers a service that
 * already said it is busy. Kept in memory only: a restart is a fresh chance.
 */
interface AuthRetryWindowRepository {
	/** Milliseconds left before [key] may call the server again; zero when it may. */
	fun remainingMillis(key: String): Long

	/** The longest wait that still blocks a sign-in of [account] (its bootstrap or the token exchange). */
	fun signInRemainingMillis(account: String): Long

	/** A 503. Consecutive ones double the wait. */
	fun recordUnavailable(key: String, retryAfterSeconds: Long?)

	/** A 429. Takes the wait the server named, or a default. */
	fun recordTooManyRequests(key: String, retryAfterSeconds: Long?)

	/** The call went through: forgets the wait and the streak of 503s. */
	fun recordSuccess(key: String)

	companion object {
		/** The token exchange is not tied to an account the client knows, so it has a key of its own. */
		const val EXCHANGE_KEY = "token/exchange"
	}
}
