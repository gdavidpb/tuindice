package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.repository.AssistantServerDisabledDataRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantServerDisabledRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantServerModeRepository
import com.gdavidpb.tuindice.assistant.utils.DEFAULT_SERVER_DISABLED_SECONDS
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
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
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

	// Until when the server holds the assistant off. Null when it does not.
	private val serverDisabledUntil = MutableStateFlow<Instant?>(null)

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

	// The last 503 decides: its retry time replaces the window of the one before it.
	override fun markServerDisabled(retryAfterSeconds: Int?) {
		val waitSeconds = retryAfterSeconds ?: DEFAULT_SERVER_DISABLED_SECONDS

		serverDisabledUntil.value = clock.now() + waitSeconds.seconds
	}

	// The window belongs to the session that received the 503, and the server's mode to the student who was given it. A
	// new session starts with neither: the mode is the configuration's last word again.
	override suspend fun clearSessionMemory() {
		serverDisabledUntil.value = null
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
		return serverDisabledUntil.transformLatest { until ->
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
		val until = serverDisabledUntil.value ?: return false

		return clock.now() < until
	}
}
