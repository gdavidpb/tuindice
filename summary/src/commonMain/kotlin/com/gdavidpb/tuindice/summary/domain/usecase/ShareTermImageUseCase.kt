package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.summary.domain.model.ShareableImage
import com.gdavidpb.tuindice.summary.domain.repository.ImageShareRepository
import com.gdavidpb.tuindice.summary.domain.usecase.error.ShareTermImageUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.ShareTermImageExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Hands the captured term card, already encoded as a PNG by whoever captured it, to the platform's share sheet under a
 * name that says nothing of the student.
 */
class ShareTermImageUseCase(
	private val imageShareRepository: ImageShareRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: ShareTermImageExceptionHandler
) : FlowUseCase<ByteArray, Unit, ShareTermImageUseCaseError>() {
	override suspend fun executeOnBackground(params: ByteArray): Flow<Unit> {
		val image = ShareableImage(
			content = params,
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
