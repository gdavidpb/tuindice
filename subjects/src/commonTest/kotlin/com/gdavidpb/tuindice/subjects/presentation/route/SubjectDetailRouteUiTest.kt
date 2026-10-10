package com.gdavidpb.tuindice.subjects.presentation.route

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult
import com.gdavidpb.tuindice.subjects.domain.usecase.LoadSubjectDetailUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.RefreshSubjectDetailUseCase
import com.gdavidpb.tuindice.subjects.presentation.machine.SubjectDetailMachine
import com.gdavidpb.tuindice.subjects.presentation.viewmodel.SubjectDetailViewModel
import com.gdavidpb.tuindice.subjects.testing.RecordingSubjectStatsRepository
import com.gdavidpb.tuindice.subjects.testing.readySubjectDetail
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SubjectDetailRouteUiTest {
	@Test
	fun when_routeStartsWithFreshDetail_then_loadsSubjectAndRendersContent() = runTuIndiceUiTest {
		val repository = RecordingSubjectStatsRepository(
			freshResult = readySubjectDetail(subjectCode = "MAT101")
		)
		val viewModel = createViewModel(repository)

		setTuIndiceTestContent {
			SubjectDetailRoute(
				subjectCode = "MAT101",
				viewModel = viewModel,
				onDismissRequest = {}
			)
		}

		waitUntilTagExists(SubjectsUiTags.Content)

		onNodeWithText("Calculo I").assertIsDisplayed()
		onNodeWithText("MAT101").assertIsDisplayed()
		onNodeWithText("5 UC").assertIsDisplayed()
		assertNodeHidden(SubjectsUiTags.CareerTab)
		assertEquals(listOf("MAT101"), repository.freshCalls)
		assertEquals(emptyList(), repository.refreshCalls)
	}

	@Test
	fun when_remoteRefreshIsPending_then_rendersLoadingUntilItCompletes() = runTuIndiceUiTest {
		val repository = RecordingSubjectStatsRepository(
			results = listOf(readySubjectDetail(subjectCode = "MAT101"))
		)
		repository.blockRefresh = true
		val viewModel = createViewModel(repository)

		setTuIndiceTestContent {
			SubjectDetailRoute(
				subjectCode = "MAT101",
				viewModel = viewModel,
				onDismissRequest = {}
			)
		}

		waitUntilTagExists(SubjectsUiTags.Loading)
		onNodeWithTag(SubjectsUiTags.LoadingTitle)
			.assertTextEquals("Calculando estadísticas")
		onNodeWithTag(SubjectsUiTags.LoadingMessage)
			.assertTextEquals("Estamos cruzando notas, intentos y tendencias de esta materia...")

		repository.releaseRefresh()

		waitUntilTagExists(SubjectsUiTags.Content)
		assertNodeHidden(SubjectsUiTags.Loading)
		assertEquals(listOf("MAT101"), repository.refreshCalls)
	}

	@Test
	fun when_loadFails_then_rendersFailedMessageAndRetryRecovers() = runTuIndiceUiTest {
		val repository = RecordingSubjectStatsRepository(
			results = listOf(
				IllegalStateException("boom"),
				readySubjectDetail(subjectCode = "MAT101")
			)
		)
		val viewModel = createViewModel(repository)

		setTuIndiceTestContent {
			SubjectDetailRoute(
				subjectCode = "MAT101",
				viewModel = viewModel,
				onDismissRequest = {}
			)
		}

		waitUntilTagExists(SubjectsUiTags.Failed)
		onNodeWithText("No pudimos calcular estadísticas").assertIsDisplayed()
		onNodeWithText("Intenta de nuevo para consultar los datos de MAT101.").assertIsDisplayed()
		onNodeWithTag(SubjectsUiTags.Retry)
			.assertTextEquals("Reintentar")
			.performClick()

		waitUntilTagExists(SubjectsUiTags.Content)
		assertNodeHidden(SubjectsUiTags.Failed)
		assertEquals(listOf("MAT101", "MAT101"), repository.refreshCalls)
	}

	@Test
	fun when_subjectIsUnavailable_then_closeActionInvokesDismissRequest() = runTuIndiceUiTest {
		val repository = RecordingSubjectStatsRepository(
			results = listOf(
				SubjectDetailResult.Unavailable(subjectCode = "MAT404", expiresAt = 123L)
			)
		)
		val viewModel = createViewModel(repository)
		var dismissRequests = 0

		setTuIndiceTestContent {
			SubjectDetailRoute(
				subjectCode = "MAT404",
				viewModel = viewModel,
				onDismissRequest = { dismissRequests++ }
			)
		}

		waitUntilTagExists(SubjectsUiTags.Unavailable)
		onNodeWithText("Sin datos suficientes").assertIsDisplayed()
		assertNodeHidden(SubjectsUiTags.Retry)
		onNodeWithText("Cerrar").performClick()

		assertEquals(1, dismissRequests)
		assertEquals(listOf("MAT404"), repository.freshCalls)
	}

	private fun ComposeUiTest.waitUntilTagExists(tag: String) {
		// The view model resolves its first strings on a background dispatcher; a cold
		// compose-resources load on the simulator can take several seconds.
		waitUntil(timeoutMillis = 15_000) {
			onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
		}
	}

	private fun createViewModel(
		repository: RecordingSubjectStatsRepository
	): SubjectDetailViewModel {
		val reportingRepository = RecordingReportingRepository()

		return SubjectDetailViewModel(
			screenMachine = SubjectDetailMachine(
				loadSubjectDetailUseCase = LoadSubjectDetailUseCase(
					subjectStatsRepository = repository,
					reportingRepository = reportingRepository
				),
				refreshSubjectDetailUseCase = RefreshSubjectDetailUseCase(
					subjectStatsRepository = repository,
					reportingRepository = reportingRepository
				)
			),
			eventPublisher = NoOpEventPublisher
		)
	}
}
