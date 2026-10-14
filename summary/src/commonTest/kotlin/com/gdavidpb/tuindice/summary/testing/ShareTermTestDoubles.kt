package com.gdavidpb.tuindice.summary.testing

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.domain.model.ShareBackgroundArt
import com.gdavidpb.tuindice.summary.domain.model.ShareableImage
import com.gdavidpb.tuindice.summary.domain.repository.ImageShareRepository
import com.gdavidpb.tuindice.summary.domain.repository.ShareBackgroundRepository
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveShareTermUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveSummaryHubUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.SetShareBackgroundPreferenceUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ShareTermImageUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.ShareTermImageExceptionHandler
import com.gdavidpb.tuindice.summary.presentation.machine.ShareTermMachine
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// A second background of the catalog, with no author to credit: its card credits only its title.
val otherShareBackground = ShareBackground(
	id = "otra-obra",
	title = "Otra obra",
	author = null,
	shortName = "Otra",
	art = ShareBackgroundArt.Bundled
)

class RecordingShareBackgroundRepository(
	private val backgrounds: List<ShareBackground> = listOf(ShareBackground.included, otherShareBackground),
	private var preferredId: String? = null
) : ShareBackgroundRepository {
	val saved = mutableListOf<String>()

	override suspend fun getShareBackgrounds(): List<ShareBackground> = backgrounds

	override suspend fun getPreferredBackgroundId(): String? = preferredId

	override suspend fun setPreferredBackgroundId(id: String) {
		saved += id
		preferredId = id
	}
}

class RecordingShareImageRepository(
	var failure: Throwable? = null
) : ImageShareRepository {
	val shared = mutableListOf<ShareableImage>()

	// The first image handed to the platform, for a test that waits for it.
	val firstImage = CompletableDeferred<ShareableImage>()

	override suspend fun shareImage(image: ShareableImage) {
		failure?.let { throwable -> throw throwable }

		shared += image
		firstImage.complete(image)
	}
}

fun shareTermMachineOf(
	hub: ObserveSummaryHubUseCase = createObserveSummaryHubUseCase(),
	backgrounds: RecordingShareBackgroundRepository = RecordingShareBackgroundRepository(),
	images: RecordingShareImageRepository = RecordingShareImageRepository(),
	user: User = DEFAULT_SUMMARY_USER,
	users: Flow<User> = flowOf(user)
): ShareTermMachine {
	val reporting = RecordingReportingRepository()

	return ShareTermMachine(
		observeShareTermUseCase = ObserveShareTermUseCase(
			observeSummaryHubUseCase = hub,
			userRepository = RecordingUserRepository(users = users),
			shareBackgroundRepository = backgrounds,
			reportingRepository = reporting
		),
		setShareBackgroundPreferenceUseCase = SetShareBackgroundPreferenceUseCase(
			shareBackgroundRepository = backgrounds,
			reportingRepository = reporting
		),
		shareTermImageUseCase = ShareTermImageUseCase(
			imageShareRepository = images,
			reportingRepository = reporting,
			exceptionHandler = ShareTermImageExceptionHandler()
		)
	)
}
