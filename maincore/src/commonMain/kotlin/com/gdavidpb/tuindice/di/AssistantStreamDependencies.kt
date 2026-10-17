package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.repository.AppEnvironmentRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.domain.repository.SessionRecoveryRepository
import io.ktor.client.plugins.logging.Logger

// What the assistant's stream client is built from: the environment and the config it reads, and the session it
// authenticates with. These are the ones the shared client takes.
data class AssistantStreamDependencies(
	val appEnvironmentRepository: AppEnvironmentRepository,
	val configRepository: ConfigRepository,
	val sessionRepository: SessionRepository,
	val sessionRecoveryRepository: SessionRecoveryRepository,
	val sessionCoroutineScope: SessionCoroutineScope,
	val logger: Logger
)
