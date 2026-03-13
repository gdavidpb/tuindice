package com.gdavidpb.tuindice.record.presentation.route

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.semantics.SemanticsActions
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSubjectGradeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.GetQuartersExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.SetSubjectGradeExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.validator.SetSubjectGradeParamsValidator
import com.gdavidpb.tuindice.record.presentation.action.LoadQuartersActionProcessor
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
	fun when_initialLoadFailsWithConflict_then_routeShowsGenericError() = runTuIndiceUiTest {
		val viewModel = createRecordViewModel(
			quarterRepository = RecordingQuarterRepository(
				quarters = flow {
					throw clientRequestException(
						statusCode = HttpStatusCode.Conflict,
						path = "/quarters/v1"
					)
				}
			)
		)
		val snackBars = mutableListOf<SnackBarMessage>()
		var navigatedToUpdatePassword = false

		setTuIndiceTestContent {
			RecordRoute(
				onNavigateToUpdatePassword = {
					navigatedToUpdatePassword = true
				},
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
	fun when_initialLoadFailsWithGenericError_then_routeShowsSnackBarWithoutNavigation() = runTuIndiceUiTest {
		val viewModel = createRecordViewModel(
			quarterRepository = RecordingQuarterRepository(
				quarters = flow {
					throw IllegalStateException("boom")
				}
			)
		)
		val snackBars = mutableListOf<SnackBarMessage>()
		var navigatedToUpdatePassword = false

		setTuIndiceTestContent {
			RecordRoute(
				onNavigateToUpdatePassword = {
					navigatedToUpdatePassword = true
				},
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
		var getQuartersFlowCalls = 0
		val quarterRepository = RecordingQuarterRepository(
			quarters = flow {
				getQuartersFlowCalls++
				throw IllegalStateException("boom")
			}
		)
		val viewModel = createRecordViewModel(quarterRepository = quarterRepository)
		val snackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			RecordRoute(
				onNavigateToUpdatePassword = {},
				showSnackBar = { message ->
					snackBars += message
				},
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			getQuartersFlowCalls > 0 && snackBars.isNotEmpty()
		}

		onNodeWithTag(BaseUiTags.ErrorViewRetryButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			getQuartersFlowCalls >= 2
		}

		assertTrue(getQuartersFlowCalls >= 2)
	}

	private fun createRecordViewModel(
		quarterRepository: QuarterRepository = RecordingQuarterRepository()
	): RecordViewModel {

		return RecordViewModel(
			loadQuartersActionProcessor = LoadQuartersActionProcessor(
				getQuartersUseCase = GetQuartersUseCase(
					quarterRepository = quarterRepository,
					exceptionHandler = GetQuartersExceptionHandler(
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
