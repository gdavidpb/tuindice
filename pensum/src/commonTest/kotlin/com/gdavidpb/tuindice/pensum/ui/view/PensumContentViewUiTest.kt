package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenSessionStore
import com.gdavidpb.tuindice.pensum.testing.SAMPLE_CURRENT_NODE_ID
import com.gdavidpb.tuindice.pensum.testing.SAMPLE_PENSUM_SELECTION_KEY
import com.gdavidpb.tuindice.pensum.testing.samplePensumScreenModelWithSelectableYears
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
class PensumContentViewUiTest {
	@Test
	fun when_contentIsIdle_then_showsSummaryAndCanvasWithoutBannersOrSheets() = runTuIndiceUiTest {
		setContentViewContent(fixture = ContentViewFixture())

		assertNodeVisible(PensumUiTags.PensumScreen)
		assertNodeVisible(PensumUiTags.PensumSummaryContainer)
		assertNodeVisible(PensumUiTags.Canvas)
		assertNodeHidden(PensumUiTags.RefreshingIndicator)
		assertNodeHidden(PensumUiTags.LocalDataWarning)
		assertNodeHidden(PensumUiTags.SubjectDetailSheet)
		assertNodeHidden(PensumUiTags.PensumCurrentSelectionSummary)
	}

	@Test
	fun when_refreshingWithALocalDataMessage_then_refreshIndicatorWinsOverTheWarning() =
		runTuIndiceUiTest {
			setContentViewContent(
				fixture = ContentViewFixture(),
				isRefreshing = true,
				localDataMessage = UiText.Raw(LOCAL_DATA_MESSAGE)
			)

			assertNodeVisible(PensumUiTags.RefreshingIndicator)
			assertNodeHidden(PensumUiTags.LocalDataWarning)
		}

	@Test
	fun when_notRefreshingWithALocalDataMessage_then_warningShowsThatMessage() = runTuIndiceUiTest {
		setContentViewContent(
			fixture = ContentViewFixture(),
			localDataMessage = UiText.Raw(LOCAL_DATA_MESSAGE)
		)

		assertNodeVisible(PensumUiTags.LocalDataWarning)
		onNode(
			hasText(LOCAL_DATA_MESSAGE) and hasParent(hasTestTag(PensumUiTags.LocalDataWarning))
		).assertExists()
		assertNodeHidden(PensumUiTags.RefreshingIndicator)
	}

	@Test
	fun when_nodeIsTapped_then_detailOpensAndSessionStoreKeepsTheSubjectContext() = runTuIndiceUiTest {
		val fixture = ContentViewFixture()
		setContentViewContent(fixture = fixture)

		onNodeWithTag(PensumUiTags.node(SAMPLE_CURRENT_NODE_ID)).performClick()

		assertNodeVisible(PensumUiTags.SubjectDetailSheet)
		onNodeWithTag(PensumUiTags.SubjectDetailCode).assertTextEquals("CI4325")
		assertEquals(SAMPLE_CURRENT_NODE_ID, fixture.selectionState.focusedNodeId)
		assertEquals(SAMPLE_CURRENT_NODE_ID, fixture.selectionState.detailNodeId)
	}

	@Test
	fun when_statsAreRequestedFromDetail_then_reportsTheCodeAndClearsTheSubjectContext() =
		runTuIndiceUiTest {
			val fixture = ContentViewFixture()
			setContentViewContent(fixture = fixture)

			onNodeWithTag(PensumUiTags.node(SAMPLE_CURRENT_NODE_ID)).performClick()
			assertNodeVisible(PensumUiTags.SubjectDetailSheet)
			onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()
			waitForIdle()

			assertEquals(listOf("stats:CI4325"), fixture.events)
			assertNodeHidden(PensumUiTags.SubjectDetailSheet)
			assertNull(fixture.selectionState.focusedNodeId)
			assertNull(fixture.selectionState.detailNodeId)
		}

	@Test
	fun when_summaryHeaderAndPensumContextAreTapped_then_eachReportsItsOwnEvent() = runTuIndiceUiTest {
		val fixture = ContentViewFixture()
		setContentViewContent(fixture = fixture)

		onNodeWithTag(PensumUiTags.PensumSummaryContainer).performClick()
		onNodeWithTag(PensumUiTags.PensumContextSummary).performClick()

		runOnIdle { assertEquals(listOf("summaryToggle", "pensumContext"), fixture.events) }
	}

	@Test
	fun when_selectionSheetIsShown_then_applyingAnotherYearReportsTheSelection() = runTuIndiceUiTest {
		val fixture = ContentViewFixture()
		setContentViewContent(fixture = fixture, showSelectionSheet = true)

		onNodeWithText("Cambiar pensum").assertExists()
		assertNodeVisible(PensumUiTags.PensumCurrentSelectionSummary)
		onNodeWithTag(PensumUiTags.versionOption(year = 2018)).performClick()
		onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()
		waitUntil(timeoutMillis = TIMEOUT_MILLIS) { "selectionDismiss" in fixture.events }

		assertEquals(listOf("selection:2018/degree_project", "selectionDismiss"), fixture.events)
	}

	@Test
	fun when_summaryIsCollapsed_then_pensumContextIsHiddenBehindTheHeader() = runTuIndiceUiTest {
		setContentViewContent(fixture = ContentViewFixture(), isSummaryCollapsed = true)

		assertNodeVisible(PensumUiTags.PensumSummaryContainer)
		assertNodeHidden(PensumUiTags.PensumContextSummary)
		assertNodeVisible(PensumUiTags.Canvas)
	}
}

private class ContentViewFixture {
	val events = mutableListOf<String>()
	val screenSessionStore = PensumScreenSessionStore()
	val selectionState get() = screenSessionStore.stateFor(SAMPLE_PENSUM_SELECTION_KEY)
}

@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.setContentViewContent(
	fixture: ContentViewFixture,
	isRefreshing: Boolean = false,
	localDataMessage: UiText? = null,
	showSelectionSheet: Boolean = false,
	isSummaryCollapsed: Boolean = false
) {
	val events = fixture.events

	setTuIndiceTestContent {
		PensumContentView(
			model = samplePensumScreenModelWithSelectableYears(),
			isRefreshing = isRefreshing,
			localDataMessage = localDataMessage,
			showSelectionSheet = showSelectionSheet,
			screenSessionStore = fixture.screenSessionStore,
			isSummaryCollapsed = isSummaryCollapsed,
			onSummaryCollapsedToggle = { events += "summaryToggle" },
			onSelectionSheetDismiss = { events += "selectionDismiss" },
			onSubjectStatsClick = { subjectCode -> events += "stats:$subjectCode" },
			onSelectionApplied = { pensum, modality -> events += "selection:${pensum.year}/${modality.id}" },
			onPensumContextClick = { events += "pensumContext" }
		)
	}
}

private const val LOCAL_DATA_MESSAGE = "Sin conexión. Mostramos la información guardada."
private const val TIMEOUT_MILLIS = 5_000L
