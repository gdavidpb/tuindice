package com.gdavidpb.tuindice.enrollmentproof.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.repository.FileRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.enrollmentproof.domain.repository.EnrollmentProofRepository
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error.FetchEnrollmentProofUseCaseError
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.exceptionhandler.FetchEnrollmentProofExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FetchEnrollmentProofUseCase(
	private val applicationRepository: FileRepository,
	private val enrollmentProofRepository: EnrollmentProofRepository,
	override val exceptionHandler: FetchEnrollmentProofExceptionHandler
) : FlowUseCase<Unit, PlatformFileRef, FetchEnrollmentProofUseCaseError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<PlatformFileRef> {
		val enrollmentProof = enrollmentProofRepository.getEnrollmentProof()
		val fileRef = PlatformFileRef(enrollmentProof.source)

		val canOpenEnrollmentProof = applicationRepository.canOpen(fileRef = fileRef)

		check(canOpenEnrollmentProof) { throw UnsupportedOperationException() }

		return flowOf(fileRef)
	}
}
