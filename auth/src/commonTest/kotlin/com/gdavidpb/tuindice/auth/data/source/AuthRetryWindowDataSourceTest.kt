package com.gdavidpb.tuindice.auth.data.source

import com.gdavidpb.tuindice.auth.domain.repository.AuthRetryWindowRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class AuthRetryWindowDataSourceTest {
	private val clock = TestTimeSource()
	private val window = AuthRetryWindowDataSource(timeSource = clock)

	@Test
	fun withoutARecordedWait_nothingIsHeldBack() {
		assertEquals(0L, window.remainingMillis("20-26123"))
		assertEquals(0L, window.signInRemainingMillis("20-26123"))
	}

	@Test
	fun unavailableWithoutRetryAfter_waitsThirtySeconds_andCountsDown() {
		window.recordUnavailable(key = "20-26123", retryAfterSeconds = null)

		assertEquals(30_000L, window.remainingMillis("20-26123"))

		clock += 10.seconds

		assertEquals(20_000L, window.remainingMillis("20-26123"))

		clock += 21.seconds

		assertEquals(0L, window.remainingMillis("20-26123"))
	}

	@Test
	fun unavailableWithRetryAfter_usesTheServersSeconds() {
		window.recordUnavailable(key = "20-26123", retryAfterSeconds = 12L)

		assertEquals(12_000L, window.remainingMillis("20-26123"))
	}

	@Test
	fun consecutiveUnavailable_doublesTheWait_upToFiveMinutes() {
		val expected = listOf(30L, 60L, 120L, 240L, 300L, 300L)

		expected.forEach { seconds ->
			window.recordUnavailable(key = "20-26123", retryAfterSeconds = null)
			assertEquals(seconds * 1_000L, window.remainingMillis("20-26123"))
			clock += 301.seconds
		}
	}

	@Test
	fun aSuccess_forgetsTheWaitAndTheStreak() {
		window.recordUnavailable(key = "20-26123", retryAfterSeconds = null)
		window.recordUnavailable(key = "20-26123", retryAfterSeconds = null)

		window.recordSuccess(key = "20-26123")

		assertEquals(0L, window.remainingMillis("20-26123"))

		window.recordUnavailable(key = "20-26123", retryAfterSeconds = null)

		assertEquals(30_000L, window.remainingMillis("20-26123"))
	}

	@Test
	fun tooManyRequests_defaultsToSixtySeconds_orTakesTheServersWait() {
		window.recordTooManyRequests(key = "20-26123", retryAfterSeconds = null)

		assertEquals(60_000L, window.remainingMillis("20-26123"))

		window.recordTooManyRequests(key = "20-26123", retryAfterSeconds = 45L)

		assertEquals(45_000L, window.remainingMillis("20-26123"))
	}

	@Test
	fun aWait_isKeptPerKey_andSignInSeesTheLongestOfItsAccountAndTheExchange() {
		window.recordUnavailable(key = "20-26123", retryAfterSeconds = 10L)
		window.recordUnavailable(key = AuthRetryWindowRepository.EXCHANGE_KEY, retryAfterSeconds = 50L)

		assertEquals(0L, window.remainingMillis("11-11111"))
		assertEquals(10_000L, window.remainingMillis("20-26123"))
		assertEquals(50_000L, window.signInRemainingMillis("20-26123"))
		assertEquals(50_000L, window.signInRemainingMillis("11-11111"))
	}
}
