package com.gdavidpb.tuindice.subjects.presentation.route

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.subjects.domain.usecase.ObserveSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.RefreshSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.presentation.machine.SubjectSearchDraft
import com.gdavidpb.tuindice.subjects.presentation.machine.SubjectSearchMachine
import com.gdavidpb.tuindice.subjects.presentation.viewmodel.SubjectSearchViewModel
import com.gdavidpb.tuindice.subjects.testing.ControllableSubjectCatalogRepository
import com.gdavidpb.tuindice.subjects.testing.subjectSearchResult
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SubjectSearchRouteUiTest {
	@Test
	fun when_exampleChipTapped_then_updatesQueryAndRendersLocalResults() = runTuIndiceUiTest {
		val repository = ControllableSubjectCatalogRepository(
			localResults = listOf(subjectSearchResult(subjectCode = "MA1111"))
		)
		val viewModel = createViewModel(repository)

		setTuIndiceTestContent {
			SubjectSearchRoute(
				viewModel = viewModel,
				onSubjectClick = {}
			)
		}

		assertNodeVisible(SubjectsUiTags.SearchGuidance)
		onNodeWithTag(SubjectsUiTags.searchExample(0)).performClick()

		waitUntilTagExists(SubjectsUiTags.searchResult("MA1111"))

		assertNodeHidden(SubjectsUiTags.SearchGuidance)
		onNodeWithTag(SubjectsUiTags.SearchTextField).assertTextContains("MA1111")
		onNodeWithText("INT. A LAS MICROONDAS Y SUS APLICACIONES").assertIsDisplayed()
		assertTrue("MA1111" in repository.observeCalls)
	}

	@Test
	fun when_userTypesQuery_then_viewModelReceivesItAndRendersResults() = runTuIndiceUiTest {
		val repository = ControllableSubjectCatalogRepository(
			localResults = listOf(subjectSearchResult(subjectCode = "EC3423"))
		)
		val viewModel = createViewModel(repository)

		setTuIndiceTestContent {
			SubjectSearchRoute(
				viewModel = viewModel,
				onSubjectClick = {}
			)
		}

		onNodeWithTag(SubjectsUiTags.SearchTextField).performTextInput("micro")

		waitUntilTagExists(SubjectsUiTags.searchResult("EC3423"))

		assertEquals("micro", viewModel.state.value.query)
		assertTrue("micro" in repository.observeCalls)
	}

	@Test
	fun when_resultTapped_then_invokesSubjectClickWithItsCode() = runTuIndiceUiTest {
		val repository = ControllableSubjectCatalogRepository(
			localResults = listOf(subjectSearchResult(subjectCode = "MA1111"))
		)
		val viewModel = createViewModel(repository)
		val clickedSubjectCodes = mutableListOf<String>()

		setTuIndiceTestContent {
			SubjectSearchRoute(
				viewModel = viewModel,
				onSubjectClick = { subjectCode -> clickedSubjectCodes += subjectCode }
			)
		}

		onNodeWithTag(SubjectsUiTags.searchExample(0)).performClick()
		waitUntilTagExists(SubjectsUiTags.searchResult("MA1111"))
		onNodeWithTag(SubjectsUiTags.searchResult("MA1111")).performClick()

		assertEquals(listOf("MA1111"), clickedSubjectCodes)
	}

	@Test
	fun when_clearTapped_then_resetsQueryAndRestoresGuidance() = runTuIndiceUiTest {
		val repository = ControllableSubjectCatalogRepository(
			localResults = listOf(subjectSearchResult(subjectCode = "MA1111"))
		)
		val viewModel = createViewModel(repository)

		setTuIndiceTestContent {
			SubjectSearchRoute(
				viewModel = viewModel,
				onSubjectClick = {}
			)
		}

		onNodeWithTag(SubjectsUiTags.searchExample(0)).performClick()
		waitUntilTagExists(SubjectsUiTags.searchResult("MA1111"))
		onNodeWithTag(SubjectsUiTags.SearchClear).performClick()

		waitUntilTagExists(SubjectsUiTags.SearchGuidance)

		assertNodeHidden(SubjectsUiTags.SearchResults)
		assertNodeHidden(SubjectsUiTags.SearchClear)
		assertEquals("", viewModel.state.value.query)
	}

	@Test
	fun when_remoteSearchFailsWithoutResults_then_retryRunsTheRefreshAgain() = runTuIndiceUiTest {
		val repository = ControllableSubjectCatalogRepository(
			refreshResponses = ArrayDeque(listOf<Any>(IllegalStateException("boom")))
		)
		val viewModel = createViewModel(repository)

		setTuIndiceTestContent {
			SubjectSearchRoute(
				viewModel = viewModel,
				onSubjectClick = {}
			)
		}

		onNodeWithTag(SubjectsUiTags.searchExample(0)).performClick()

		waitUntilTagExists(SubjectsUiTags.SearchRetry)
		onNodeWithText("No pudimos buscar materias").assertIsDisplayed()
		assertEquals(listOf("MA1111"), repository.refreshCalls)

		onNodeWithTag(SubjectsUiTags.SearchRetry).performClick()

		waitUntil(timeoutMillis = WAIT_TIMEOUT_MILLIS) {
			onAllNodesWithTag(SubjectsUiTags.SearchRetry).fetchSemanticsNodes().isEmpty() &&
				!viewModel.state.value.isRefreshing
		}

		onNodeWithText("No encontramos materias").assertIsDisplayed()
		assertEquals(listOf("MA1111", "MA1111"), repository.refreshCalls)
	}

	private fun ComposeUiTest.waitUntilTagExists(tag: String) {
		waitUntil(timeoutMillis = WAIT_TIMEOUT_MILLIS) {
			onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
		}
	}

	private fun createViewModel(
		repository: ControllableSubjectCatalogRepository
	): SubjectSearchViewModel {
		val reportingRepository = RecordingReportingRepository()

		return SubjectSearchViewModel(
			screenMachine = SubjectSearchMachine(
				draft = SubjectSearchDraft(),
				observeSubjectSearchUseCase = ObserveSubjectSearchUseCase(
					subjectCatalogRepository = repository,
					reportingRepository = reportingRepository
				),
				refreshSubjectSearchUseCase = RefreshSubjectSearchUseCase(
					subjectCatalogRepository = repository,
					reportingRepository = reportingRepository
				)
			),
			eventPublisher = NoOpEventPublisher
		)
	}

	private companion object {
		const val WAIT_TIMEOUT_MILLIS = 15_000L
	}
}
