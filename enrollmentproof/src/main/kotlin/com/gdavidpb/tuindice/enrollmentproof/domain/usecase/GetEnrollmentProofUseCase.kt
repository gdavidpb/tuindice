package com.gdavidpb.tuindice.enrollmentproof.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.EventUseCase
import com.gdavidpb.tuindice.base.utils.ConfigKeys
import com.gdavidpb.tuindice.base.utils.annotations.Timeout
import com.gdavidpb.tuindice.base.utils.extensions.*
import com.gdavidpb.tuindice.enrollmentproof.domain.error.GetEnrollmentError
import com.gdavidpb.tuindice.enrollmentproof.domain.repository.EnrollmentProofRepository

@Timeout(key = ConfigKeys.TIME_OUT_GET_ENROLLMENT)
class GetEnrollmentProofUseCase(
	private val authRepository: AuthRepository,
	private val networkRepository: NetworkRepository,
	private val enrollmentProofRepository: EnrollmentProofRepository
) : EventUseCase<Unit, String, GetEnrollmentError>() {
	override suspend fun executeOnBackground(params: Unit): String {
		val activeAuth = authRepository.getActiveAuth()

		val enrollmentProof = enrollmentProofRepository.getEnrollmentProof(
			uid = activeAuth.uid
		)!! // TODO route exception to Unavailable

		return enrollmentProof.source
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