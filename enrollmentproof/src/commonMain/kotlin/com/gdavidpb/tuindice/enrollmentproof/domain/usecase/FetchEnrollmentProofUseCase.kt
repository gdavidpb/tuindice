package com.gdavidpb.tuindice.enrollmentproof.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.FileRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.enrollmentproof.domain.repository.EnrollmentProofRepository
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error.FetchEnrollmentProofUseCaseError
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.exceptionhandler.FetchEnrollmentProofExceptionHandler
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FetchEnrollmentProofUseCase(
	private val applicationRepository: FileRepository,
	private val enrollmentProofRepository: EnrollmentProofRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: FetchEnrollmentProofExceptionHandler
) : FlowUseCase<Unit, PlatformFile, FetchEnrollmentProofUseCaseError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<PlatformFile> {
		val enrollmentProof = enrollmentProofRepository.getEnrollmentProof()
		val file = PlatformFile(enrollmentProof.source)

		val canOpenEnrollmentProof = applicationRepository.canOpen(file = file)

		check(canOpenEnrollmentProof) { throw UnsupportedOperationException() }

		return flowOf(file)
	}
}
