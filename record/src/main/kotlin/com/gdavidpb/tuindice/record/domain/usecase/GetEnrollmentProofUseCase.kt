package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.repository.ServicesRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.StorageRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.EventUseCase
import com.gdavidpb.tuindice.base.utils.ConfigKeys
import com.gdavidpb.tuindice.record.utils.Paths
import com.gdavidpb.tuindice.base.utils.annotations.Timeout
import com.gdavidpb.tuindice.base.utils.extensions.*
import com.gdavidpb.tuindice.record.domain.error.GetEnrollmentError
import java.io.File

@Timeout(key = ConfigKeys.TIME_OUT_GET_ENROLLMENT)
class GetEnrollmentProofUseCase(
	private val apiRepository: ServicesRepository,
	private val storageRepository: StorageRepository,
	private val networkRepository: NetworkRepository
) : EventUseCase<Quarter, File, GetEnrollmentError>() {
	override suspend fun executeOnBackground(params: Quarter): File {
		val enrollmentFilePath = File(Paths.ENROLLMENT, "${params.name}.pdf").path
		val enrollmentFile = storageRepository.get(enrollmentFilePath)

		if (!enrollmentFile.exists()) {
			val enrollmentProof = apiRepository.getEnrollmentProof()

			val inputStream = enrollmentProof.inputStream
			val outputStream = storageRepository.outputStream(enrollmentFilePath)

			inputStream.copyToAndClose(outputStream)
		}

		return enrollmentFile
	}

	override suspend fun executeOnException(throwable: Throwable): GetEnrollmentError? {
		return when {
			throwable.isForbidden() -> GetEnrollmentError.AccountDisabled
			throwable.isNotFound() -> GetEnrollmentError.NotFound
			throwable.isUnavailable() -> GetEnrollmentError.Unavailable
			throwable.isConflict() -> GetEnrollmentError.OutdatedPassword
			throwable.isTimeout() -> GetEnrollmentError.Timeout
			throwable.isConnection() -> GetEnrollmentError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}