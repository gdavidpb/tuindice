package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class TakeProfilePictureUseCase(
	private val applicationRepository: ApplicationRepository
) : FlowUseCase<Unit, String, ProfilePictureError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<String> {
		val outputPath = applicationRepository.createFile("profile_picture.jpg")

		return flowOf(outputPath)
	}
}