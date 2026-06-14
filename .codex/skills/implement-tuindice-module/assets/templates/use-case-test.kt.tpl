package $PACKAGE.domain.usecase

import $PACKAGE.domain.usecase.exceptionhandler.$UPDATE_EXCEPTION_HANDLER_NAME
import $PACKAGE.testing.$RECORDING_REPOSITORY_NAME
import app.cash.turbine.test
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class $USE_CASE_TEST_CLASS_NAME {
	@Test
	fun observeUseCase_emitsLoadingThenData_fromRepositoryFlow() = runTest {
		val useCase = $OBSERVE_USE_CASE_NAME(
			$REPOSITORY_PARAM_NAME = $RECORDING_REPOSITORY_NAME(message = "observed"),
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertEquals("observed", awaitLoadingThenData(this))
			awaitComplete()
		}
	}

	@Test
	fun updateUseCase_emitsLoadingThenData_andDelegatesUpdate() = runTest {
		val repository = $RECORDING_REPOSITORY_NAME()
		val useCase = $UPDATE_USE_CASE_NAME(
			$REPOSITORY_PARAM_NAME = repository,
			reportingRepository = RecordingReportingRepository(),
			exceptionHandler = $UPDATE_EXCEPTION_HANDLER_NAME(
				networkRepository = FakeNetworkRepository(isAvailable = true),
				reportingRepository = RecordingReportingRepository()
			)
		)

		useCase.execute(Unit).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(1, repository.updateCalls.value)
	}
}
