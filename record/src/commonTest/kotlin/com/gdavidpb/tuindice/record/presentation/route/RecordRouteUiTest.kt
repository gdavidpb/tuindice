package com.gdavidpb.tuindice.record.presentation.route

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import androidx.compose.ui.semantics.SemanticsActions
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.usecase.ObserveQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSubjectGradeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.SetSubjectGradeExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.UpdateQuartersExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.validator.SetSubjectGradeParamsValidator
import com.gdavidpb.tuindice.record.presentation.action.ObserveQuartersActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.RefreshQuartersActionProcessor
import com.gdavidpb.tuindice.record.presentation.action.SetSubjectGradeActionProcessor
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_QUARTER
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_SUBJECT
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.record.testing.RecordingQuarterRepository
import com.gdavidpb.tuindice.record.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class RecordRouteUiTest {
	@Test
	fun when_setSubjectGradeIsInvalid_then_routeForwardsSnackBarEffect() = runTuIndiceUiTest {
		val viewModel = createRecordViewModel()
		val snackBars = mutableListOf<SnackBarMessage>()
		var navigatedToUpdatePassword = false

		setTuIndiceTestContent {
			RecordRoute(
				onNavigateToUpdatePassword = {
					navigatedToUpdatePassword = true
				},
				onViewStateChanged = {},
				showSnackBar = { message ->
					snackBars += message
				},
				viewModel = viewModel
			)
		}

		runOnIdle {
			viewModel.updateSubjectAction(
				quarterId = "quarter-1",
				subjectId = "subject-1",
				grade = -1,
				commit = false
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			snackBars.isNotEmpty()
		}

		assertEquals("¡Ha ocurrido un error!", snackBars.first().message)
		assertFalse(navigatedToUpdatePassword)
	}

	@Test
	fun when_initialRefreshFailsWithConflict_then_routeShowsGenericError() = runTuIndiceUiTest {
		val viewModel = createRecordViewModel(
			quarterRepository = RecordingQuarterRepository(
				quarters = flowOf(emptyList()),
				updateThrowable = clientRequestException(
					statusCode = HttpStatusCode.Conflict,
					path = "/quarters/v1"
				)
			)
		)
		val snackBars = mutableListOf<SnackBarMessage>()
		var navigatedToUpdatePassword = false

		setTuIndiceTestContent {
			RecordRoute(
				onNavigateToUpdatePassword = {
					navigatedToUpdatePassword = true
				},
				onViewStateChanged = {},
				showSnackBar = { message ->
					snackBars += message
				},
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			snackBars.isNotEmpty()
		}

		assertFalse(navigatedToUpdatePassword)
		assertEquals("¡Ha ocurrido un error!", snackBars.first().message)
	}

	@Test
	fun when_initialRefreshFailsWithGenericError_then_routeShowsSnackBarWithoutNavigation() = runTuIndiceUiTest {
		val viewModel = createRecordViewModel(
			quarterRepository = RecordingQuarterRepository(
				quarters = flowOf(emptyList()),
				updateThrowable = IllegalStateException("boom")
			)
		)
		val snackBars = mutableListOf<SnackBarMessage>()
		var navigatedToUpdatePassword = false

		setTuIndiceTestContent {
			RecordRoute(
				onNavigateToUpdatePassword = {
					navigatedToUpdatePassword = true
				},
				onViewStateChanged = {},
				showSnackBar = { message ->
					snackBars += message
				},
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			snackBars.isNotEmpty()
		}

		assertEquals("¡Ha ocurrido un error!", snackBars.first().message)
		assertFalse(navigatedToUpdatePassword)
	}

	@Test
	fun when_setSubjectGradeIsValid_then_routeDoesNotEmitSnackBarOrNavigationEffects() = runTuIndiceUiTest {
		val quarterRepository = RecordingQuarterRepository()
		val viewModel = createRecordViewModel(quarterRepository = quarterRepository)
		val snackBars = mutableListOf<SnackBarMessage>()
		var navigatedToUpdatePassword = false

		setTuIndiceTestContent {
			RecordRoute(
				onNavigateToUpdatePassword = {
					navigatedToUpdatePassword = true
				},
				onViewStateChanged = {},
				showSnackBar = { message ->
					snackBars += message
				},
				viewModel = viewModel
			)
		}

		runOnIdle {
			viewModel.updateSubjectAction(
				quarterId = "quarter-1",
				subjectId = "subject-1",
				grade = 4,
				commit = true
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			quarterRepository.setGradeCalls.value.isNotEmpty()
		}

		assertTrue(snackBars.isEmpty())
		assertFalse(navigatedToUpdatePassword)
	}

	@Test
	fun when_subjectGradeSliderMoved_then_routeUpdatesSubjectGradeInRepository() = runTuIndiceUiTest {
		val quarterRepository = RecordingQuarterRepository(
			quarters = flow {
				emit(
					listOf(
						DEFAULT_RECORD_QUARTER.copy(
							grade = 4.0,
							gradeSum = 4.0,
							subjects = listOf(
								DEFAULT_RECORD_SUBJECT.copy(grade = 4)
							)
						)
					)
				)
			}
		)
		val viewModel = createRecordViewModel(quarterRepository = quarterRepository)
		val snackBars = mutableListOf<SnackBarMessage>()
		var navigatedToUpdatePassword = false

		setTuIndiceTestContent {
			RecordRoute(
				onNavigateToUpdatePassword = {
					navigatedToUpdatePassword = true
				},
				onViewStateChanged = {},
				showSnackBar = { message ->
					snackBars += message
				},
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(RecordUiTags.subjectGradeSlider("subject-1"))
				.fetchSemanticsNodes().isNotEmpty()
		}

		onNodeWithTag(RecordUiTags.subjectGradeSlider("subject-1"))
			.performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
				assertTrue(setProgress(5f))
			}

		waitUntil(timeoutMillis = 2_000) {
			quarterRepository.setGradeCalls.value.isNotEmpty()
		}

		assertTrue(
			quarterRepository.setGradeCalls.value.any { setGradeCall ->
				setGradeCall.id == "subject-1" &&
					setGradeCall.quarterId == "quarter-1" &&
					setGradeCall.grade == 5
			}
		)
		assertTrue(snackBars.isEmpty())
		assertFalse(navigatedToUpdatePassword)
	}

	@Test
	fun when_retryTappedAfterFailedLoad_then_routeRequestsLoadAgain() = runTuIndiceUiTest {
		val quarterRepository = RecordingQuarterRepository(
			quarters = flowOf(emptyList()),
			updateThrowable = IllegalStateException("boom")
		)
		val viewModel = createRecordViewModel(quarterRepository = quarterRepository)
		val snackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			RecordRoute(
				onNavigateToUpdatePassword = {},
				onViewStateChanged = {},
				showSnackBar = { message ->
					snackBars += message
				},
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			quarterRepository.updateQuartersCalls.value > 0 && snackBars.isNotEmpty()
		}

		onNodeWithTag(BaseUiTags.ErrorViewRetryButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			quarterRepository.updateQuartersCalls.value >= 2
		}

		assertTrue(quarterRepository.updateQuartersCalls.value >= 2)
	}

	@Test
	fun when_selectedQuarterIsCurrent_then_topBarActionIsVisibleOtherwiseHidden() = runTuIndiceUiTest {
		val olderQuarter = DEFAULT_RECORD_QUARTER.copy(
			id = "quarter-2",
			name = "2025-3",
			startDate = DEFAULT_RECORD_QUARTER.startDate - 100_000L,
			endDate = DEFAULT_RECORD_QUARTER.endDate - 100_000L,
			isCurrent = false,
			subjects = listOf(
				DEFAULT_RECORD_SUBJECT.copy(
					id = "subject-2",
					quarterId = "quarter-2"
				)
			)
		)
		val viewModel = createRecordViewModel(
			quarterRepository = RecordingQuarterRepository(
				quarters = flowOf(
					listOf(
						DEFAULT_RECORD_QUARTER,
						olderQuarter
					)
				)
			)
		)
		val viewStates = mutableListOf<ViewState>()

		setTuIndiceTestContent {
			RecordRoute(
				onNavigateToUpdatePassword = {},
				onViewStateChanged = { state ->
					viewStates += state
				},
				showSnackBar = {},
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			viewStates.lastOrNull()?.topBarConfig == TopBarConfig.Record
		}

		onNodeWithTag(RecordUiTags.quarterChip("quarter-2")).performClick()

		waitUntil(timeoutMillis = 2_000) {
			viewStates.lastOrNull()?.topBarConfig == null
		}

		onNodeWithTag(RecordUiTags.quarterChip("quarter-1")).performClick()

		waitUntil(timeoutMillis = 2_000) {
			viewStates.lastOrNull()?.topBarConfig == TopBarConfig.Record
		}
	}

	private fun createRecordViewModel(
		quarterRepository: QuarterRepository = RecordingQuarterRepository()
	): RecordViewModel {

		return RecordViewModel(
			observeQuartersActionProcessor = ObserveQuartersActionProcessor(
				observeQuartersUseCase = ObserveQuartersUseCase(
					quarterRepository = quarterRepository,
				)
			),
			refreshQuartersActionProcessor = RefreshQuartersActionProcessor(
				updateQuartersUseCase = UpdateQuartersUseCase(
					quarterRepository = quarterRepository,
					exceptionHandler = UpdateQuartersExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true),
						reportingRepository = RecordingReportingRepository()
					)
				)
			),
			setSubjectGradeActionProcessor = SetSubjectGradeActionProcessor(
				setSubjectGradeUseCase = SetSubjectGradeUseCase(
					quarterRepository = quarterRepository,
					paramsValidator = SetSubjectGradeParamsValidator(),
					exceptionHandler = SetSubjectGradeExceptionHandler(
						reportingRepository = RecordingReportingRepository()
					)
				)
			)
		)
	}
}
