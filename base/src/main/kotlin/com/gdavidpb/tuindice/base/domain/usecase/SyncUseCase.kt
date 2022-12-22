package com.gdavidpb.tuindice.base.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.exception.IllegalAuthProviderException
import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase
import com.gdavidpb.tuindice.base.domain.usecase.error.SyncError
import com.gdavidpb.tuindice.base.utils.ConfigKeys
import com.gdavidpb.tuindice.base.utils.Topics
import com.gdavidpb.tuindice.base.utils.annotations.Timeout
import com.gdavidpb.tuindice.base.utils.extensions.*

@Timeout(key = ConfigKeys.TIME_OUT_SYNC)
class SyncUseCase(
	private val serviceRepository: ServicesRepository,
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

		/* Api migration */
		check(authRepository.getAuthProvider() == "custom") {
			throw IllegalAuthProviderException()
		}

		/* Try to fetch remote configs */
		configRepository.tryFetchAndActivate()

		/* Update messaging service token */
		val token = messagingRepository.getToken()

		if (token != null) databaseRepository.updateToken(uid = activeAuth.uid, token = token)

		/* Subscribe to general topic */
		if (!settingsRepository.isSubscribedToTopic(Topics.TOPIC_GENERAL)) {
			messagingRepository.subscribeToTopic(Topics.TOPIC_GENERAL)
			settingsRepository.saveSubscriptionTopic(Topics.TOPIC_GENERAL)
		}

		if (isUpdated) return false

		/* Call sync API */
		serviceRepository.sync()

		return true
	}

	override suspend fun executeOnException(throwable: Throwable): SyncError? {
		return when {
			throwable.isForbidden() -> SyncError.AccountDisabled
			throwable.isUnavailable() -> SyncError.Unavailable
			throwable.isConflict() -> SyncError.OutdatedPassword
			throwable.isTimeout() -> SyncError.Timeout
			throwable.isIllegalAuthProvider() -> SyncError.IllegalAuthProvider
			throwable.isConnection() -> SyncError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}