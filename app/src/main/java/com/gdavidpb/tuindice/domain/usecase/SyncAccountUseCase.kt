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
import com.gdavidpb.tuindice.utils.annotations.Timeout
import com.gdavidpb.tuindice.utils.extensions.computeGradeSum
import com.gdavidpb.tuindice.utils.extensions.isConnection
import com.gdavidpb.tuindice.utils.extensions.isInvalidCredentials
import com.gdavidpb.tuindice.utils.extensions.isTimeout
import com.gdavidpb.tuindice.utils.mappers.buildAccount
import com.gdavidpb.tuindice.utils.mappers.toQuarter

@Timeout(key = ConfigKeys.TIME_OUT_SYNC)
class SyncAccountUseCase(
        private val dstRepository: DstRepository,
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository,
        private val settingsRepository: SettingsRepository,
        private val networkRepository: NetworkRepository
) : ResultUseCase<Unit, Boolean, SyncError>() {
    override suspend fun executeOnBackground(params: Unit): Boolean {
        val activeAuth = authRepository.getActiveAuth()
        val isUpdated = databaseRepository.isUpdated(uid = activeAuth.uid)

        if (isUpdated) return false

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

            /* Add enrollment quarter */
            if (enrollment != null) {
                val enrollmentQuarter = enrollment.toQuarter(uid = activeAuth.uid)

                quarters.add(enrollmentQuarter)

                /* Compute gradeSum */
                val gradeSum = quarters.computeGradeSum(until = enrollmentQuarter)

                quarters.add(quarters.removeLast().copy(gradeSum = gradeSum))
            }

            quarters.forEach { quarter ->
                addQuarter(uid = activeAuth.uid, quarter = quarter)
            }
        }

        return true
    }

    override suspend fun executeOnException(throwable: Throwable): SyncError? {
        return when {
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
            dstRepository.signIn(credentials)
        }.getOrElse { throwable ->
            when {
                throwable.isInvalidCredentials() -> throw OutdatedPasswordException()
                else -> throw throwable
            }
        }
    }
}