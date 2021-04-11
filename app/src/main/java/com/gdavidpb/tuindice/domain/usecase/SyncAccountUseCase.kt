package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.Credentials
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.exception.UnauthenticatedException
import com.gdavidpb.tuindice.domain.model.service.DstAuth
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.SyncError
import com.gdavidpb.tuindice.utils.Paths
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.mappers.buildAccount
import com.gdavidpb.tuindice.utils.mappers.toDstCredentials
import com.gdavidpb.tuindice.utils.mappers.toQuarter
import java.io.File

class SyncAccountUseCase(
        private val dstRepository: DstRepository,
        private val storageRepository: StorageRepository<File>,
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
        val recordAuth = credentials.auth(serviceUrl = BuildConfig.ENDPOINT_DST_RECORD_AUTH)

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
                    sigIn = recordAuth,
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
        val causes = throwable.causes()

        return when {
            throwable is UnauthenticatedException -> SyncError.Unauthenticated
            causes.isAccountDisabled() -> SyncError.AccountDisabled
            throwable.isInvalidCredentials() -> SyncError.InvalidCredentials
            throwable.isConnectionIssue() -> SyncError.NoConnection(networkRepository.isAvailable())
            else -> null
        }
    }

    private suspend fun Credentials.auth(serviceUrl: String): DstAuth {
        storageRepository.delete(Paths.COOKIES)

        val request = toDstCredentials(serviceUrl)

        return dstRepository.signIn(request)
    }
}