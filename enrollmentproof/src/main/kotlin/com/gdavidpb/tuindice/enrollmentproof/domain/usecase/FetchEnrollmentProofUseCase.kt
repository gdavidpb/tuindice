package com.gdavidpb.tuindice.enrollmentproof.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.enrollmentproof.domain.exception.EnrollmentProofNotFoundException
import com.gdavidpb.tuindice.enrollmentproof.domain.repository.EnrollmentProofRepository
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error.FetchEnrollmentProofError
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.exceptionhandler.FetchEnrollmentProofExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FetchEnrollmentProofUseCase(
	private val authRepository: AuthRepository,
	private val applicationRepository: ApplicationRepository,
	private val enrollmentProofRepository: EnrollmentProofRepository,
	override val exceptionHandler: FetchEnrollmentProofExceptionHandler
) : FlowUseCase<Unit, String, FetchEnrollmentProofError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<String> {
		val activeAuth = authRepository.getActiveAuth()

		val enrollmentProof = enrollmentProofRepository.getEnrollmentProof(
			uid = activeAuth.uid
		) ?: throw EnrollmentProofNotFoundException()

		val canOpenEnrollmentProof =
			applicationRepository.canOpenFile(path = enrollmentProof.source)

		check(canOpenEnrollmentProof) { throw UnsupportedOperationException() }

		return flowOf(enrollmentProof.source)
	}
}