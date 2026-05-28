package com.gdavidpb.tuindice.enrollmentproof.presentation.route

import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.domain.repository.FileOpenerRepository
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.enrollmentproof.domain.exception.EnrollmentProofNotFoundException
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.enrollmentproof.domain.repository.EnrollmentProofRepository
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.FetchEnrollmentProofUseCase
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.exceptionhandler.FetchEnrollmentProofExceptionHandler
import com.gdavidpb.tuindice.enrollmentproof.presentation.action.FetchEnrollmentProofActionProcessor
import com.gdavidpb.tuindice.enrollmentproof.presentation.resource.DefaultEnrollmentProofTextProvider
import com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel.EnrollmentProofViewModel
import com.gdavidpb.tuindice.enrollmentproof.testing.clientRequestException
import com.gdavidpb.tuindice.enrollmentproof.ui.EnrollmentProofUiTags
import com.gdavidpb.tuindice.testkit.base.repository.FakeFileRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import io.ktor.http.HttpStatusCode
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CompletableDeferred
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalTestApi::class)
class EnrollmentProofRouteUiTest {
	@Test
	fun when_fetchSucceeds_then_opensFileAndDismissesSheet() = runTuIndiceUiTest {
		val externalActions = RecordingFileOpenerRepository()
		val viewModel = createEnrollmentProofViewModel()
		var navigateCalls = 0
		var dismissCalls = 0
		val snackBarMessages = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			EnrollmentProofRoute(
				onNavigateToUpdatePassword = { navigateCalls++ },
				onDismissRequest = { dismissCalls++ },
				showSnackBar = { message -> snackBarMessages += message },
				externalActions = externalActions,
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			externalActions.lastOpenedFile != null
		}

		assertNotNull(externalActions.lastOpenedFile)
		assertEquals(0, navigateCalls)
		assertEquals(1, dismissCalls)
		assertEquals(0, snackBarMessages.size)
	}

	@Test
	fun when_fetchSucceedsAfterUserDismisses_then_ignoresLateOpenEffect() = runTuIndiceUiTest {
		val externalActions = RecordingFileOpenerRepository()
		val enrollmentProofResult = CompletableDeferred<EnrollmentProof>()
		val viewModel = createEnrollmentProofViewModel(
			enrollmentProofRepository = object : EnrollmentProofRepository {
				override suspend fun getEnrollmentProof(): EnrollmentProof =
					enrollmentProofResult.await()
			}
		)
		var navigateCalls = 0
		var dismissCalls = 0
		val snackBarMessages = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			EnrollmentProofRoute(
				onNavigateToUpdatePassword = { navigateCalls++ },
				onDismissRequest = { dismissCalls++ },
				showSnackBar = { message -> snackBarMessages += message },
				externalActions = externalActions,
				viewModel = viewModel
			)
		}

		onNodeWithTag(EnrollmentProofUiTags.FetchingCancelButton).performClick()
		assertEquals(1, dismissCalls)

		enrollmentProofResult.complete(
			EnrollmentProof(
				source = "/tmp/enrollment-proof.pdf",
				content = "PDF"
			)
		)
		waitForIdle()

