package com.gdavidpb.tuindice.summary.testing

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.summary.domain.model.ShareBackground
import com.gdavidpb.tuindice.summary.domain.model.ShareBackgroundArt
import com.gdavidpb.tuindice.summary.domain.model.ShareableImage
import com.gdavidpb.tuindice.summary.domain.repository.ImageShareRepository
import com.gdavidpb.tuindice.summary.domain.repository.ShareBackgroundRepository
import com.gdavidpb.tuindice.summary.domain.repository.ShareBackgroundSelectionRepository
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveShareTermUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveSummaryHubUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.SetShareBackgroundPreferenceUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ShareTermImageUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateShareBackgroundsUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.ShareTermImageExceptionHandler
import com.gdavidpb.tuindice.summary.presentation.machine.ShareTermMachine
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

// A second background of the catalog, with no author to credit: its card credits only its title.
val otherShareBackground = ShareBackground(
	id = "otra-obra",
	title = "Otra obra",
	author = null,
	shortName = "Otra",
	art = ShareBackgroundArt.Bundled
)

// A remote background on a port nothing listens on, so its artwork fails to load at once.
val unreachableShareBackground = ShareBackground(
	id = "espejo-solar",
	title = "Espejo Solar",
	author = "Alejandro Otero",
	shortName = "Espejo",
	art = ShareBackgroundArt.Remote(
		cardUrl = "http://127.0.0.1:9/espejo-solar/v1/card.webp",
		thumbUrl = "http://127.0.0.1:9/espejo-solar/v1/thumb.webp"
	)
)

// The catalog a test serves and the choice the student made. The catalog is a flow, so a test can make a refresh land
// while the sheet is open. [refreshes] counts the asks that the sheet made of the server.
class RecordingShareBackgroundRepository(
	initialCatalog: List<ShareBackground> = listOf(ShareBackground.included, otherShareBackground),
	preferredId: String? = null
) : ShareBackgroundRepository, ShareBackgroundSelectionRepository {
	val catalog = MutableStateFlow(initialCatalog)
	val saved = mutableListOf<String>()
	var refreshes = 0

	// Completes at the first ask of the server, for a test that waits for the sheet to make it.
	val firstRefresh = CompletableDeferred<Unit>()

	private val preferred = MutableStateFlow(preferredId)

	override fun observeShareBackgrounds(): Flow<List<ShareBackground>> = catalog

	override suspend fun refreshShareBackgrounds() {
		refreshes++
		firstRefresh.complete(Unit)
	}

	override fun observePreferredBackgroundId(): Flow<String?> = preferred

	override suspend fun setPreferredBackgroundId(id: String) {
		saved += id
		preferred.value = id
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
			shareBackgroundSelectionRepository = backgrounds,
			reportingRepository = reporting
		),
		updateShareBackgroundsUseCase = UpdateShareBackgroundsUseCase(
			shareBackgroundRepository = backgrounds,
			reportingRepository = reporting
		),
		setShareBackgroundPreferenceUseCase = SetShareBackgroundPreferenceUseCase(
			shareBackgroundSelectionRepository = backgrounds,
			reportingRepository = reporting
		),
		shareTermImageUseCase = ShareTermImageUseCase(
			imageShareRepository = images,
			reportingRepository = reporting,
			exceptionHandler = ShareTermImageExceptionHandler()
		)
	)
}
