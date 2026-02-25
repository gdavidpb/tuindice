package com.gdavidpb.tuindice.summary.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.model.EncodedImage
import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.model.PlatformUri
import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.base.domain.repository.FileRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import com.gdavidpb.tuindice.summary.domain.repository.EncoderRepository
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.domain.usecase.GetUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.TakeProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.GetUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.validator.UploadProfilePictureParamsValidator
import com.gdavidpb.tuindice.summary.presentation.action.ConfirmRemoveProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.LoadSummaryActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.OpenProfilePictureSettingsActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.PickProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.RemoveProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.TakeProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.UploadProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.resource.SummaryTextProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class SummaryViewModelTest {
	@Test
	fun initialLoadSummaryAction_setsContentState() = runBlocking {
		val userRepository = SummaryViewModelFakeUserRepository()
		val viewModel = createViewModel(userRepository = userRepository)
		val stateJob = launch { viewModel.state.collect() }

		try {
			waitUntil { viewModel.state.value is Summary.State.Content }

			val content = assertIs<Summary.State.Content>(viewModel.state.value)
			assertEquals("Ada Lovelace", content.name)
			assertEquals("updated-1700000000000", content.lastUpdate)
			assertEquals("https://tuindice.app/old-picture.jpg", content.profilePictureUrl)
			assertEquals(true, content.isGradeVisible)
		} finally {
			stateJob.cancel()
		}
	}

	@Test
	fun uploadTakenProfilePictureAction_updatesPictureAndShowsSnackBar() = runBlocking {
		val userRepository = SummaryViewModelFakeUserRepository()
		val encoderRepository = SummaryViewModelFakeEncoderRepository()
		val viewModel = createViewModel(
			userRepository = userRepository,
			encoderRepository = encoderRepository
		)
		val effects = mutableListOf<Summary.Effect>()
		val effectJob = launch { viewModel.effect.collect { effects += it } }
		val stateJob = launch { viewModel.state.collect() }

		try {
			waitUntil { viewModel.state.value is Summary.State.Content }

			viewModel.setCameraOutput(PlatformFileRef("/tmp/new-profile-picture.jpg"))
			viewModel.uploadTakenProfilePictureAction()

			waitUntil {
				val state = viewModel.state.value
				state is Summary.State.Content &&
					state.profilePictureUrl == "https://tuindice.app/new-picture.jpg" &&
					!state.isProfilePictureLoading &&
					effects.any { it is Summary.Effect.ShowSnackBar }
			}

			assertEquals(PlatformUri("/tmp/new-profile-picture.jpg"), encoderRepository.lastUri)
			val snackBar = effects.filterIsInstance<Summary.Effect.ShowSnackBar>().last()
			assertEquals("Profile picture updated", snackBar.message)
		} finally {
			effectJob.cancel()
			stateJob.cancel()
		}
	}

	@Test
	fun takeProfilePictureAction_emitsOpenCameraEffect() = runBlocking {
		val viewModel = createViewModel()
		val effects = mutableListOf<Summary.Effect>()
		val effectJob = launch { viewModel.effect.collect { effects += it } }
		val stateJob = launch { viewModel.state.collect() }

		try {
			waitUntil { viewModel.state.value is Summary.State.Content }

			viewModel.takeProfilePictureAction()

			waitUntil { effects.any { it is Summary.Effect.OpenCamera } }
			val camera = effects.filterIsInstance<Summary.Effect.OpenCamera>().single()
			assertEquals(PlatformFileRef("/tmp/profile_picture.jpg"), camera.output)
		} finally {
			effectJob.cancel()
			stateJob.cancel()
		}
	}

	private fun createViewModel(
		userRepository: SummaryViewModelFakeUserRepository = SummaryViewModelFakeUserRepository(),
		encoderRepository: SummaryViewModelFakeEncoderRepository = SummaryViewModelFakeEncoderRepository()
	): SummaryViewModel {
		return SummaryViewModel(
			loadSummaryActionProcessor = LoadSummaryActionProcessor(
				getUserUseCase = GetUserUseCase(
					userRepository = userRepository,
					exceptionHandler = GetUserExceptionHandler(
						networkRepository = SummaryViewModelFakeNetworkStatusGateway(),
						reportingRepository = SummaryViewModelFakeReportingGateway
					)
				),
				textProvider = SummaryViewModelFakeTextProvider
			),
			takeProfilePictureActionProcessor = TakeProfilePictureActionProcessor(
				takeProfilePictureUseCase = TakeProfilePictureUseCase(
					applicationRepository = SummaryViewModelFakeFileGateway()
				),
				textProvider = SummaryViewModelFakeTextProvider
			),
			pickProfilePictureActionProcessor = PickProfilePictureActionProcessor(),
			uploadProfilePictureActionProcessor = UploadProfilePictureActionProcessor(
				uploadProfilePictureUseCase = UploadProfilePictureUseCase(
					userRepository = userRepository,
					encoderRepository = encoderRepository,
					paramsValidator = UploadProfilePictureParamsValidator(),
					exceptionHandler = UploadProfilePictureExceptionHandler(
						networkRepository = SummaryViewModelFakeNetworkStatusGateway(),
						reportingRepository = SummaryViewModelFakeReportingGateway
					)
				),
				textProvider = SummaryViewModelFakeTextProvider
			),
			confirmRemoveProfilePictureActionProcessor = ConfirmRemoveProfilePictureActionProcessor(
				removeProfilePictureUseCase = RemoveProfilePictureUseCase(
					userRepository = userRepository,
					exceptionHandler = RemoveProfilePictureExceptionHandler(
						networkRepository = SummaryViewModelFakeNetworkStatusGateway(),
						reportingRepository = SummaryViewModelFakeReportingGateway
					)
				),
				textProvider = SummaryViewModelFakeTextProvider
			),
			removeProfilePictureActionProcessor = RemoveProfilePictureActionProcessor(),
			openProfilePictureSettingsActionProcessor = OpenProfilePictureSettingsActionProcessor()
		)
	}

	private suspend fun waitUntil(
		timeoutMs: Long = 2_000L,
		condition: () -> Boolean
	) {
		val mark = TimeSource.Monotonic.markNow()

		while (!condition() && mark.elapsedNow() < timeoutMs.milliseconds) {
			delay(20)
		}

		assertTrue(condition(), "Condition not reached within timeout.")
	}
}

