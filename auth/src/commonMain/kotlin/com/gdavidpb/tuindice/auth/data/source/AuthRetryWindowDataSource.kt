package com.gdavidpb.tuindice.auth.data.source

import com.gdavidpb.tuindice.auth.domain.repository.AuthRetryWindowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

private const val DEFAULT_UNAVAILABLE_SECONDS = 30L
private const val DEFAULT_TOO_MANY_REQUESTS_SECONDS = 60L
private const val MAX_WAIT_SECONDS = 300L
private const val MAX_DOUBLINGS = 8

class AuthRetryWindowDataSource(
	private val timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic
) : AuthRetryWindowRepository {
	private val entries = MutableStateFlow<Map<String, Entry>>(emptyMap())

	override fun remainingMillis(key: String): Long {
		val blockedUntil = entries.value[key]?.blockedUntil ?: return 0L

		return maxOf(0L, -blockedUntil.elapsedNow().inWholeMilliseconds)
	}

	override fun signInRemainingMillis(account: String): Long {
		return maxOf(
			remainingMillis(account),
			remainingMillis(AuthRetryWindowRepository.EXCHANGE_KEY)
		)
	}

	override fun recordUnavailable(key: String, retryAfterSeconds: Long?) {
		entries.update { current ->
			val entry = current[key] ?: Entry()
			val streak = entry.unavailableStreak + 1
			val base = retryAfterSeconds?.takeIf { it > 0 } ?: DEFAULT_UNAVAILABLE_SECONDS
			val wait = (base shl (streak - 1).coerceAtMost(MAX_DOUBLINGS)).coerceAtMost(MAX_WAIT_SECONDS)

			current + (key to Entry(blockedUntil = blockFor(wait.seconds), unavailableStreak = streak))
		}
	}

	override fun recordTooManyRequests(key: String, retryAfterSeconds: Long?) {
		entries.update { current ->
			val entry = current[key] ?: Entry()
			val wait = (retryAfterSeconds?.takeIf { it > 0 } ?: DEFAULT_TOO_MANY_REQUESTS_SECONDS)
				.coerceAtMost(MAX_WAIT_SECONDS)

			current + (key to entry.copy(blockedUntil = blockFor(wait.seconds)))
		}
	}

	override fun recordSuccess(key: String) {
		entries.update { current -> current - key }
	}

	private fun blockFor(wait: Duration): ComparableTimeMark = timeSource.markNow() + wait

	private data class Entry(
		val blockedUntil: ComparableTimeMark? = null,
		val unavailableStreak: Int = 0
	)
}