		assertEquals(0, navigateCalls)
		assertEquals(1, dismissCalls)
		assertEquals(0, snackBarMessages.size)
		assertEquals(null, externalActions.lastOpenedFile)
	}

	@Test
	fun when_fetchFailsWithNotFound_then_showsSnackBarAndDismissesSheet() = runTuIndiceUiTest {
		val externalActions = RecordingFileOpenerRepository()
		val viewModel = createEnrollmentProofViewModel(
			throwable = EnrollmentProofNotFoundException()
		)
		var navigateCalls = 0
		var dismissCalls = 0
		val snackBarMessages = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			EnrollmentProofRoute(
				onNavigateToUpdatePassword = { navigateCalls++ },
				onDismissRequest = { dismissCalls++ },
				showSnackBar = { message -> snackBarMessages += message },
				externalActions = externalActions,
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			snackBarMessages.isNotEmpty()
		}

		assertEquals(0, navigateCalls)
		assertEquals(1, dismissCalls)
		assertEquals(1, snackBarMessages.size)
		assertEquals("Comprobante no disponible", snackBarMessages.first().message)
		assertEquals(null, externalActions.lastOpenedFile)
	}

	@Test
	fun when_fetchFailsWithConflict_then_navigatesToUpdatePassword() = runTuIndiceUiTest {
		val externalActions = RecordingFileOpenerRepository()
		val viewModel = createEnrollmentProofViewModel(
			throwable = clientRequestException(HttpStatusCode.Conflict)
		)
		var navigateCalls = 0
		var dismissCalls = 0
		val snackBarMessages = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			EnrollmentProofRoute(
				onNavigateToUpdatePassword = { navigateCalls++ },
				onDismissRequest = { dismissCalls++ },
				showSnackBar = { message -> snackBarMessages += message },
				externalActions = externalActions,
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			navigateCalls > 0
		}

		assertEquals(1, navigateCalls)
		assertEquals(0, dismissCalls)
		assertEquals(0, snackBarMessages.size)
		assertEquals(null, externalActions.lastOpenedFile)
	}

	@Test
	fun when_fetchFailsWithUnsupportedFile_then_showsSnackBarAndDismissesSheet() = runTuIndiceUiTest {
		val externalActions = RecordingFileOpenerRepository()
		val viewModel = createEnrollmentProofViewModel(
			canOpenFile = false
		)
		var navigateCalls = 0
		var dismissCalls = 0
		val snackBarMessages = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			EnrollmentProofRoute(
				onNavigateToUpdatePassword = { navigateCalls++ },
				onDismissRequest = { dismissCalls++ },
				showSnackBar = { message -> snackBarMessages += message },
				externalActions = externalActions,
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			snackBarMessages.isNotEmpty()
		}

		assertEquals(0, navigateCalls)
		assertEquals(1, dismissCalls)
		assertEquals(1, snackBarMessages.size)
		assertEquals("Archivo no soportado ;(", snackBarMessages.first().message)
		assertEquals(null, externalActions.lastOpenedFile)
	}

	@Test
	fun when_fetchFailsWithNoConnectionAndOfflineNetwork_then_showsNetworkSnackBarAndDismissesSheet() = runTuIndiceUiTest {
		val externalActions = RecordingFileOpenerRepository()
		val viewModel = createEnrollmentProofViewModel(
			throwable = IllegalStateException("network unreachable"),
			networkAvailable = false
		)
		var navigateCalls = 0
		var dismissCalls = 0
		val snackBarMessages = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			EnrollmentProofRoute(
				onNavigateToUpdatePassword = { navigateCalls++ },
				onDismissRequest = { dismissCalls++ },
				showSnackBar = { message -> snackBarMessages += message },
				externalActions = externalActions,
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			snackBarMessages.isNotEmpty()
		}

		assertEquals(0, navigateCalls)
		assertEquals(1, dismissCalls)
		assertEquals(1, snackBarMessages.size)
		assertEquals("Comprueba tu conexión", snackBarMessages.first().message)
		assertEquals(null, externalActions.lastOpenedFile)
	}

	@Test
	fun when_fetchFailsWithNoConnectionAndOnlineNetwork_then_showsServiceUnavailableSnackBarAndDismissesSheet() = runTuIndiceUiTest {
		val externalActions = RecordingFileOpenerRepository()
		val viewModel = createEnrollmentProofViewModel(
			throwable = IllegalStateException("network unreachable"),
			networkAvailable = true
		)
		var navigateCalls = 0
		var dismissCalls = 0
		val snackBarMessages = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			EnrollmentProofRoute(
				onNavigateToUpdatePassword = { navigateCalls++ },
				onDismissRequest = { dismissCalls++ },
				showSnackBar = { message -> snackBarMessages += message },
				externalActions = externalActions,
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			snackBarMessages.isNotEmpty()
		}

		assertEquals(0, navigateCalls)
		assertEquals(1, dismissCalls)
		assertEquals(1, snackBarMessages.size)
		assertEquals("Servicio no disponible", snackBarMessages.first().message)
		assertEquals(null, externalActions.lastOpenedFile)
	}

	@Test
	fun when_fetchFailsWithServiceUnavailable_then_showsSnackBarAndDismissesSheet() = runTuIndiceUiTest {
		val externalActions = RecordingFileOpenerRepository()
		val viewModel = createEnrollmentProofViewModel(
			throwable = clientRequestException(HttpStatusCode.ServiceUnavailable)
		)
		var navigateCalls = 0
		var dismissCalls = 0
		val snackBarMessages = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			EnrollmentProofRoute(
				onNavigateToUpdatePassword = { navigateCalls++ },
				onDismissRequest = { dismissCalls++ },
				showSnackBar = { message -> snackBarMessages += message },
				externalActions = externalActions,
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			snackBarMessages.isNotEmpty()
		}

		assertEquals(0, navigateCalls)
		assertEquals(1, dismissCalls)
		assertEquals(1, snackBarMessages.size)
		assertEquals("Servicio no disponible", snackBarMessages.first().message)
		assertEquals(null, externalActions.lastOpenedFile)
	}

	@Test
	fun when_fetchFailsWithTimeout_then_showsTimeoutSnackBarAndDismissesSheet() = runTuIndiceUiTest {
		val externalActions = RecordingFileOpenerRepository()
		val viewModel = createEnrollmentProofViewModel(
			throwable = IllegalStateException("Request timed out")
		)
		var navigateCalls = 0
		var dismissCalls = 0
		val snackBarMessages = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			EnrollmentProofRoute(
				onNavigateToUpdatePassword = { navigateCalls++ },
				onDismissRequest = { dismissCalls++ },
				showSnackBar = { message -> snackBarMessages += message },
				externalActions = externalActions,
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			snackBarMessages.isNotEmpty()
		}

		assertEquals(0, navigateCalls)
		assertEquals(1, dismissCalls)
		assertEquals(1, snackBarMessages.size)
		assertEquals("Tiempo de espera agotado", snackBarMessages.first().message)
		assertEquals(null, externalActions.lastOpenedFile)
	}

	@Test
	fun when_fetchFailsWithUnknownError_then_showsDefaultSnackBarAndDismissesSheet() = runTuIndiceUiTest {
		val externalActions = RecordingFileOpenerRepository()
		val viewModel = createEnrollmentProofViewModel(
			throwable = IllegalStateException("unexpected")
		)
		var navigateCalls = 0
		var dismissCalls = 0
		val snackBarMessages = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			EnrollmentProofRoute(
				onNavigateToUpdatePassword = { navigateCalls++ },
				onDismissRequest = { dismissCalls++ },
				showSnackBar = { message -> snackBarMessages += message },
				externalActions = externalActions,
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			snackBarMessages.isNotEmpty()
		}

		assertEquals(0, navigateCalls)
		assertEquals(1, dismissCalls)
		assertEquals(1, snackBarMessages.size)
		assertEquals("¡Ha ocurrido un error!", snackBarMessages.first().message)
		assertEquals(null, externalActions.lastOpenedFile)
	}

	@Test
	fun when_fetchSucceedsButFileOpenerReturnsFalse_then_stillDismissesSheetWithoutSnackBarOrNavigation() = runTuIndiceUiTest {
		val externalActions = RecordingFileOpenerRepository(openResult = false)
		val viewModel = createEnrollmentProofViewModel()
		var navigateCalls = 0
		var dismissCalls = 0
		val snackBarMessages = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			EnrollmentProofRoute(
				onNavigateToUpdatePassword = { navigateCalls++ },
				onDismissRequest = { dismissCalls++ },
				showSnackBar = { message -> snackBarMessages += message },
				externalActions = externalActions,
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			externalActions.lastOpenedFile != null
		}

		assertNotNull(externalActions.lastOpenedFile)
		assertEquals(0, navigateCalls)
		assertEquals(1, dismissCalls)
		assertEquals(0, snackBarMessages.size)
	}

	private fun createEnrollmentProofViewModel(): EnrollmentProofViewModel {
		return createEnrollmentProofViewModel(throwable = null)
	}

	private fun createEnrollmentProofViewModel(
		throwable: Throwable? = null,
		canOpenFile: Boolean = true,
		networkAvailable: Boolean = true,
		enrollmentProofRepository: EnrollmentProofRepository? = null
	): EnrollmentProofViewModel {
		val resolvedEnrollmentProofRepository = enrollmentProofRepository ?: object : EnrollmentProofRepository {
			override suspend fun getEnrollmentProof(): EnrollmentProof {
				throwable?.let { throw it }
				return EnrollmentProof(
					source = "/tmp/enrollment-proof.pdf",
					content = "PDF"
				)
			}
		}

		val useCase = FetchEnrollmentProofUseCase(
			applicationRepository = FakeFileRepository(canOpenResult = canOpenFile),
			enrollmentProofRepository = resolvedEnrollmentProofRepository,
			reportingRepository = RecordingReportingRepository(),
			exceptionHandler = FetchEnrollmentProofExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = networkAvailable)
			)
		)

		return EnrollmentProofViewModel(
			enrollmentProofActionProcessor = FetchEnrollmentProofActionProcessor(
				enrollmentProofUseCase = useCase,
				textProvider = DefaultEnrollmentProofTextProvider()
			)
		)
	}

	private class RecordingFileOpenerRepository(
		private val openResult: Boolean = true
	) : FileOpenerRepository {
		var lastOpenedFile: PlatformFile? = null

		override fun openFile(file: PlatformFile): Boolean {
			lastOpenedFile = file
			return openResult
		}
	}
}
