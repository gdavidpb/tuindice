package com.gdavidpb.tuindice.record.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.ui.LocalTuIndiceCompositionProbe
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.record.testing.RecordRouteFixture
import com.gdavidpb.tuindice.record.testing.academicAttempt
import com.gdavidpb.tuindice.record.testing.academicTerm
import com.gdavidpb.tuindice.record.testing.recordRouteFixture
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// The probe names each card of a term. A card whose term keeps its data keeps its composition when the selected term
// changes: the selector and the pager both make the change, and neither may compose such a card again.
@OptIn(ExperimentalTestApi::class)
class RecordTermChangeCompositionUiTest {
	@Test
	fun when_aTermIsChosenInTheSelector_then_theCardOfTheLeavingTermIsNotComposedAgain() = runTuIndiceUiTest {
		val fixture = recordRouteFixture(record = twoTermsRecord())
		val compositions = mutableListOf<String>()
		fixture.selectionRepository.setSelectedTermId(viewMode = RecordViewMode.Projection, termId = CurrentTermId)

		setRoute(fixture, compositions)
		awaitSelectedTerm(fixture, CurrentTermId)
		val settled = compositions.countsByName()

		onNodeWithTag(RecordUiTags.termChip(HistoricalTermId)).performClick()

		awaitSelectedTerm(fixture, HistoricalTermId)
		assertCardsOfTheChange(settled, compositions.countsByName())
	}

	@Test
	fun when_thePagerSwipesToAnotherTerm_then_theCardOfTheTermItLandsOnComposesOnce() = runTuIndiceUiTest {
		val fixture = recordRouteFixture(record = twoTermsRecord())
		val compositions = mutableListOf<String>()
		fixture.selectionRepository.setSelectedTermId(viewMode = RecordViewMode.Projection, termId = CurrentTermId)

		setRoute(fixture, compositions)
		awaitSelectedTerm(fixture, CurrentTermId)
		val settled = compositions.countsByName()

		onNodeWithTag(RecordUiTags.TermPager).performTouchInput { swipeRight() }

		awaitSelectedTerm(fixture, HistoricalTermId)
		assertCardsOfTheChange(settled, compositions.countsByName())
	}

	private fun ComposeUiTest.setRoute(fixture: RecordRouteFixture, compositions: MutableList<String>) {
		val probe: (String) -> Unit = { name -> compositions += name }

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceCompositionProbe provides probe) {
				Route(viewModel = fixture.viewModel)
			}
		}
	}

	// The record is stored and its refresh has run once, so the cards compose only for what the change does.
	private fun ComposeUiTest.awaitSelectedTerm(fixture: RecordRouteFixture, termId: String) {
		waitUntil(timeoutMillis = StateTimeoutMillis) {
			(fixture.viewModel.state.value as? Record.State.Content)?.selectedTermId == termId
		}
		waitUntil(timeoutMillis = StateTimeoutMillis) {
			fixture.academicRecordRepository.updateAcademicRecordCalls == 1
		}
		waitForIdle()
	}

	private fun assertCardsOfTheChange(settled: Map<String, Int>, changed: Map<String, Int>) {
		assertTrue((settled[CurrentCard] ?: 0) >= 1, "the term on screen composed its card before the change")
		assertEquals(
			settled[CurrentCard] ?: 0,
			changed[CurrentCard] ?: 0,
			"the card of the term that leaves composes no more for the change"
		)
		assertEquals(1, changed[HistoricalCard] ?: 0, "the card of the term that enters composes once, for its page")
	}

	@Composable
	private fun Route(viewModel: RecordViewModel) {
		RecordRoute(
			onNavigateToUpdatePassword = {},
			onNavigateToCreateSyntheticTerm = {},
			onNavigateToUpdateSyntheticTerm = {},
			onNavigateToDeleteSyntheticTermConfirmation = {},
			onTopBarViewModeChangeAvailable = {},
			onTopBarTermSelectionAvailable = {},
			onNavigateToEnrollmentProof = {},
			showTopBarBanner = {},
			showSnackBar = {},
			viewModel = viewModel
		)
	}

	// Two closed terms, one attempt each, under the ids the probe names. The current term is the one selected first.
	private fun twoTermsRecord() = AcademicRecord(
		id = "record",
		terms = listOf(
			academicTerm(
				id = HistoricalTermId,
				kind = TermKind.HISTORICAL,
				periodYear = 2025,
				periodCode = AcademicTermPeriod.SEP_DEC,
				attempts = listOf(
					academicAttempt(subjectCode = "MA1111", id = "attempt-historical", score = AttemptScore.numeric(3))
				)
			),
			academicTerm(
				id = CurrentTermId,
				kind = TermKind.CURRENT,
				periodYear = 2026,
				periodCode = AcademicTermPeriod.SEP_DEC,
				attempts = listOf(
					academicAttempt(subjectCode = "CI5311", id = "attempt-current", score = AttemptScore.numeric(4))
				)
			)
		)
	)

	private fun List<String>.countsByName() = groupingBy { it }.eachCount()

	private companion object {
		const val HistoricalTermId = "historical-2025"
		const val CurrentTermId = "current-2026"
		const val HistoricalCard = "term-card:attempt-historical"
		const val CurrentCard = "term-card:attempt-current"

		// A cold start of the view model plus the first resource load on the simulator.
		const val StateTimeoutMillis = 15_000L
	}
}
