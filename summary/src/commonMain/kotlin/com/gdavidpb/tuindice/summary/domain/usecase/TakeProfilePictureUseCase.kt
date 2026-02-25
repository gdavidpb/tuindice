package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.repository.FileRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class TakeProfilePictureUseCase(
	private val applicationRepository: FileRepository
) : FlowUseCase<Unit, PlatformFileRef, ProfilePictureUseCaseError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<PlatformFileRef> {
		val outputPath = applicationRepository.createTemporaryFile("profile_picture.jpg")

		return flowOf(outputPath)
	}
}
