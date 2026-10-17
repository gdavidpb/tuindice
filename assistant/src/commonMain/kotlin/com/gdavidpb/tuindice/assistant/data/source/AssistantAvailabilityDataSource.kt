package com.gdavidpb.tuindice.assistant.data.source

import com.gdavidpb.tuindice.assistant.data.repository.AssistantServerDisabledDataRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantServerDisabledRepository
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
	SessionMemory {

	// The mode the configuration reported last. ConfigRepository has no synchronous read of it, so until someone
	// observes the availability this reads as off.
	private val configuredMode = MutableStateFlow<AssistantMode?>(null)

	// Until when the server holds the assistant off. Null when it does not.
	private val serverDisabledUntil = MutableStateFlow<Instant?>(null)

	override fun observeAvailability(): Flow<AssistantAvailability> {
		return combine(configuredModeFlow(), serverDisabledFlow()) { mode, serverDisabled ->
			AssistantAvailability(mode = mode, serverDisabled = serverDisabled)
		}.distinctUntilChanged()
	}

	override fun currentAvailability(): AssistantAvailability {
		return AssistantAvailability(
			mode = configuredMode.value ?: AssistantMode.OFF,
			serverDisabled = isServerDisabledNow()
		)
	}

	// The last 503 decides: its retry time replaces the window of the one before it.
	override fun markServerDisabled(retryAfterSeconds: Int?) {
		val waitSeconds = retryAfterSeconds ?: DEFAULT_SERVER_DISABLED_SECONDS

		serverDisabledUntil.value = clock.now() + waitSeconds.seconds
	}

	// The window belongs to the session that received the 503. A new session starts with no window.
	override suspend fun clearSessionMemory() {
		serverDisabledUntil.value = null
	}

	private fun configuredModeFlow(): Flow<AssistantMode> {
		return configRepository.observeAssistantMode().onEach { mode -> configuredMode.value = mode }
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
