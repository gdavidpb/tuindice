package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.repository.AssistantServerDisabledDataRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantServerDisabledRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantServerModeRepository
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantAvailability
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import com.gdavidpb.tuindice.base.domain.repository.AssistantAvailabilityRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class AssistantAvailabilityDataSource(
	private val configRepository: ConfigRepository,
	private val clock: Clock
) : AssistantAvailabilityRepository,
	AssistantServerDisabledDataRepository,
	AssistantServerDisabledRepository,
	AssistantServerModeRepository,
	SessionMemory {

	// The mode the configuration reported last. ConfigRepository has no synchronous read of it, so until someone
	// observes the availability this reads as off.
	private val configuredMode = MutableStateFlow<AssistantMode?>(null)

	// The mode the availability follows: the last word of the configuration or of the server, whichever came last. The
	// server's word is given with the status of the student, and it is forgotten when the session ends.
	private val reportedMode = MutableStateFlow<AssistantMode?>(null)

	// Until when the server holds the assistant off, and the session that window belongs to. The generation moves on
	// when the session ends, and a switch-off answered for an earlier session no longer matches it.
	private val serverDisabled = MutableStateFlow(ServerDisabledWindow())

	override fun observeAvailability(): Flow<AssistantAvailability> {
		return combine(modeFlow(), serverDisabledFlow()) { mode, serverDisabled ->
			AssistantAvailability(mode = mode, serverDisabled = serverDisabled)
		}.distinctUntilChanged()
	}

	override fun currentAvailability(): AssistantAvailability {
		return AssistantAvailability(
			mode = reportedMode.value ?: AssistantMode.OFF,
			serverDisabled = isServerDisabledNow()
		)
	}

	override fun reportServerMode(mode: AssistantMode) {
		reportedMode.value = mode
	}

	override fun sessionMark(): Int = serverDisabled.value.sessionGeneration

	// The last 503 decides: its retry time replaces the window of the one before it. A 503 answered for a session that has
	// ended is not held, so it cannot switch off the assistant of the one that follows.
	override fun markServerDisabled(retryAfterSeconds: Int?, sessionMark: Int) {
		val waitSeconds = retryAfterSeconds ?: DEFAULT_SERVER_DISABLED_SECONDS
		val until = clock.now() + waitSeconds.seconds

		serverDisabled.update { window ->
			if (window.sessionGeneration != sessionMark) window else window.copy(until = until)
		}
	}

	// The window belongs to the session that received the 503, and the server's mode to the student who was given it. A
	// new session starts with neither: the mode is the configuration's last word again.
	override suspend fun clearSessionMemory() {
		serverDisabled.update { window -> ServerDisabledWindow(sessionGeneration = window.sessionGeneration + 1) }
		reportedMode.value = configuredMode.value
	}

	// The configuration's changes and the server's reports both move the mode, and the flow follows the last of them.
	private fun modeFlow(): Flow<AssistantMode> {
		return merge(
			configRepository.observeAssistantMode().onEach { mode ->
				configuredMode.value = mode
				reportedMode.value = mode
			},
			reportedMode.filterNotNull()
		).distinctUntilChanged()
	}

	// Reads true while the window lasts. A timer lowers the flag at its deadline, and a new window restarts it.
	@OptIn(ExperimentalCoroutinesApi::class)
	private fun serverDisabledFlow(): Flow<Boolean> {
		return serverDisabled.map { window -> window.until }.transformLatest { until ->
			if (until == null) {
				emit(false)
				return@transformLatest
			}

			while (true) {
				val remaining = until - clock.now()

				if (remaining <= Duration.ZERO) {
					emit(false)
					return@transformLatest
				}

				emit(true)
				delay(remaining)
			}
		}.distinctUntilChanged()
	}

	private fun isServerDisabledNow(): Boolean {
		val until = serverDisabled.value.until ?: return false

		return clock.now() < until
	}

	private data class ServerDisabledWindow(
		val until: Instant? = null,
		val sessionGeneration: Int = 0
	)
}
