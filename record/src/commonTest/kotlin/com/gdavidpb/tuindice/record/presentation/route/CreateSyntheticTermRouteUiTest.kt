package com.gdavidpb.tuindice.record.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationSnapshot
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.presentation.viewmodel.CreateSyntheticTermViewModel
import com.gdavidpb.tuindice.record.testing.academicAttempt
import com.gdavidpb.tuindice.record.testing.academicTerm
import com.gdavidpb.tuindice.record.testing.createSyntheticTermRouteFixture
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class CreateSyntheticTermRouteUiTest {
	@Test
	fun when_theFormOpensUntouched_then_itShowsWhatThePensumSuggests_andBackIsNotHeldUp() = runTuIndiceUiTest {
		val fixture = createSyntheticTermRouteFixture()
		val host = RouteHost()

		setTuIndiceTestContent {
			Route(viewModel = fixture.viewModel, host = host)
		}

		assertNodeVisible(RecordUiTags.CreateSyntheticTermScreen)

		fixture.creationRepository.snapshotFlow.value = snapshot(selectedPeriod = null, selected = emptyList())
		awaitState { fixture.viewModel.state.value.suggestedSubjects.isNotEmpty() }

		assertNodeVisible(RecordUiTags.createSyntheticTermSubject("MA1111"))
		onNodeWithText("MATEMÁTICAS I").assertIsDisplayed()

		// Nothing was chosen yet: leaving loses nothing, so the route lets the back gesture through.
		val isBackHeld = runOnIdle { assertNotNull(host.backInterceptor).invoke() }

		assertFalse(isBackHeld)
		waitForIdle()
		assertNodeHidden(BaseUiTags.ConfirmationDialogSheet)
		assertEquals(0, host.backs)
	}

	@Test
	fun when_aSuggestionIsAdded_then_theSubjectJoinsTheDraftOfTheTerm() = runTuIndiceUiTest {
		val fixture = createSyntheticTermRouteFixture()
		val host = RouteHost()

		setTuIndiceTestContent {
			Route(viewModel = fixture.viewModel, host = host)
		}

		fixture.creationRepository.snapshotFlow.value = snapshot(selectedPeriod = null, selected = emptyList())
		awaitState { fixture.viewModel.state.value.suggestedSubjects.isNotEmpty() }

		onNodeWithTag(
			RecordUiTags.createSyntheticTermSubjectAction(subjectCode = "MA1111", action = "add")
		).performClick()

		// The tap went through the view model into the draft the snapshot is computed from.
		val draftedSubjects = fixture.creationRepository.observeSnapshotCalls.first().selectedSubjectsFlow

		waitUntil(timeoutMillis = StateTimeoutMillis) { draftedSubjects.value.isNotEmpty() }
		assertEquals(listOf("MA1111"), draftedSubjects.value.map(SyntheticTermSubject::subjectCode))
	}

	@Test
	fun when_backIsAskedWithADraft_then_itIsHeld_andCancelKeepsTheStudentOnTheForm() = runTuIndiceUiTest {
		val fixture = createSyntheticTermRouteFixture()
		val host = RouteHost()

		setTuIndiceTestContent {
			Route(viewModel = fixture.viewModel, host = host)
		}

		fixture.creationRepository.snapshotFlow.value = snapshot(selectedPeriod = FarPeriod, selected = listOf(Algebra))
		awaitState { fixture.viewModel.state.value.hasDiscardableDraft }

		val isBackHeld = runOnIdle { assertNotNull(host.backInterceptor).invoke() }

		assertTrue(isBackHeld, "a period and a subject are chosen: leaving would lose them")
		assertNodeVisible(RecordUiTags.DiscardSyntheticTermMessage)

		onNodeWithTag(BaseUiTags.ConfirmationDialogNegativeButton).performClick()

		waitUntil(timeoutMillis = StateTimeoutMillis) {
			onAllNodesWithTag(BaseUiTags.ConfirmationDialogSheet).fetchSemanticsNodes().isEmpty()
		}
		assertEquals(0, host.backs)
		assertNodeVisible(RecordUiTags.CreateSyntheticTermScreen)
	}

	@Test
	fun when_theDiscardIsConfirmed_then_theRouteGoesBack_once_andTheDialogCloses() = runTuIndiceUiTest {
		val fixture = createSyntheticTermRouteFixture()
		val host = RouteHost()

		setTuIndiceTestContent {
			Route(viewModel = fixture.viewModel, host = host)
		}

		fixture.creationRepository.snapshotFlow.value = snapshot(selectedPeriod = FarPeriod, selected = listOf(Algebra))
		awaitState { fixture.viewModel.state.value.hasDiscardableDraft }

		runOnIdle { assertNotNull(host.backInterceptor).invoke() }
		assertNodeVisible(BaseUiTags.ConfirmationDialogPositiveButton)
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton)
			.assertTextEquals("Descartar")
			.performClick()

		waitUntil(timeoutMillis = StateTimeoutMillis) { host.backs == 1 }
		waitUntil(timeoutMillis = StateTimeoutMillis) {
			onAllNodesWithTag(BaseUiTags.ConfirmationDialogSheet).fetchSemanticsNodes().isEmpty()
		}
		assertEquals(1, host.backs)
		// Discarding saves nothing.
		assertTrue(fixture.academicRecordRepository.addedTerms.isEmpty())
	}

	@Test
	fun when_theTermIsCreated_then_itIsSaved_andTheRouteGoesBack() = runTuIndiceUiTest {
		val fixture = createSyntheticTermRouteFixture(record = recordWithAClosedTerm())
		val host = RouteHost()

		setTuIndiceTestContent {
			Route(viewModel = fixture.viewModel, host = host)
		}

		fixture.creationRepository.snapshotFlow.value = snapshot(selectedPeriod = FarPeriod, selected = listOf(Algebra))
		awaitState { fixture.viewModel.state.value.canSubmit }

		onNodeWithText("1 materia seleccionada").assertIsDisplayed()
		onNodeWithTag(RecordUiTags.CreateSyntheticTermSubmitButton)
			.assertTextEquals("Crear")
			.assertIsEnabled()
			.performClick()

		waitUntil(timeoutMillis = StateTimeoutMillis) { host.backs == 1 }

		val created = fixture.academicRecordRepository.addedTerms.single()

		assertEquals(FarPeriod.termKey, created.termId)
		assertEquals(listOf("MA2611"), created.attempts.map { attempt -> attempt.subjectCode })
		// Saving is not discarding: nobody is asked to confirm it.
		assertNodeHidden(BaseUiTags.ConfirmationDialogSheet)
	}

	@Test
	fun when_theStudentRemovesASubjectOfTheTerm_then_itLeavesTheDraft() = runTuIndiceUiTest {
		val fixture = createSyntheticTermRouteFixture()
		val host = RouteHost()

		setTuIndiceTestContent {
			Route(viewModel = fixture.viewModel, host = host)
		}

		fixture.creationRepository.snapshotFlow.value = snapshot(selectedPeriod = FarPeriod, selected = listOf(Algebra))
		awaitState { fixture.viewModel.state.value.selectedSubjects.isNotEmpty() }

		val draftedSubjects = fixture.creationRepository.observeSnapshotCalls.first().selectedSubjectsFlow

		// The draft starts from what the student adds: put the subject in it, then take it out.
		runOnIdle { fixture.viewModel.addSubjectAction(fixture.viewModel.state.value.selectedSubjects.single()) }
		waitUntil(timeoutMillis = StateTimeoutMillis) { draftedSubjects.value.isNotEmpty() }

		onNodeWithTag(
			RecordUiTags.createSyntheticTermSubjectAction(subjectCode = "MA2611", action = "remove")
		).performClick()

		waitUntil(timeoutMillis = StateTimeoutMillis) { draftedSubjects.value.isEmpty() }
	}

	@Test
	fun when_theRouteLeavesTheScreen_then_itStopsInterceptingBack() = runTuIndiceUiTest {
		val fixture = createSyntheticTermRouteFixture()
		val host = RouteHost()
		val isOnScreen = mutableStateOf(true)

		setTuIndiceTestContent {
			if (isOnScreen.value) {
				Route(viewModel = fixture.viewModel, host = host)
			}
		}

		assertNodeVisible(RecordUiTags.CreateSyntheticTermScreen)
		assertNotNull(host.backInterceptor)

		runOnIdle { isOnScreen.value = false }
		waitForIdle()

		// A form that is gone must not go on answering for the back gesture of the next screen.
		assertNull(host.backInterceptor)
	}

	// The snapshot reaches the screen through the view model: wait for the state, then for the frame.
	private fun ComposeUiTest.awaitState(isReached: () -> Boolean) {
		waitUntil(timeoutMillis = StateTimeoutMillis) { isReached() }
		waitForIdle()
	}

	@Composable
	private fun Route(viewModel: CreateSyntheticTermViewModel, host: RouteHost) {
		CreateSyntheticTermRoute(
			termId = null,
			viewModel = viewModel,
			onBack = { host.backs++ },
			onBackInterceptorAvailable = { interceptor -> host.backInterceptor = interceptor },
			onSubjectStatsClick = { subjectCode -> host.openedStats += subjectCode }
		)
	}

	// What stands around the route in the app: the navigator, and the bar's back arrow that asks
	// the route whether it may leave.
	private class RouteHost {
		var backInterceptor: (() -> Boolean)? = null
		var backs = 0
		val openedStats = mutableListOf<String>()
	}

	private fun snapshot(
		selectedPeriod: SyntheticTermPeriodOption?,
		selected: List<SyntheticTermSubject>
	) = SyntheticTermCreationSnapshot(
		periodOptions = listOf(FarPeriod),
		selectedPeriod = selectedPeriod,
		selectedSubjects = selected,
		suggestedSubjects = listOf(SyntheticTermSubject(subjectCode = "MA1111", name = "Matemáticas I", credits = 4)),
		searchResults = emptyList()
	)

	// A record the new term can be validated against: one closed term, long before the new one.
	private fun recordWithAClosedTerm() = AcademicRecord(
		id = "record",
		terms = listOf(
			academicTerm(
				id = "historical",
				kind = TermKind.HISTORICAL,
				attempts = listOf(academicAttempt(subjectCode = "MA1111", outcome = AttemptOutcome.APPROVED))
			)
		)
	)

	private companion object {
		// Far enough ahead that no clock makes it a term of the past.
		val FarPeriod = SyntheticTermPeriodOption(periodYear = 9999, periodCode = AcademicTermPeriod.JAN_MAR)
		val Algebra = SyntheticTermSubject(subjectCode = "MA2611", name = "Álgebra lineal", credits = 4)

		// A cold start of the view model plus the first resource load on the simulator.
		const val StateTimeoutMillis = 15_000L
	}
}
