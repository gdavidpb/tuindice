package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.SyncError
import com.gdavidpb.tuindice.utils.ConfigKeys
import com.gdavidpb.tuindice.utils.Topics
import com.gdavidpb.tuindice.utils.annotations.Timeout
import com.gdavidpb.tuindice.utils.extensions.*

@Timeout(key = ConfigKeys.TIME_OUT_SYNC)
class SyncUseCase(
    private val apiRepository: ApiRepository,
    private val authRepository: AuthRepository,
    private val configRepository: ConfigRepository,
    private val databaseRepository: DatabaseRepository,
    private val settingsRepository: SettingsRepository,
    private val messagingRepository: MessagingRepository,
    private val networkRepository: NetworkRepository
) : ResultUseCase<Unit, Boolean, SyncError>() {
    override suspend fun executeOnBackground(params: Unit): Boolean {
        val activeAuth = authRepository.getActiveAuth()
        val isUpdated = databaseRepository.isUpdated(uid = activeAuth.uid)

        /* Try to fetch remote configs */
        configRepository.tryFetchAndActivate()

        /* Update messaging service token */
        val token = messagingRepository.getToken()

        if (token != null) databaseRepository.updateToken(uid = activeAuth.uid, token = token)

        /* Subscribe to version topic */
        if (!settingsRepository.isSubscribedToTopic(Topics.TOPIC_GENERAL)) {
            messagingRepository.subscribeToTopic(Topics.TOPIC_GENERAL)
            settingsRepository.storeTopicSubscription(Topics.TOPIC_GENERAL)
        }

        if (isUpdated) return false

        /* Call sync API */
        apiRepository.sync()

        /* Refresh cache */
        databaseRepository.cache(uid = activeAuth.uid)

        return true
    }

    override suspend fun executeOnException(throwable: Throwable): SyncError? {
        return when {
            throwable.isForbidden() -> SyncError.AccountDisabled
            throwable.isUnavailable() -> SyncError.Unavailable
            throwable.isConflict() -> SyncError.OutdatedPassword
            throwable.isTimeout() -> SyncError.Timeout
            throwable.isConnection() -> SyncError.NoConnection(networkRepository.isAvailable())
            else -> null
        }
    }
}