package com.gdavidpb.tuindice.record.presentation.action

import app.cash.turbine.test
import com.gdavidpb.tuindice.record.domain.usecase.GetSelectedQuarterIdUseCase
import com.gdavidpb.tuindice.record.domain.usecase.GetRecordViewModeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetRecordViewModeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSelectedQuarterIdUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSubjectGradeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.SetSubjectGradeExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.UpdateQuartersExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.validator.SetSubjectGradeParamsValidator
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_QUARTER
import com.gdavidpb.tuindice.record.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.record.testing.RecordingQuarterRepository
import com.gdavidpb.tuindice.record.testing.RecordingQuarterSelectionRepository
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
		val quarterSelectionRepository = RecordingQuarterSelectionRepository()
		val processor = ObserveQuartersActionProcessor(
			observeQuartersUseCase = ObserveQuartersUseCase(
				quarterRepository = RecordingQuarterRepository(
					quarters = flowOf(listOf(DEFAULT_RECORD_QUARTER))
				),
				reportingRepository = RecordingReportingRepository()
			),
			getRecordViewModeUseCase = GetRecordViewModeUseCase(
				quarterSelectionRepository = quarterSelectionRepository,
				reportingRepository = RecordingReportingRepository()
			),
			getSelectedQuarterIdUseCase = GetSelectedQuarterIdUseCase(
				quarterSelectionRepository = quarterSelectionRepository,
				reportingRepository = RecordingReportingRepository()
			),
			setSelectedQuarterIdUseCase = SetSelectedQuarterIdUseCase(
				quarterSelectionRepository = quarterSelectionRepository,
				reportingRepository = RecordingReportingRepository()
			)
		)
		val effects = mutableListOf<Record.Effect>()

		processor.process(
			action = Record.Action.ObserveQuarters,
			sideEffect = effects::add
		).test {
			val content = assertIs<Record.State.Content>(awaitItem()(Record.State.Loading))
			assertEquals(listOf(DEFAULT_RECORD_QUARTER), content.quarters)
			assertEquals(DEFAULT_RECORD_QUARTER.id, content.selectedQuarterId)

			awaitComplete()
		}

		assertTrue(effects.isEmpty())
	}

	@Test
	fun observeQuartersActionProcessor_keepsLoading_whenInitialSnapshotIsEmpty() = runTest {
		val quarterSelectionRepository = RecordingQuarterSelectionRepository()
		val processor = ObserveQuartersActionProcessor(
			observeQuartersUseCase = ObserveQuartersUseCase(
				quarterRepository = RecordingQuarterRepository(
					quarters = flowOf(emptyList())
				),
				reportingRepository = RecordingReportingRepository()
			),
			getRecordViewModeUseCase = GetRecordViewModeUseCase(
				quarterSelectionRepository = quarterSelectionRepository,
				reportingRepository = RecordingReportingRepository()
			),
			getSelectedQuarterIdUseCase = GetSelectedQuarterIdUseCase(
				quarterSelectionRepository = quarterSelectionRepository,
				reportingRepository = RecordingReportingRepository()
			),
			setSelectedQuarterIdUseCase = SetSelectedQuarterIdUseCase(
				quarterSelectionRepository = quarterSelectionRepository,
				reportingRepository = RecordingReportingRepository()
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
	fun observeQuartersActionProcessor_restoresSharedQuarterFromOtherViewSelection() = runTest {
		val officialQuarter = DEFAULT_RECORD_QUARTER.copy(
			id = "quarter-official",
			name = "2025-3",
			startDate = DEFAULT_RECORD_QUARTER.startDate - 100_000L,
			endDate = DEFAULT_RECORD_QUARTER.endDate - 100_000L,
			isCurrent = false,
			isReadOnly = true,
			subjects = listOf(
				DEFAULT_RECORD_QUARTER.subjects.single().copy(
					id = "subject-official",
					quarterId = "quarter-official"
				)
			)
		)
		val quarterSelectionRepository = RecordingQuarterSelectionRepository(
			initialSelectedQuarterId = officialQuarter.id,
			initialViewMode = RecordViewMode.Official
		)
		val processor = ObserveQuartersActionProcessor(
			observeQuartersUseCase = ObserveQuartersUseCase(
				quarterRepository = RecordingQuarterRepository(
					quarters = flowOf(listOf(DEFAULT_RECORD_QUARTER, officialQuarter))
				),
				reportingRepository = RecordingReportingRepository()
			),
			getRecordViewModeUseCase = GetRecordViewModeUseCase(
				quarterSelectionRepository = quarterSelectionRepository,
				reportingRepository = RecordingReportingRepository()
			),
			getSelectedQuarterIdUseCase = GetSelectedQuarterIdUseCase(
				quarterSelectionRepository = quarterSelectionRepository,
				reportingRepository = RecordingReportingRepository()
			),
			setSelectedQuarterIdUseCase = SetSelectedQuarterIdUseCase(
				quarterSelectionRepository = quarterSelectionRepository,
				reportingRepository = RecordingReportingRepository()
			)
		)

		processor.process(
			action = Record.Action.ObserveQuarters,
			sideEffect = {}
		).test {
			val content = assertIs<Record.State.Content>(awaitItem()(Record.State.Loading))
			assertEquals(RecordViewMode.Official, content.viewMode)
			assertEquals(officialQuarter.id, content.selectedQuarterId)
			awaitComplete()
		}

		assertEquals(
			officialQuarter.id,
			quarterSelectionRepository.getSelectedQuarterId(RecordViewMode.Official)
		)
	}

	@Test
	fun setRecordViewModeActionProcessor_keepsSharedQuarterSelectedAcrossViews() = runTest {
		val officialQuarter = DEFAULT_RECORD_QUARTER.copy(
			id = "quarter-official",
			name = "2025-3",
			startDate = DEFAULT_RECORD_QUARTER.startDate - 100_000L,
			endDate = DEFAULT_RECORD_QUARTER.endDate - 100_000L,
			isCurrent = false,
			isReadOnly = true,
			subjects = listOf(
				DEFAULT_RECORD_QUARTER.subjects.single().copy(
					id = "subject-official",
					quarterId = "quarter-official"
				)
			)
		)
		val quarterSelectionRepository = RecordingQuarterSelectionRepository(
			initialSelectedQuarterId = officialQuarter.id
		)
		val processor = SetRecordViewModeActionProcessor(
			setRecordViewModeUseCase = SetRecordViewModeUseCase(
				quarterSelectionRepository = quarterSelectionRepository,
				reportingRepository = RecordingReportingRepository()
			),
			getSelectedQuarterIdUseCase = GetSelectedQuarterIdUseCase(
				quarterSelectionRepository = quarterSelectionRepository,
				reportingRepository = RecordingReportingRepository()
			),
			setSelectedQuarterIdUseCase = SetSelectedQuarterIdUseCase(
				quarterSelectionRepository = quarterSelectionRepository,
				reportingRepository = RecordingReportingRepository()
			)
		)
		val initialState = Record.State.Content(
			quarters = listOf(DEFAULT_RECORD_QUARTER, officialQuarter),
			viewMode = RecordViewMode.Simulation,
			selectedQuarterId = officialQuarter.id
		)

		processor.process(
			action = Record.Action.SetViewMode(RecordViewMode.Official),
			sideEffect = {}
		).test {
			val content = assertIs<Record.State.Content>(awaitItem()(initialState))
			assertEquals(RecordViewMode.Official, content.viewMode)
			assertEquals(officialQuarter.id, content.selectedQuarterId)
			awaitComplete()
		}

		assertEquals(
			officialQuarter.id,
			quarterSelectionRepository.getSelectedQuarterId(RecordViewMode.Official)
		)
	}

	@Test
	fun selectQuarterActionProcessor_syncsSharedQuarterSelectionAcrossViews() = runTest {
		val officialQuarter = DEFAULT_RECORD_QUARTER.copy(
			id = "quarter-official",
			name = "2025-3",
			startDate = DEFAULT_RECORD_QUARTER.startDate - 100_000L,
			endDate = DEFAULT_RECORD_QUARTER.endDate - 100_000L,
			isCurrent = false,
			isReadOnly = true,
			subjects = listOf(
				DEFAULT_RECORD_QUARTER.subjects.single().copy(
					id = "subject-official",
					quarterId = "quarter-official"
				)
			)
		)
		val quarterSelectionRepository = RecordingQuarterSelectionRepository()
		val processor = SelectQuarterActionProcessor(
			setSelectedQuarterIdUseCase = SetSelectedQuarterIdUseCase(
				quarterSelectionRepository = quarterSelectionRepository,
				reportingRepository = RecordingReportingRepository()
			)
		)
		val initialState = Record.State.Content(
			quarters = listOf(DEFAULT_RECORD_QUARTER, officialQuarter),
			viewMode = RecordViewMode.Simulation,
			selectedQuarterId = DEFAULT_RECORD_QUARTER.id
		)

		processor.process(
			action = Record.Action.SelectQuarter(officialQuarter.id),
			sideEffect = {}
		).test {
			val content = assertIs<Record.State.Content>(awaitItem()(initialState))
			assertEquals(officialQuarter.id, content.selectedQuarterId)
			awaitComplete()
		}

		assertEquals(
			officialQuarter.id,
			quarterSelectionRepository.getSelectedQuarterId(RecordViewMode.Simulation)
		)
		assertEquals(
			officialQuarter.id,
			quarterSelectionRepository.getSelectedQuarterId(RecordViewMode.Official)
		)
	}

	@Test
	fun refreshQuartersActionProcessor_setsLoadingFromFailedState() = runTest {
		val processor = RefreshQuartersActionProcessor(
			updateQuartersUseCase = UpdateQuartersUseCase(
				quarterRepository = RecordingQuarterRepository(),
				reportingRepository = RecordingReportingRepository(),
				exceptionHandler = UpdateQuartersExceptionHandler(
					networkRepository = FakeNetworkRepository(isAvailable = true)
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
				reportingRepository = RecordingReportingRepository(),
				exceptionHandler = UpdateQuartersExceptionHandler(
					networkRepository = FakeNetworkRepository(isAvailable = false)
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
				reportingRepository = RecordingReportingRepository(),
				paramsValidator = SetSubjectGradeParamsValidator(),
				exceptionHandler = SetSubjectGradeExceptionHandler()
			)
		)
		val initialState = Record.State.Content(
			quarters = listOf(DEFAULT_RECORD_QUARTER),
			viewMode = RecordViewMode.Simulation,
			selectedQuarterId = DEFAULT_RECORD_QUARTER.id
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
				reportingRepository = RecordingReportingRepository(),
				paramsValidator = SetSubjectGradeParamsValidator(),
				exceptionHandler = SetSubjectGradeExceptionHandler()
			)
		)
		val initialState = Record.State.Content(
			quarters = listOf(DEFAULT_RECORD_QUARTER),
			viewMode = RecordViewMode.Simulation,
			selectedQuarterId = DEFAULT_RECORD_QUARTER.id
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
