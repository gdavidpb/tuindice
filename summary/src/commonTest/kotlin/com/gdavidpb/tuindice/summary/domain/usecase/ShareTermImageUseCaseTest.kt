package com.gdavidpb.tuindice.summary.domain.usecase

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

class ShareTermImageUseCaseTest {
	@Test
	fun when_theCardIsShared_then_itsPngGoesToTheSheetUnderANameWithoutPersonalData() = runTest {
		val repository = RecordingImageShareRepository()

		val states = useCase(repository).execute(PNG_BYTES).toList()

		assertEquals(UseCaseState.Data(Unit), states.last())
		val image = repository.shared.single()
		assertEquals("mi-trimestre.png", image.fileName)
		assertContentEquals(PNG_BYTES, image.content)
	}

	@Test
	fun when_theSheetCannotBeShown_then_theImageIsUnavailable() = runTest {
		val repository = RecordingImageShareRepository().apply {
			failure = IllegalStateException("La hoja no se pudo mostrar.")
		}

		val states = useCase(repository).execute(PNG_BYTES).toList()

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
		// The first bytes of a PNG, which is what the card's capture hands over.
		val PNG_BYTES = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
	}
}
