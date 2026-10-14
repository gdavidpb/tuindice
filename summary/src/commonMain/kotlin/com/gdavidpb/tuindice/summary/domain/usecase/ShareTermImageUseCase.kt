package com.gdavidpb.tuindice.summary.domain.usecase

import androidx.compose.ui.graphics.ImageBitmap
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.summary.domain.model.ShareableImage
import com.gdavidpb.tuindice.summary.domain.repository.ImageShareRepository
import com.gdavidpb.tuindice.summary.domain.usecase.error.ShareTermImageUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.ShareTermImageExceptionHandler
import com.gdavidpb.tuindice.summary.ui.image.toPngBytes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Hands the captured term card to the platform's share sheet as a PNG.
 *
 * The bitmap is encoded here, so the image that the sheet receives is the one the student saw.
 */
class ShareTermImageUseCase(
	private val imageShareRepository: ImageShareRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: ShareTermImageExceptionHandler
) : FlowUseCase<ImageBitmap, Unit, ShareTermImageUseCaseError>() {
	override suspend fun executeOnBackground(params: ImageBitmap): Flow<Unit> {
		val image = ShareableImage(
			content = params.toPngBytes(),
			fileName = FILE_NAME
		)

		imageShareRepository.shareImage(image)

		return flowOf(Unit)
	}

	private companion object {
		// The receiving app sees this name, so it carries no name of the student and no code.
		const val FILE_NAME = "mi-trimestre.png"
	}
}
