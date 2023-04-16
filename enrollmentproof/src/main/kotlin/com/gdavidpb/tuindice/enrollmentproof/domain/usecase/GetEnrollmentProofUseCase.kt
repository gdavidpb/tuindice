package com.gdavidpb.tuindice.enrollmentproof.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.enrollmentproof.domain.exception.EnrollmentProofNotFoundException
import com.gdavidpb.tuindice.enrollmentproof.domain.repository.EnrollmentProofRepository
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error.GetEnrollmentError
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.exceptionhandler.GetEnrollmentProofExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GetEnrollmentProofUseCase(
	private val authRepository: AuthRepository,
	private val enrollmentProofRepository: EnrollmentProofRepository,
	override val exceptionHandler: GetEnrollmentProofExceptionHandler
) : FlowUseCase<Unit, String, GetEnrollmentError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<String> {
		val activeAuth = authRepository.getActiveAuth()

		val enrollmentProof = enrollmentProofRepository.getEnrollmentProof(
			uid = activeAuth.uid
		) ?: throw EnrollmentProofNotFoundException()

		return flowOf(enrollmentProof.source)
	}
}