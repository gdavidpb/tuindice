package com.gdavidpb.tuindice.summary.presentation.route

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.summary.presentation.contract.ShareTerm
import com.gdavidpb.tuindice.summary.presentation.viewmodel.ShareTermViewModel
import com.gdavidpb.tuindice.summary.testing.ControllableRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.summary.testing.ControllableVisibleAcademicRecordRepository
import com.gdavidpb.tuindice.summary.testing.RecordingShareImageRepository
import com.gdavidpb.tuindice.summary.testing.createObserveSummaryHubUseCase
import com.gdavidpb.tuindice.summary.testing.hubClosedTerm
import com.gdavidpb.tuindice.summary.testing.hubPassedAttempt
import com.gdavidpb.tuindice.summary.testing.hubRecord
import com.gdavidpb.tuindice.summary.testing.shareTermMachineOf
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ShareTermRouteUiTest {
	@Test
	fun when_theSheetOpensOnATerm_then_itSelectsTheTermItWasAskedFor() = runTuIndiceUiTest {
		val viewModel = createShareViewModel(images = RecordingShareImageRepository())

		setTuIndiceTestContent {
			ShareTermRoute(termKey = olderTermKey(), viewModel = viewModel, onDismissRequest = {})
		}

		waitUntil(timeoutMillis = TIMEOUT_MILLIS) { selectedTermKey(viewModel) == olderTermKey() }
		assertEquals(olderTermKey(), selectedTermKey(viewModel))
		assertEquals(olderTermKey(), selectedTermKey(viewModel))
	}

	@Test
	fun when_aShareFails_then_theSnackbarSaysSoAndTheSheetStaysOpen() = runTuIndiceUiTest {
		val viewModel = createShareViewModel(
			images = RecordingShareImageRepository(failure = IllegalStateException(FAILURE_CAUSE))
		)

		setTuIndiceTestContent {
			ShareTermRoute(termKey = null, viewModel = viewModel, onDismissRequest = {})
		}

		waitUntil(timeoutMillis = TIMEOUT_MILLIS) { selectedTermKey(viewModel) != null }
		waitForIdle()
		onNodeWithTag(SummaryUiTags.ShareTermShareButton).assertIsEnabled().performClick()

		waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
			onAllNodesWithText(FAILED_MESSAGE).fetchSemanticsNodes().isNotEmpty()
		}
		onNodeWithText(FAILED_MESSAGE).assertExists()
		onNodeWithTag(SummaryUiTags.ShareTermSheet).assertIsDisplayed()
	}

	private fun selectedTermKey(viewModel: ShareTermViewModel): String? {
		return (viewModel.state.value as? ShareTerm.State.Content)?.selectedTerm?.key
	}

	// The key of the older of the two closed terms of the record, which is the year and the period of the term: the sheet
	// opens on it when it is asked for.
	private fun olderTermKey(): String = "$OLDER_YEAR-SEP_DEC"

	private fun createShareViewModel(images: RecordingShareImageRepository): ShareTermViewModel {
		return ShareTermViewModel(
			screenMachine = shareTermMachineOf(
				hub = createObserveSummaryHubUseCase(
					prerequisite = ControllableRecordDataPrerequisiteRepository(isReady = true),
					record = ControllableVisibleAcademicRecordRepository(initialRecord = closedRecord())
				),
				images = images
			),
			eventPublisher = NoOpEventPublisher,
			dispatchers = TestTuIndiceDispatchers(Dispatchers.Unconfined)
		)
	}

	private fun closedRecord(): AcademicRecord {
		return hubRecord(
			hubClosedTerm(
				year = OLDER_YEAR,
				attempts = listOf(hubPassedAttempt(code = "CI1")),
				index = 3.0
			),
			hubClosedTerm(
				year = 2025,
				attempts = listOf(hubPassedAttempt(code = "CI2")),
				index = 4.0
			)
		)
	}

	private companion object {
		const val TIMEOUT_MILLIS = 10_000L
		const val OLDER_YEAR = 2024
		const val FAILURE_CAUSE = "La hoja no se pudo mostrar."

		// The text the sheet shows when the image cannot be prepared, as the app's strings give it.
		const val FAILED_MESSAGE = "No pudimos preparar la imagen. Inténtalo de nuevo."
	}
}
