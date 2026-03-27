package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.record.domain.exception.SubjectIllegalArgumentException
import com.gdavidpb.tuindice.record.domain.usecase.error.SubjectUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.error.UpdateQuartersUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.SetSubjectGradeExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.UpdateQuartersExceptionHandler
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RecordExceptionHandlerTest {
	@Test
	fun setSubjectGradeExceptionHandler_mapsNotFound() {
		val throwable = clientRequestException(HttpStatusCode.NotFound, path = "/quarters/v1/qid/subjects/sid")

		val actual = SetSubjectGradeExceptionHandler().parseException(throwable)

		assertEquals(SubjectUseCaseError.NotFound, actual)
	}

	@Test
	fun setSubjectGradeExceptionHandler_mapsConflict() {
		val throwable = clientRequestException(HttpStatusCode.PreconditionFailed, path = "/quarters/v1/qid/subjects/sid")

		val actual = SetSubjectGradeExceptionHandler().parseException(throwable)

		assertEquals(SubjectUseCaseError.Conflict, actual)
	}

	@Test
	fun setSubjectGradeExceptionHandler_mapsValidationErrors() {
		val throwable = SubjectIllegalArgumentException(SubjectUseCaseError.OutOfRangeGrade)

		val actual = SetSubjectGradeExceptionHandler().parseException(throwable)

		assertEquals(SubjectUseCaseError.OutOfRangeGrade, actual)
	}

	@Test
	fun updateQuartersExceptionHandler_leavesConflictUnhandled() {
		val throwable = clientRequestException(HttpStatusCode.Conflict, path = "/quarters/v1")

		val actual = UpdateQuartersExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true)
		).parseException(throwable)

		assertEquals(null, actual)
	}

	@Test
	fun updateQuartersExceptionHandler_mapsConnectionToNoConnection() {
		val throwable = IllegalStateException("network is unreachable")

		val actual = UpdateQuartersExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = false)
		).parseException(throwable)

		val error = assertIs<UpdateQuartersUseCaseError.NoConnection>(actual)
		assertEquals(false, error.isNetworkAvailable)
	}
}
