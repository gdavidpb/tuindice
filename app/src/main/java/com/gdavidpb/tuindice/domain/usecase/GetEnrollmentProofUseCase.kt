package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.Credentials
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.service.DstAuth
import com.gdavidpb.tuindice.domain.model.service.DstCredentials
import com.gdavidpb.tuindice.domain.repository.DstRepository
import com.gdavidpb.tuindice.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.domain.repository.StorageRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.EventUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.GetEnrollmentError
import com.gdavidpb.tuindice.utils.ConfigKeys
import com.gdavidpb.tuindice.utils.Paths
import com.gdavidpb.tuindice.utils.annotations.Timeout
import com.gdavidpb.tuindice.utils.extensions.*
import java.io.File
import java.io.StreamCorruptedException

@Timeout(key = ConfigKeys.TIME_OUT_GET_ENROLLMENT)
class GetEnrollmentProofUseCase(
        private val dstRepository: DstRepository,
        private val settingsRepository: SettingsRepository,
        private val storageRepository: StorageRepository<File>,
        private val networkRepository: NetworkRepository
) : EventUseCase<Quarter, File, GetEnrollmentError>() {
    override suspend fun executeOnBackground(params: Quarter): File {
        val quarterName = params.startDate.formatQuarterName(params.endDate)
        val enrollmentFilePath = File(Paths.ENROLLMENT, "$quarterName.pdf").path
        val enrollmentFile = storageRepository.get(enrollmentFilePath)

        if (!enrollmentFile.exists()) {
            val credentials = settingsRepository.getCredentials()

            credentials.auth(serviceUrl = BuildConfig.ENDPOINT_DST_ENROLLMENT_AUTH)

            val inputStream = dstRepository.getEnrollmentProof().byteStream()
            val outputStream = storageRepository.outputStream(enrollmentFilePath)

            inputStream.copyToAndClose(outputStream)
        }

        return enrollmentFile
    }

    override suspend fun executeOnException(throwable: Throwable): GetEnrollmentError? {
        val causes = throwable.causes()

        return when {
            causes.isAccountDisabled() -> GetEnrollmentError.AccountDisabled
            throwable is StreamCorruptedException -> GetEnrollmentError.NotFound
            throwable.isInvalidCredentials() -> GetEnrollmentError.NotFound
            throwable.isTimeout() -> GetEnrollmentError.Timeout
            throwable.isNotEnrolled() -> GetEnrollmentError.NotEnrolled
            throwable.isConnection() -> GetEnrollmentError.NoConnection(networkRepository.isAvailable())
            else -> null
        }
    }

    private suspend fun Credentials.auth(serviceUrl: String): DstAuth {
        val credentials = DstCredentials(
                usbId = usbId,
                password = password,
                serviceUrl = serviceUrl
        )

        return dstRepository.signIn(credentials)
    }
}