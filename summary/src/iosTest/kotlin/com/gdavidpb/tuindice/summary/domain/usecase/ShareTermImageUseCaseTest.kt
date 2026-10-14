package com.gdavidpb.tuindice.summary.domain.usecase

import androidx.compose.ui.graphics.ImageBitmap
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.summary.domain.model.ShareableImage
import com.gdavidpb.tuindice.summary.domain.repository.ImageShareRepository
import com.gdavidpb.tuindice.summary.domain.usecase.error.ShareTermImageUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.ShareTermImageExceptionHandler
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

// It runs on iOS because an ImageBitmap is only a bitmap where the platform draws it: the Android host has no
// bitmap to hand the use case, and the encoding of the bitmap has its own test there.
class ShareTermImageUseCaseTest {
	@Test
	fun when_theCardIsShared_then_itGoesToTheSheetAsAPngWithoutPersonalData() = runTest {
		val repository = RecordingImageShareRepository()

		val states = useCase(repository).execute(ImageBitmap(width = 2, height = 2)).toList()

		assertEquals(UseCaseState.Data(Unit), states.last())
		val image = repository.shared.single()
		assertEquals("mi-trimestre.png", image.fileName)
		assertContentEquals(PNG_SIGNATURE, image.content.copyOf(PNG_SIGNATURE.size))
	}

	@Test
	fun when_theSheetCannotBeShown_then_theImageIsUnavailable() = runTest {
		val repository = RecordingImageShareRepository().apply {
			failure = IllegalStateException("La hoja no se pudo mostrar.")
		}

		val states = useCase(repository).execute(ImageBitmap(width = 2, height = 2)).toList()

		assertEquals(
			UseCaseState.Error(ShareTermImageUseCaseError.ImageUnavailable),
			states.last()
		)
	}

	private fun useCase(repository: ImageShareRepository) = ShareTermImageUseCase(
		imageShareRepository = repository,
		reportingRepository = RecordingReportingRepository(),
		exceptionHandler = ShareTermImageExceptionHandler()
	)

	private class RecordingImageShareRepository : ImageShareRepository {
		val shared = mutableListOf<ShareableImage>()
		var failure: Throwable? = null

		override suspend fun shareImage(image: ShareableImage) {
			failure?.let { throwable -> throw throwable }

			shared += image
		}
	}

	private companion object {
		// The first bytes of every PNG: the signature that says what the sheet is handed.
		val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
	}
}