private object SummaryViewModelFakeTextProvider : SummaryTextProvider {
	override fun lastUpdate(lastUpdate: Long): String = "updated-$lastUpdate"
	override fun serviceUnavailable(): String = "Service unavailable"
	override fun networkUnavailable(): String = "Network unavailable"
	override fun timeout(): String = "Timeout"
	override fun noService(): String = "No service"
	override fun defaultError(): String = "Default error"
	override fun profilePictureUpdated(): String = "Profile picture updated"
	override fun profilePictureRemoved(): String = "Profile picture removed"
}

private class SummaryViewModelFakeUserRepository : UserRepository {
	override suspend fun getUserFlow(): Flow<User> {
		return flowOf(
			User(
				id = "user-1",
				cid = "V12345",
				usbId = "20-32000",
				email = "ada@tuindice.app",
				pictureUrl = "https://tuindice.app/old-picture.jpg",
				fullName = "Ada Lovelace",
				firstNames = "Ada Augusta",
				lastNames = "Lovelace Byron",
				careerName = "Computer Science",
				careerCode = 10,
				scholarship = false,
				grade = 3.84,
				enrolledSubjects = 5,
				enrolledCredits = 20,
				approvedSubjects = 20,
				approvedCredits = 80,
				retiredSubjects = 1,
				retiredCredits = 4,
				failedSubjects = 0,
				failedCredits = 0,
				lastUpdate = 1_700_000_000_000L
			)
		)
	}

	override suspend fun uploadProfilePicture(content: ByteArray, mimeType: String): ProfilePicture {
		return ProfilePicture(url = "https://tuindice.app/new-picture.jpg")
	}

	override suspend fun removeProfilePicture() = Unit
}

private class SummaryViewModelFakeEncoderRepository : EncoderRepository {
	var lastUri: PlatformUri? = null

	override suspend fun encodePicture(uri: PlatformUri): EncodedImage {
		lastUri = uri
		return EncodedImage(
			content = byteArrayOf(1, 2, 3),
			mimeType = "image/jpeg"
		)
	}
}

private class SummaryViewModelFakeFileGateway : FileRepository {
	override suspend fun createTemporaryFile(nameHint: String): PlatformFileRef {
		return PlatformFileRef("/tmp/profile_picture.jpg")
	}

	override suspend fun canOpen(fileRef: PlatformFileRef): Boolean = true
}

private class SummaryViewModelFakeNetworkStatusGateway : NetworkRepository {
	override fun isAvailable(): Boolean = true
}

private object SummaryViewModelFakeReportingGateway : ReportingRepository {
	override fun setIdentifier(identifier: String) = Unit
	override fun logException(throwable: Throwable) = Unit
	override fun logMessage(message: String) = Unit
	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}
