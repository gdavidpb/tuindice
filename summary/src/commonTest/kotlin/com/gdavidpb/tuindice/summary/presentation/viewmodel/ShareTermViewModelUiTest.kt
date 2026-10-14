package com.gdavidpb.tuindice.summary.presentation.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.summary.presentation.contract.ShareTerm
import com.gdavidpb.tuindice.summary.testing.ControllableRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.summary.testing.ControllableVisibleAcademicRecordRepository
import com.gdavidpb.tuindice.summary.testing.RecordingShareImageRepository
import com.gdavidpb.tuindice.summary.testing.createObserveSummaryHubUseCase
import com.gdavidpb.tuindice.summary.testing.shareTermMachineOf
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

// UiTest-suffixed: the picture is encoded with the platform's own encoder, so these run on the iOS gate.
@OptIn(ExperimentalCoroutinesApi::class)
class ShareTermViewModelUiTest {
	@Test
	fun when_theCardIsShared_then_itsPngReachesTheSystemSheet() = runTest {
		withMainDispatcher { dispatchers ->
			val images = RecordingShareImageRepository()
			val viewModel = createShareViewModel(dispatchers, images)
			openSheet(viewModel)

			viewModel.shareImageAction(bitmap = ImageBitmap(width = WIDTH, height = HEIGHT))

			val image = withTimeout(TIMEOUT_MILLIS) { images.firstImage.await() }
			assertEquals("mi-trimestre.png", image.fileName)
			assertContentEquals(PNG_SIGNATURE, image.content.copyOf(PNG_SIGNATURE.size))
		}
	}

	@Test
	fun when_theImageCannotBeShared_then_theSheetReportsItAsAFailure() = runTest {
		withMainDispatcher { dispatchers ->
			val images = RecordingShareImageRepository(
				failure = IllegalStateException("La hoja no se pudo mostrar.")
			)
			val viewModel = createShareViewModel(dispatchers, images)
			openSheet(viewModel)

			viewModel.shareImageAction(bitmap = ImageBitmap(width = WIDTH, height = HEIGHT))

			val effect = withTimeout(TIMEOUT_MILLIS) { viewModel.effect.first() }
			assertEquals(ShareTerm.Effect.ImageShareFailed, effect)
		}
	}

	private suspend fun openSheet(viewModel: ShareTermViewModel) {
		viewModel.observeShareTermAction(termKey = null)

		withTimeout(TIMEOUT_MILLIS) {
			viewModel.state.filterIsInstance<ShareTerm.State.Content>().first()
		}
	}

	private fun createShareViewModel(
		dispatchers: TuIndiceDispatchers,
		images: RecordingShareImageRepository
	): ShareTermViewModel {
		return ShareTermViewModel(
			screenMachine = shareTermMachineOf(
				hub = createObserveSummaryHubUseCase(
					prerequisite = ControllableRecordDataPrerequisiteRepository(isReady = true),
					record = ControllableVisibleAcademicRecordRepository()
				),
				images = images
			),
			eventPublisher = NoOpEventPublisher,
			dispatchers = dispatchers
		)
	}

	private companion object {
		const val TIMEOUT_MILLIS = 5_000L
		const val WIDTH = 4
		const val HEIGHT = 3
		val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
	}
}
