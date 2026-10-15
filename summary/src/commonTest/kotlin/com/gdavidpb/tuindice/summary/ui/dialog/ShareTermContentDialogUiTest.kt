package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.presentation.contract.ShareTerm
import com.gdavidpb.tuindice.summary.presentation.mapper.toShareableTermCardItem
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermBackgroundItem
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermTermItem
import com.gdavidpb.tuindice.summary.presentation.model.ShareableTermCardItem
import com.gdavidpb.tuindice.summary.testing.otherShareBackground
import com.gdavidpb.tuindice.summary.testing.statisticsTerm
import com.gdavidpb.tuindice.summary.testing.unreachableShareBackground
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.share.ShareTermUiTags
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalTestApi::class)
class ShareTermContentDialogUiTest {
	@Test
	fun when_noTermHasClosed_then_theSheetSaysWhenItCanBeShared_andTheButtonIsDisabled() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermContentDialog(
				state = ShareTerm.State.Content(terms = emptyList(), backgrounds = backgrounds()),
				snackbarHostState = remember { SnackbarHostState() },
				actions = ShareTermSheetActions(onTermClick = {}, onBackgroundClick = {}, onShareClick = {}),
				onDismissRequest = {}
			)
		}

		onAllNodesWithTag(SummaryUiTags.ShareTermEmpty).assertCountEquals(1)
		onNodeWithTag(SummaryUiTags.ShareTermShareButton).assertIsNotEnabled()
	}

	@Test
	fun when_theRecordCannotBeRead_then_theSheetSaysSo() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			ShareTermContentDialog(
				state = ShareTerm.State.Unavailable(
					title = UiText.Raw("Compartir no disponible"),
					message = UiText.Raw("No pudimos leer tu registro en este momento.")
				),
				snackbarHostState = remember { SnackbarHostState() },
				actions = ShareTermSheetActions(onTermClick = {}, onBackgroundClick = {}, onShareClick = {}),
				onDismissRequest = {}
			)
		}

		onAllNodesWithTag(SummaryUiTags.ShareTermUnavailableTitle).assertCountEquals(1)
		onAllNodesWithTag(SummaryUiTags.ShareTermUnavailableMessage).assertCountEquals(1)
	}

	@Test
	fun when_theCardIsCaptured_then_sharingHandsItsPictureOver() = runTuIndiceUiTest {
		val shared = mutableListOf<ImageBitmap>()

		setTuIndiceTestContent {
			ShareTermContentDialog(
				state = ShareTerm.State.Content(terms = terms(), backgrounds = backgrounds()),
				snackbarHostState = remember { SnackbarHostState() },
				actions = ShareTermSheetActions(
					onTermClick = {},
					onBackgroundClick = {},
					onShareClick = { bitmap -> shared += bitmap }
				),
				onDismissRequest = {}
			)
		}

		// The button is there from the start: the picture is taken when the student asks for it.
		onNodeWithTag(SummaryUiTags.ShareTermShareButton).assertIsEnabled().performClick()
		waitUntil(timeoutMillis = CAPTURE_TIMEOUT_MILLIS) { shared.isNotEmpty() }

		assertEquals(CARD_WIDTH_PIXELS, shared.single().width)
	}

	@Test
	fun when_thePreviewIsSwipedLeftThenRight_then_itAsksForTheNextBackgroundAndThenThePreviousOne() = runTuIndiceUiTest {
		val asked = mutableListOf<String>()
		val chosen = mutableStateOf(ShareBackground.included.id)

		setTuIndiceTestContent {
			ShareTermContentDialog(
				state = ShareTerm.State.Content(
					terms = terms(),
					backgrounds = backgrounds(chosenId = chosen.value)
				),
				snackbarHostState = remember { SnackbarHostState() },
				actions = ShareTermSheetActions(
					onTermClick = {},
					onBackgroundClick = { id ->
						asked += id
						chosen.value = id
					},
					onShareClick = {}
				),
				onDismissRequest = {}
			)
		}

		onNodeWithTag(SummaryUiTags.ShareTermPreview).performTouchInput { swipeLeft() }
		waitForIdle()
		onNodeWithTag(SummaryUiTags.ShareTermPreview).performTouchInput { swipeRight() }
		waitForIdle()

		assertEquals(listOf(otherShareBackground.id, ShareBackground.included.id), asked)
	}

	@Test
	fun when_aTermIsChosen_then_theShareButtonStaysEnabled_andTheCardShowsIt() = runTuIndiceUiTest {
		val chosen = mutableStateOf(NEWEST_TERM_KEY)

		setTuIndiceTestContent {
			ShareTermContentDialog(
				state = ShareTerm.State.Content(terms = terms(chosen.value), backgrounds = backgrounds()),
				snackbarHostState = remember { SnackbarHostState() },
				actions = ShareTermSheetActions(
					onTermClick = { termKey -> chosen.value = termKey },
					onBackgroundClick = {},
					onShareClick = {}
				),
				onDismissRequest = {}
			)
		}
		onNodeWithTag(SummaryUiTags.ShareTermShareButton).assertIsEnabled()

		onNodeWithTag(ShareTermUiTags.term(OLDER_TERM_KEY)).performClick()
		val olderIndex = terms().last().card.index
		waitUntil(timeoutMillis = CAPTURE_TIMEOUT_MILLIS) {
			runCatching { onNodeWithTag(SummaryUiTags.ShareTermCardIndex).assertTextEquals(olderIndex) }.isSuccess
		}

		onAllNodesWithTag(ShareTermUiTags.termSelected(OLDER_TERM_KEY)).assertCountEquals(1)
		onNodeWithTag(SummaryUiTags.ShareTermShareButton).assertIsEnabled()
	}

	@Test
	fun when_aBackgroundIsChosen_then_theShareButtonStaysEnabled_andTheCardWearsIt() = runTuIndiceUiTest {
		val chosen = mutableStateOf(ShareBackground.included.id)

		setTuIndiceTestContent {
			ShareTermContentDialog(
				state = ShareTerm.State.Content(terms = terms(), backgrounds = backgrounds(chosenId = chosen.value)),
				snackbarHostState = remember { SnackbarHostState() },
				actions = ShareTermSheetActions(
					onTermClick = {},
					onBackgroundClick = { id -> chosen.value = id },
					onShareClick = {}
				),
				onDismissRequest = {}
			)
		}

		onNodeWithTag(ShareTermUiTags.background(otherShareBackground.id)).performClick()
		waitUntil(timeoutMillis = CAPTURE_TIMEOUT_MILLIS) {
			runCatching { onNodeWithTag(SummaryUiTags.ShareTermCardCredit).assertTextEquals(OTHER_CREDIT) }.isSuccess
		}

		onNodeWithTag(SummaryUiTags.ShareTermShareButton).assertIsEnabled()
	}

	@Test
	fun when_theBackgroundChangesBeforeSharing_then_thePictureSharedIsTheNewCard() = runTuIndiceUiTest {
		val shared = mutableListOf<ImageBitmap>()
		val chosen = mutableStateOf(ShareBackground.included.id)

		setTuIndiceTestContent {
			ShareTermContentDialog(
				state = ShareTerm.State.Content(terms = terms(), backgrounds = backgrounds(chosenId = chosen.value)),
				snackbarHostState = remember { SnackbarHostState() },
				actions = ShareTermSheetActions(
					onTermClick = {},
					onBackgroundClick = { id -> chosen.value = id },
					onShareClick = { bitmap -> shared += bitmap }
				),
				onDismissRequest = {}
			)
		}

		onNodeWithTag(SummaryUiTags.ShareTermShareButton).performClick()
		waitUntil(timeoutMillis = CAPTURE_TIMEOUT_MILLIS) { shared.size == 1 }

		onNodeWithTag(ShareTermUiTags.background(otherShareBackground.id)).performClick()
		waitForIdle()
		onNodeWithTag(SummaryUiTags.ShareTermShareButton).assertIsEnabled().performClick()
		waitUntil(timeoutMillis = CAPTURE_TIMEOUT_MILLIS) { shared.size == 2 }

		assertEquals(CARD_WIDTH_PIXELS, shared[1].width)
		// The two cards differ in their credit alone: a picture of the card before the change would be the same as the first.
		assertFalse(shared[0].pixels().contentEquals(shared[1].pixels()), "The picture shared is the card before the change.")
	}

	@Test
	fun when_aRemoteBackgroundCannotBeLoaded_then_thePictureSharedIsTheBundledCard() = runTuIndiceUiTest {
		val shared = mutableListOf<ImageBitmap>()
		val chosen = mutableStateOf(ShareBackground.included.id)
		val all = listOf(ShareBackground.included, otherShareBackground, unreachableShareBackground)

		setTuIndiceTestContent {
			ShareTermContentDialog(
				state = ShareTerm.State.Content(
					terms = terms(),
					backgrounds = backgrounds(chosenId = chosen.value, all = all)
				),
				snackbarHostState = remember { SnackbarHostState() },
				actions = ShareTermSheetActions(
					onTermClick = {},
					onBackgroundClick = { id -> chosen.value = id },
					onShareClick = { bitmap -> shared += bitmap }
				),
				onDismissRequest = {}
			)
		}

		onNodeWithTag(SummaryUiTags.ShareTermShareButton).performClick()
		waitUntil(timeoutMillis = CAPTURE_TIMEOUT_MILLIS) { shared.size == 1 }

		onNodeWithTag(ShareTermUiTags.background(unreachableShareBackground.id)).performClick()
		waitForIdle()
		onNodeWithTag(SummaryUiTags.ShareTermShareButton).performClick()
		waitUntil(timeoutMillis = REMOTE_TIMEOUT_MILLIS) { shared.size == 2 }

		// The artwork that cannot be loaded gives way to the one the app ships with, credit and all: the picture is the same.
		assertEquals(CARD_WIDTH_PIXELS, shared[1].width)
		assertEquals(
			0,
			shared[0].pixels().differencesFrom(shared[1].pixels()),
			"The remote artwork is not shared as the bundled card."
		)
	}

	private fun terms(selectedKey: String = NEWEST_TERM_KEY): List<ShareTermTermItem> {
		return listOf(
			ShareTermTermItem(
				key = NEWEST_TERM_KEY,
				label = UiText.Raw("Ene - Mar 2024"),
				card = card(year = 2024, periodCode = AcademicTermPeriod.JAN_MAR, index = 4.0),
				selected = false
			),
			ShareTermTermItem(
				key = OLDER_TERM_KEY,
				label = UiText.Raw("Sep - Dic 2023"),
				card = card(year = 2023, periodCode = AcademicTermPeriod.SEP_DEC, index = 2.5),
				selected = false
			)
		).map { item -> item.copy(selected = item.key == selectedKey) }
	}

	private fun card(
		year: Int,
		periodCode: AcademicTermPeriod,
		index: Double
	): ShareableTermCardItem = checkNotNull(
		statisticsTerm(year = year, periodCode = periodCode, index = index)
			.toShareableTermCardItem(careerName = "Ingeniería de Computación")
	)

	private fun backgrounds(
		chosenId: String = ShareBackground.included.id,
		all: List<ShareBackground> = listOf(ShareBackground.included, otherShareBackground)
	) = all.map { background ->
		ShareTermBackgroundItem(background = background, selected = background.id == chosenId)
	}

	private fun ImageBitmap.pixels(): IntArray = IntArray(width * height).also { pixels -> readPixels(pixels) }

	// Counted rather than compared whole: a failing array of a million pixels would not fit in the report of the run.
	private fun IntArray.differencesFrom(other: IntArray): Int = indices.count { index -> this[index] != other[index] }

	private companion object {
		const val CARD_WIDTH_PIXELS = 1080
		const val CAPTURE_TIMEOUT_MILLIS = 5_000L

		// A remote artwork that cannot be loaded is asked for before its card is shared, so its failure is waited for too.
		const val REMOTE_TIMEOUT_MILLIS = 10_000L

		const val NEWEST_TERM_KEY = "closed-2024-JAN_MAR"
		const val OLDER_TERM_KEY = "closed-2023-SEP_DEC"
		const val OTHER_CREDIT = "Otra obra"
	}
}
