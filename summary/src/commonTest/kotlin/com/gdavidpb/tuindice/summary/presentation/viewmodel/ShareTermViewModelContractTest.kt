package com.gdavidpb.tuindice.summary.presentation.viewmodel

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.contract.ShareTerm
import com.gdavidpb.tuindice.summary.presentation.model.ShareCardPicture
import com.gdavidpb.tuindice.summary.testing.ControllableRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.summary.testing.ControllableVisibleAcademicRecordRepository
import com.gdavidpb.tuindice.summary.testing.RecordingShareBackgroundRepository
import com.gdavidpb.tuindice.summary.testing.RecordingShareImageRepository
import com.gdavidpb.tuindice.summary.testing.createObserveSummaryHubUseCase
import com.gdavidpb.tuindice.summary.testing.hubClosedTerm
import com.gdavidpb.tuindice.summary.testing.hubPassedAttempt
import com.gdavidpb.tuindice.summary.testing.hubRecord
import com.gdavidpb.tuindice.summary.testing.otherShareBackground
import com.gdavidpb.tuindice.summary.testing.shareTermMachineOf
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.share_term_unavailable_title
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShareTermViewModelContractTest {
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theRecordHasClosedTerms_then_theNewestOneIsSelected_withItsIndex() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createShareViewModel(dispatchers, record = closedRecord())

			val content = openedContent(viewModel)

			assertEquals(listOf(INDEX_NEWEST, INDEX_OLDEST), content.terms.map { item -> item.card.index })
			assertEquals(listOf(true, false), content.terms.map { item -> item.selected })
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theSheetOpensOnAnOlderTerm_then_thatTermIsSelected() = runTest {
		withMainDispatcher { dispatchers ->
			val keys = openedContent(createShareViewModel(dispatchers, record = closedRecord()))
				.terms.map { item -> item.key }
			val viewModel = createShareViewModel(dispatchers, record = closedRecord())

			val content = openedContent(viewModel, termKey = keys.last())

			assertEquals(keys.last(), content.terms.single { item -> item.selected }.key)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theSheetOpensOnATermThatIsNotThere_then_theNewestOneIsSelected() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createShareViewModel(dispatchers, record = closedRecord())

			val content = openedContent(viewModel, termKey = "a-term-that-is-not-there")

			assertEquals(INDEX_NEWEST, content.terms.single { item -> item.selected }.card.index)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_noTermHasClosed_then_thereIsNoTermToShare_andTheBackgroundsAreThere() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createShareViewModel(dispatchers, record = hubRecord())

			val content = openedContent(viewModel)

			assertTrue(content.terms.isEmpty())
			assertEquals(2, content.backgrounds.size)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_aTermIsChosen_then_itIsSelected_andAnUnknownOneChangesNothing() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createShareViewModel(dispatchers, record = closedRecord())
			val keys = openedContent(viewModel).terms.map { item -> item.key }

			viewModel.selectTermAction(termKey = "a-term-that-is-not-there")
			viewModel.selectTermAction(termKey = keys.last())

			val content = withTimeout(TIMEOUT_MILLIS) {
				viewModel.state.filterIsInstance<ShareTerm.State.Content>()
					.first { state -> state.terms.single { item -> item.selected }.key == keys.last() }
			}
			assertEquals(keys.last(), content.terms.single { item -> item.selected }.key)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_aBackgroundIsChosen_then_itIsSelected_andKeptAsThePreference() = runTest {
		withMainDispatcher { dispatchers ->
			val backgrounds = RecordingShareBackgroundRepository()
			val viewModel = createShareViewModel(dispatchers, record = closedRecord(), backgrounds = backgrounds)
			openedContent(viewModel)

			viewModel.selectBackgroundAction(backgroundId = otherShareBackground.id)

			val content = withTimeout(TIMEOUT_MILLIS) {
				viewModel.state.filterIsInstance<ShareTerm.State.Content>().first { state ->
					state.backgrounds.single { item -> item.selected }.background.id == otherShareBackground.id
				}
			}
			assertEquals(otherShareBackground.id, content.backgrounds.single { item -> item.selected }.background.id)
			assertEquals(listOf(otherShareBackground.id), backgrounds.saved)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theTermsCannotBeRead_then_theSheetSaysSo() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = ShareTermViewModel(
				screenMachine = shareTermMachineOf(users = flow { throw IllegalStateException("the record is locked") }),
				eventPublisher = NoOpEventPublisher,
				dispatchers = dispatchers
			)

			viewModel.observeShareTermAction(termKey = null)

			val unavailable = withTimeout(TIMEOUT_MILLIS) {
				viewModel.state.filterIsInstance<ShareTerm.State.Unavailable>().first()
			}
			assertEquals(UiText.Resource(Res.string.share_term_unavailable_title), unavailable.title)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theCardIsShared_then_thePictureEncodedAsPngIsWhatReachesTheShare() = runTest {
		withMainDispatcher { dispatchers ->
			val images = RecordingShareImageRepository()
			val viewModel = createShareViewModel(dispatchers, record = closedRecord(), images = images)
			openedContent(viewModel)

			viewModel.shareImageAction(picture = ShareCardPicture { PNG_BYTES })

			val image = withTimeout(TIMEOUT_MILLIS) { images.firstImage.await() }
			assertContentEquals(PNG_BYTES, image.content)
		}
	}

	private suspend fun openedContent(
		viewModel: ShareTermViewModel,
		termKey: String? = null
	): ShareTerm.State.Content {
		viewModel.observeShareTermAction(termKey = termKey)

		return withTimeout(TIMEOUT_MILLIS) {
			viewModel.state.filterIsInstance<ShareTerm.State.Content>().first()
		}
	}

	private fun createShareViewModel(
		dispatchers: TuIndiceDispatchers,
		record: AcademicRecord?,
		backgrounds: RecordingShareBackgroundRepository = RecordingShareBackgroundRepository(),
		images: RecordingShareImageRepository = RecordingShareImageRepository()
	): ShareTermViewModel {
		return ShareTermViewModel(
			screenMachine = shareTermMachineOf(
				hub = createObserveSummaryHubUseCase(
					record = ControllableVisibleAcademicRecordRepository(initialRecord = record),
					prerequisite = ControllableRecordDataPrerequisiteRepository(isReady = true)
				),
				backgrounds = backgrounds,
				images = images
			),
			eventPublisher = NoOpEventPublisher,
			dispatchers = dispatchers
		)
	}

	// Two closed terms with an index: the older at 3.0000 and the newer at 4.0000, as the card writes them.
	private fun closedRecord(): AcademicRecord {
		return hubRecord(
			hubClosedTerm(
				year = 2024,
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
		const val TIMEOUT_MILLIS = 3_000L
		const val INDEX_NEWEST = "4.0000"
		const val INDEX_OLDEST = "3.0000"
		val PNG_BYTES = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
	}
}
