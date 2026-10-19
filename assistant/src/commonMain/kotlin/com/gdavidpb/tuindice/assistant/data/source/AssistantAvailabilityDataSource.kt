package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.repository.AssistantServerDisabledDataRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantServerDisabledRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantServerModeRepository
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantAvailability
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import com.gdavidpb.tuindice.base.domain.repository.AssistantAvailabilityRepository
import com.gdavidpb.tuindice.base.domain.repository.AssistantConfigRepository
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
	private val configRepository: AssistantConfigRepository,
	private val clock: Clock
) : AssistantAvailabilityRepository,
	AssistantServerDisabledDataRepository,
	AssistantServerDisabledRepository,
	AssistantServerModeRepository,
	SessionMemory {

	// Everything the availability holds lives in one state, so a write made for a session checks that session and writes
	// in one step: a request answered after its session ended finds another generation and changes nothing.
	private val state = MutableStateFlow(AvailabilityState())

	override fun observeAvailability(): Flow<AssistantAvailability> {
		return combine(modeFlow(), serverDisabledFlow()) { mode, serverDisabled ->
			AssistantAvailability(mode = mode, serverDisabled = serverDisabled)
		}.distinctUntilChanged()
	}

	override fun currentAvailability(): AssistantAvailability {
		return AssistantAvailability(
			mode = state.value.reportedMode ?: AssistantMode.OFF,
			serverDisabled = isServerDisabledNow()
		)
	}

	override fun sessionMark(): Int = state.value.sessionGeneration

	// The server's word on the mode is the student's: it is kept only for the session the status was read in.
	override fun reportServerMode(mode: AssistantMode, sessionMark: Int) {
		state.update { current ->
			if (current.sessionGeneration != sessionMark) current else current.copy(reportedMode = mode)
		}
	}

	// The last 503 decides: its retry time replaces the window of the one before it. A 503 answered for a session that has
	// ended is not held, so it cannot switch off the assistant of the one that follows.
	override fun markServerDisabled(retryAfterSeconds: Int?, sessionMark: Int) {
		val waitSeconds = retryAfterSeconds ?: DEFAULT_SERVER_DISABLED_SECONDS
		val until = clock.now() + waitSeconds.seconds

		state.update { current ->
			if (current.sessionGeneration != sessionMark) current else current.copy(serverDisabledUntil = until)
		}
	}

	// The window and the server's mode belong to the session that received them. A new session starts with neither: the
	// mode is the configuration's last word again, and the generation moves on so late answers find nothing to write.
	override suspend fun clearSessionMemory() {
		state.update { current ->
			AvailabilityState(
				configuredMode = current.configuredMode,
				reportedMode = current.configuredMode,
				sessionGeneration = current.sessionGeneration + 1
			)
		}
	}

	// The configuration's changes and the server's reports both move the mode, and the flow follows the last of them.
	private fun modeFlow(): Flow<AssistantMode> {
		return merge(
			configRepository.observeAssistantMode().onEach { mode ->
				state.update { current -> current.copy(configuredMode = mode, reportedMode = mode) }
			},
			state.map { current -> current.reportedMode }.filterNotNull()
		).distinctUntilChanged()
	}

	// Reads true while the window lasts. A timer lowers the flag at its deadline, and a new window restarts it.
	@OptIn(ExperimentalCoroutinesApi::class)
	private fun serverDisabledFlow(): Flow<Boolean> {
		return state.map { current -> current.serverDisabledUntil }
			.distinctUntilChanged()
			.transformLatest { until ->
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
		val until = state.value.serverDisabledUntil ?: return false

		return clock.now() < until
	}

	private data class AvailabilityState(
		// The mode the configuration reported last. AssistantConfigRepository has no synchronous read of it, so until someone
		// observes the availability this reads as off.
		val configuredMode: AssistantMode? = null,
		// The mode the availability follows: the last word of the configuration or of the server, whichever came last.
		val reportedMode: AssistantMode? = null,
		// Until when the server holds the assistant off.
		val serverDisabledUntil: Instant? = null,
		// Counts the sessions that ended on this device; a write made for an earlier session finds another generation.
		val sessionGeneration: Int = 0
	)
}
