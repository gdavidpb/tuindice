package com.gdavidpb.tuindice.enrollmentproof.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.repository.FileRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.enrollmentproof.domain.repository.EnrollmentProofRepository
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error.FetchEnrollmentProofUseCaseError
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.exceptionhandler.FetchEnrollmentProofExceptionHandler
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FetchEnrollmentProofUseCaseTest {
	@Test
	fun execute_whenFileCanBeOpened_emitsFileReference() = runBlocking {
		val useCase = FetchEnrollmentProofUseCase(
			applicationRepository = FakeFileGateway(canOpen = true),
			enrollmentProofRepository = FakeEnrollmentProofRepository(
				enrollmentProof = EnrollmentProof(
					source = "/tmp/enrollment-proof.pdf",
					content = "dummy"
				)
			),
			exceptionHandler = FetchEnrollmentProofExceptionHandler(
				networkRepository = FakeNetworkStatusGateway(),
				reportingRepository = FakeReportingGateway()
			)
		)

		val states = useCase.execute(Unit).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<PlatformFileRef, FetchEnrollmentProofUseCaseError>>(states[0])
		val success = assertIs<UseCaseState.Data<PlatformFileRef, FetchEnrollmentProofUseCaseError>>(states[1])
		assertEquals(PlatformFileRef("/tmp/enrollment-proof.pdf"), success.value)
	}

	@Test
	fun execute_whenFileCannotBeOpened_emitsUnsupportedFileError() = runBlocking {
		val useCase = FetchEnrollmentProofUseCase(
			applicationRepository = FakeFileGateway(canOpen = false),
			enrollmentProofRepository = FakeEnrollmentProofRepository(
				enrollmentProof = EnrollmentProof(
					source = "/tmp/enrollment-proof.pdf",
					content = "dummy"
				)
			),
			exceptionHandler = FetchEnrollmentProofExceptionHandler(
				networkRepository = FakeNetworkStatusGateway(),
				reportingRepository = FakeReportingGateway()
			)
		)

		val states = useCase.execute(Unit).toList()

		assertEquals(2, states.size)
		assertIs<UseCaseState.Loading<PlatformFileRef, FetchEnrollmentProofUseCaseError>>(states[0])
		val failure = assertIs<UseCaseState.Error<PlatformFileRef, FetchEnrollmentProofUseCaseError>>(states[1])
		assertEquals(FetchEnrollmentProofUseCaseError.UnsupportedFile, failure.error)
	}
}

private class FakeEnrollmentProofRepository(
	private val enrollmentProof: EnrollmentProof
) : EnrollmentProofRepository {
	override suspend fun getEnrollmentProof(): EnrollmentProof = enrollmentProof
}

private class FakeFileGateway(
	private val canOpen: Boolean
) : FileRepository {
	override suspend fun createTemporaryFile(nameHint: String): PlatformFileRef {
		return PlatformFileRef("/tmp/$nameHint")
	}

	override suspend fun canOpen(fileRef: PlatformFileRef): Boolean = canOpen
}

private class FakeNetworkStatusGateway : NetworkRepository {
	override fun isAvailable(): Boolean = true
}

private class FakeReportingGateway : ReportingRepository {
	override fun setIdentifier(identifier: String) = Unit

	override fun logException(throwable: Throwable) = Unit

	override fun logMessage(message: String) = Unit

	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}
