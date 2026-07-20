package com.gdavidpb.tuindice.summary.presentation.machine

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import com.gdavidpb.tuindice.summary.testing.RecordingUserRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import io.github.vinceglb.filekit.PlatformFile
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Upload lifecycle against the real machine: the optimistic preview must appear with
 * the in-flight flag, survive the upload ack, and be released only when observation
 * re-emits the new picture identity. UiTest-suffixed for the same reason as the walk:
 * the flow constructs PlatformFile instances the android host JVM stub cannot parse.
 */
class SummaryProfilePictureLifecycleUiTest {
	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun uploadKeepsPreviewAndLoading_untilObservationDeliversNewPictureIdentity() = runTest {
		withMainDispatcher { dispatchers ->
			val users = MutableStateFlow(DEFAULT_SUMMARY_USER)
			val uploadedPicture = ProfilePicture(
				url = "https://cdn.tuindice.app/profile/uploaded.jpg"
			)
			val viewModel = createViewModel(
				userRepository = RecordingUserRepository(
					users = users,
					profilePicture = uploadedPicture
				),
				dispatchers = dispatchers
			)
			val stateCollector = backgroundScope.launchStateCollector(
				flow = viewModel.state,
				testScheduler = testScheduler
			)

			try {
				withTimeout(3_000) {
					viewModel.state
						.filterIsInstance<Summary.State.Content>()
						.first()
				}

				viewModel.uploadProfilePictureAction(
					file = PlatformFile("/tmp/profile-preview.jpg")
				)

				val uploading = withTimeout(3_000) {
					viewModel.state
						.filterIsInstance<Summary.State.Content>()
						.first { state -> state.profilePictureLocalPreview != null }
				}
				assertTrue(uploading.isProfilePictureLoading)
				assertEquals(DEFAULT_SUMMARY_USER.pictureUrl, uploading.profilePictureUrl)

				users.value = DEFAULT_SUMMARY_USER.copy(
					pictureUrl = uploadedPicture.url,
					pictureVersion = 1
				)

				val converged = withTimeout(3_000) {
					viewModel.state
						.filterIsInstance<Summary.State.Content>()
						.first { state -> state.profilePictureUrl == uploadedPicture.url }
				}
				assertEquals(false, converged.isProfilePictureLoading)
				assertNull(converged.profilePictureLocalPreview)
				assertEquals(1, converged.profilePictureVersion)
			} finally {
				stateCollector.cancel()
			}
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun signedUrlRotation_keepsPreviewAndLoading_whileUploadIsPending() = runTest {
		withMainDispatcher { dispatchers ->
			val picturePath = "https://storage.googleapis.com/tuindice/profile_pictures/uid.jpg"
			val signedUser = DEFAULT_SUMMARY_USER.copy(
				pictureUrl = "$picturePath?X-Goog-Signature=aaa"
			)
			val users = MutableStateFlow(signedUser)
			val viewModel = createViewModel(
				userRepository = RecordingUserRepository(
					users = users,
					profilePicture = ProfilePicture(url = "$picturePath?X-Goog-Signature=ccc")
				),
				dispatchers = dispatchers
			)
			val stateCollector = backgroundScope.launchStateCollector(
				flow = viewModel.state,
				testScheduler = testScheduler
			)

			try {
				withTimeout(3_000) {
					viewModel.state
						.filterIsInstance<Summary.State.Content>()
						.first()
				}

				viewModel.uploadProfilePictureAction(
					file = PlatformFile("/tmp/profile-preview.jpg")
				)

				withTimeout(3_000) {
					viewModel.state
						.filterIsInstance<Summary.State.Content>()
						.first { state -> state.profilePictureLocalPreview != null }
				}

				// A backend re-fetch rotates only the signature: same picture identity,
				// so the optimistic preview and the in-flight flag must survive.
				users.value = signedUser.copy(
					pictureUrl = "$picturePath?X-Goog-Signature=bbb"
				)

				val rotated = withTimeout(3_000) {
					viewModel.state
						.filterIsInstance<Summary.State.Content>()
						.first { state -> state.profilePictureUrl.endsWith("bbb") }
				}
				assertTrue(rotated.isProfilePictureLoading)
				assertNotNull(rotated.profilePictureLocalPreview)

				users.value = signedUser.copy(
					pictureUrl = "$picturePath?X-Goog-Signature=ccc",
					pictureVersion = 1
				)

				val converged = withTimeout(3_000) {
					viewModel.state
						.filterIsInstance<Summary.State.Content>()
						.first { state -> state.profilePictureVersion == 1 }
				}
				assertEquals(false, converged.isProfilePictureLoading)
				assertNull(converged.profilePictureLocalPreview)
			} finally {
				stateCollector.cancel()
			}
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun uploadFailure_releasesPreview_andKeepsPreviousPicture() = runTest {
		withMainDispatcher { dispatchers ->
			val users = MutableStateFlow(DEFAULT_SUMMARY_USER)
			val viewModel = createViewModel(
				userRepository = RecordingUserRepository(
					users = users,
					throwable = clientRequestException(
						statusCode = HttpStatusCode.InternalServerError,
						path = "/users/v1/picture"
					)
				),
				dispatchers = dispatchers
			)
			val stateCollector = backgroundScope.launchStateCollector(
				flow = viewModel.state,
				testScheduler = testScheduler
			)

			try {
				withTimeout(3_000) {
					viewModel.state
						.filterIsInstance<Summary.State.Content>()
						.first()
				}

				val failureSnackBar = async {
					viewModel.effect
						.filterIsInstance<Summary.Effect.ShowSnackBar>()
						.first()
				}

				viewModel.uploadProfilePictureAction(
					file = PlatformFile("/tmp/profile-preview.jpg")
				)

				withTimeout(3_000) { failureSnackBar.await() }

				val rolledBack = withTimeout(3_000) {
					viewModel.state
						.filterIsInstance<Summary.State.Content>()
						.first { state ->
							!state.isProfilePictureLoading &&
								state.profilePictureLocalPreview == null
						}
				}
				assertEquals(DEFAULT_SUMMARY_USER.pictureUrl, rolledBack.profilePictureUrl)
				assertEquals(DEFAULT_SUMMARY_USER.pictureVersion, rolledBack.profilePictureVersion)
			} finally {
				stateCollector.cancel()
			}
		}
	}

	private fun createViewModel(
		userRepository: RecordingUserRepository,
		dispatchers: TuIndiceDispatchers
	): SummaryViewModel {
		return SummaryViewModel(
			screenMachine = SummaryMachine(
				observeUserUseCase = ObserveUserUseCase(
					userRepository = userRepository,
					reportingRepository = RecordingReportingRepository()
				),
				updateUserUseCase = UpdateUserUseCase(
					userRepository = userRepository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = UpdateUserExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true)
					)
				),
				uploadProfilePictureUseCase = UploadProfilePictureUseCase(
					userRepository = userRepository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = UploadProfilePictureExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true)
					)
				),
				removeProfilePictureUseCase = RemoveProfilePictureUseCase(
					userRepository = userRepository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = RemoveProfilePictureExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true)
					)
				)
			),
			eventPublisher = NoOpEventPublisher,
			dispatchers = dispatchers
		)
	}
}
