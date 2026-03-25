package com.gdavidpb.tuindice.record.presentation.action

import app.cash.turbine.test
import com.gdavidpb.tuindice.record.domain.usecase.ObserveQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSubjectGradeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.SetSubjectGradeExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.UpdateQuartersExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.validator.SetSubjectGradeParamsValidator
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_QUARTER
import com.gdavidpb.tuindice.record.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.record.testing.RecordingQuarterRepository
import com.gdavidpb.tuindice.record.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.snack_default_error
import tuindice.record.generated.resources.snack_network_unavailable
import tuindice.record.generated.resources.snack_record_not_found
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RecordActionProcessorContractTest {
	@Test
	fun observeQuartersActionProcessor_reducesStateToContent() = runTest {
		val processor = ObserveQuartersActionProcessor(
			observeQuartersUseCase = ObserveQuartersUseCase(
				quarterRepository = RecordingQuarterRepository(
					quarters = flowOf(listOf(DEFAULT_RECORD_QUARTER))
				)
			)
		)
		val effects = mutableListOf<Record.Effect>()

		processor.process(
			action = Record.Action.ObserveQuarters,
			sideEffect = effects::add
		).test {
			val content = assertIs<Record.State.Content>(awaitItem()(Record.State.Loading))
			assertEquals(listOf(DEFAULT_RECORD_QUARTER), content.quarters)

			awaitComplete()
		}

		assertTrue(effects.isEmpty())
	}

	@Test
	fun observeQuartersActionProcessor_keepsLoading_whenInitialSnapshotIsEmpty() = runTest {
		val processor = ObserveQuartersActionProcessor(
			observeQuartersUseCase = ObserveQuartersUseCase(
				quarterRepository = RecordingQuarterRepository(
					quarters = flowOf(emptyList())
				)
			)
		)

		processor.process(
			action = Record.Action.ObserveQuarters,
			sideEffect = {}
		).test {
			assertEquals(Record.State.Loading, awaitItem()(Record.State.Loading))
			awaitComplete()
		}
	}

	@Test
	fun refreshQuartersActionProcessor_setsLoadingFromFailedState() = runTest {
		val processor = RefreshQuartersActionProcessor(
			updateQuartersUseCase = UpdateQuartersUseCase(
				quarterRepository = RecordingQuarterRepository(),
				exceptionHandler = UpdateQuartersExceptionHandler(
					networkRepository = FakeNetworkRepository(isAvailable = true),
					reportingRepository = RecordingReportingRepository()
				)
			)
		)

		processor.process(
			action = Record.Action.RefreshQuarters,
			sideEffect = {}
		).test {
			assertEquals(Record.State.Loading, awaitItem()(Record.State.Failed))
			awaitComplete()
		}
	}

	@Test
	fun refreshQuartersActionProcessor_showsNetworkMessage_andFailsWithoutCachedData() = runTest {
		val processor = RefreshQuartersActionProcessor(
			updateQuartersUseCase = UpdateQuartersUseCase(
				quarterRepository = RecordingQuarterRepository(
					updateThrowable = IllegalStateException("network is unreachable")
				),
				exceptionHandler = UpdateQuartersExceptionHandler(
					networkRepository = FakeNetworkRepository(isAvailable = false),
					reportingRepository = RecordingReportingRepository()
				)
			)
		)
		val effects = mutableListOf<Record.Effect>()

		processor.process(
			action = Record.Action.RefreshQuarters,
			sideEffect = effects::add
		).test {
			assertEquals(Record.State.Loading, awaitItem()(Record.State.Empty))
			assertEquals(Record.State.Failed, awaitItem()(Record.State.Loading))
			awaitComplete()
		}

		val effect = assertIs<Record.Effect.ShowSnackBar>(effects.single())
		assertEquals(getString(Res.string.snack_network_unavailable), effect.message)
	}

	@Test
	fun setSubjectGradeActionProcessor_preservesState_andShowsErrorOnValidationFailure() = runTest {
		val processor = SetSubjectGradeActionProcessor(
			setSubjectGradeUseCase = SetSubjectGradeUseCase(
				quarterRepository = RecordingQuarterRepository(),
				paramsValidator = SetSubjectGradeParamsValidator(),
				exceptionHandler = SetSubjectGradeExceptionHandler(
					reportingRepository = RecordingReportingRepository()
				)
			)
		)
		val initialState = Record.State.Content(
			quarters = listOf(DEFAULT_RECORD_QUARTER)
		)
		val effects = mutableListOf<Record.Effect>()

		processor.process(
			action = Record.Action.SetSubjectGrade(
				quarterId = DEFAULT_RECORD_QUARTER.id,
				subjectId = DEFAULT_RECORD_QUARTER.subjects.single().id,
				grade = -1,
				commit = false
			),
			sideEffect = effects::add
		).test {
			assertEquals(initialState, awaitItem()(initialState))
			assertEquals(initialState, awaitItem()(initialState))
			awaitComplete()
		}

		val effect = assertIs<Record.Effect.ShowSnackBar>(effects.single())
		assertEquals(getString(Res.string.snack_default_error), effect.message)
	}

	@Test
	fun setSubjectGradeActionProcessor_showsRecordChangedMessage_whenBackendRejectsCommit() = runTest {
		val processor = SetSubjectGradeActionProcessor(
			setSubjectGradeUseCase = SetSubjectGradeUseCase(
				quarterRepository = RecordingQuarterRepository(
					setSubjectGradeThrowable = clientRequestException(
						HttpStatusCode.PreconditionFailed,
						path = "/quarters/v1/qid/subjects/sid"
					)
				),
				paramsValidator = SetSubjectGradeParamsValidator(),
				exceptionHandler = SetSubjectGradeExceptionHandler(
					reportingRepository = RecordingReportingRepository()
				)
			)
		)
		val initialState = Record.State.Content(
			quarters = listOf(DEFAULT_RECORD_QUARTER)
		)
		val effects = mutableListOf<Record.Effect>()

		processor.process(
			action = Record.Action.SetSubjectGrade(
				quarterId = DEFAULT_RECORD_QUARTER.id,
				subjectId = DEFAULT_RECORD_QUARTER.subjects.single().id,
				grade = 4,
				commit = true
			),
			sideEffect = effects::add
		).test {
			assertEquals(initialState, awaitItem()(initialState))
			assertEquals(initialState, awaitItem()(initialState))
			awaitComplete()
		}

		val effect = assertIs<Record.Effect.ShowSnackBar>(effects.single())
		assertEquals(getString(Res.string.snack_record_not_found), effect.message)
	}
}
