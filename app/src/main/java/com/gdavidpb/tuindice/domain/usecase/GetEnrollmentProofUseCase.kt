package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.Credentials
import com.gdavidpb.tuindice.domain.model.service.DstAuth
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.GetEnrollmentError
import com.gdavidpb.tuindice.utils.Paths
import com.gdavidpb.tuindice.utils.extensions.copyToAndClose
import com.gdavidpb.tuindice.utils.extensions.isConnectionIssue
import com.gdavidpb.tuindice.utils.extensions.isInvalidCredentials
import com.gdavidpb.tuindice.utils.extensions.isNotEnrolled
import com.gdavidpb.tuindice.utils.mappers.formatQuarterTitle
import com.gdavidpb.tuindice.utils.mappers.toDstCredentials
import java.io.File
import java.io.StreamCorruptedException

class GetEnrollmentProofUseCase(
        private val authRepository: AuthRepository,
        private val dstRepository: DstRepository,
        private val databaseRepository: DatabaseRepository,
        private val settingsRepository: SettingsRepository,
        private val storageRepository: StorageRepository<File>,
        private val networkRepository: NetworkRepository
) : EventUseCase<Unit, File, GetEnrollmentError>() {
    override suspend fun executeOnBackground(params: Unit): File {
        val activeUId = authRepository.getActiveAuth().uid

        val currentQuarter = databaseRepository.getCurrentQuarter(uid = activeUId)

        /* Try to get current quarter enrollment proof file */
        val enrollmentTitle = with(currentQuarter) {
            (startDate to endDate).formatQuarterTitle()
        }

        val enrollmentName = File(Paths.ENROLLMENT, "$enrollmentTitle.pdf").path
        val enrollmentFile = storageRepository.get(enrollmentName)

        if (!enrollmentFile.exists()) {
            /* Get credentials */
            val credentials = settingsRepository.getCredentials()

            /* Enrollment service auth */
            credentials.auth(serviceUrl = BuildConfig.ENDPOINT_DST_ENROLLMENT_AUTH)

            /* Get enrollment proof file from dst service */

            val inputStream = dstRepository.getEnrollmentProof().byteStream()

            /* Save file in local storage */

            val outputStream = storageRepository.outputStream(enrollmentName)

            inputStream.copyToAndClose(outputStream)
        }

        return enrollmentFile
    }

    override suspend fun executeOnException(throwable: Throwable): GetEnrollmentError? {
        return when {
            throwable is StreamCorruptedException -> GetEnrollmentError.NotFound
            throwable.isInvalidCredentials() -> GetEnrollmentError.InvalidCredentials
            throwable.isNotEnrolled() -> GetEnrollmentError.NotEnrolled
            throwable.isConnectionIssue() -> GetEnrollmentError.NoConnection(networkRepository.isAvailable())
            else -> null
        }
    }

    private suspend fun Credentials.auth(serviceUrl: String): DstAuth {
        storageRepository.delete(Paths.COOKIES)

        val request = toDstCredentials(serviceUrl)

        return dstRepository.signIn(request)
    }
}