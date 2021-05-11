package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.Credentials
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.exception.OutdatedPasswordException
import com.gdavidpb.tuindice.domain.model.service.DstAuth
import com.gdavidpb.tuindice.domain.model.service.DstCredentials
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.SyncError
import com.gdavidpb.tuindice.utils.ConfigKeys
import com.gdavidpb.tuindice.utils.Topics
import com.gdavidpb.tuindice.utils.annotations.Timeout
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.buildAccount
import com.gdavidpb.tuindice.utils.mappers.toQuarter

@Timeout(key = ConfigKeys.TIME_OUT_SYNC)
class SyncAccountUseCase(
        private val dstRepository: DstRepository,
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

        /* Reload auth */
        authRepository.reloadActiveAuth()

        /* Get credentials */
        val credentials = settingsRepository.getCredentials()

        /* Record service auth */
        credentials.auth(serviceUrl = BuildConfig.ENDPOINT_DST_RECORD_AUTH)

        val personal = dstRepository.getPersonalData()
        val record = dstRepository.getRecordData()

        /* Enrollment service auth */
        val enrollmentAuth = credentials.auth(serviceUrl = BuildConfig.ENDPOINT_DST_ENROLLMENT_AUTH)

        val enrollment = if (enrollmentAuth.isSuccessful)
            dstRepository.getEnrollment()
        else
            null

        databaseRepository.runBatch {
            /* Add account */
            val account = buildAccount(
                    auth = activeAuth,
                    personal = personal,
                    record = record
            )

            addAccount(uid = activeAuth.uid, account = account)

            val quarters = mutableListOf<Quarter>()

            /* Add record quarters */
            val recordQuarters = record.quarters.map { it.toQuarter(uid = activeAuth.uid) }

            quarters.addAll(recordQuarters)

            /* Add current quarter */
            if (enrollment != null) {
                val currentQuarter = enrollment.toQuarter(uid = activeAuth.uid)

                /* Check if current quarter is closed */
                val isCurrentQuarterClosed = recordQuarters.any { it.id == currentQuarter.id }

                if (!isCurrentQuarterClosed) {
                    /* Compute gradeSum */
                    val gradeSum = (recordQuarters + currentQuarter).computeGradeSum(until = currentQuarter)

                    quarters.add(currentQuarter.copy(gradeSum = gradeSum))
                }
            }

            quarters.forEach { quarter ->
                addQuarter(uid = activeAuth.uid, quarter = quarter)
            }
        }

        return true
    }

    override suspend fun executeOnException(throwable: Throwable): SyncError? {
        val causes = throwable.causes()

        return when {
            causes.isAccountDisabled() -> SyncError.AccountDisabled
            throwable is OutdatedPasswordException -> SyncError.OutdatedPassword
            throwable.isTimeout() -> SyncError.Timeout
            throwable.isConnection() -> SyncError.NoConnection(networkRepository.isAvailable())
            else -> null
        }
    }

    private suspend fun Credentials.auth(serviceUrl: String): DstAuth {
        val credentials = DstCredentials(
                usbId = usbId,
                password = password,
                serviceUrl = serviceUrl
        )

        return runCatching {
            dstRepository.checkCredentials(credentials)

            dstRepository.signIn(credentials)
        }.getOrElse { throwable ->
            when {
                throwable.isInvalidCredentials() -> throw OutdatedPasswordException()
                else -> throw throwable
            }
        }
    }
}