package com.gdavidpb.tuindice.enrollmentproof.presentation.navigation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.repository.FileOpenerRepository
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.navigation.NavResult
import com.gdavidpb.tuindice.base.presentation.navigation.NavShellBindings
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavActions
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.enrollmentproof.domain.repository.EnrollmentProofRepository
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.FetchEnrollmentProofUseCase
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.exceptionhandler.FetchEnrollmentProofExceptionHandler
import com.gdavidpb.tuindice.enrollmentproof.presentation.machine.EnrollmentProofMachine
import com.gdavidpb.tuindice.enrollmentproof.presentation.resource.DefaultEnrollmentProofTextProvider
import com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel.EnrollmentProofViewModel
import com.gdavidpb.tuindice.enrollmentproof.testing.clientRequestException
import com.gdavidpb.tuindice.enrollmentproof.ui.EnrollmentProofUiTags
import com.gdavidpb.tuindice.testkit.base.repository.FakeFileRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import io.github.vinceglb.filekit.PlatformFile
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.awaitCancellation
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * The dialog entry as the host mounts it: what it asks of the navigator when it is cancelled, when
 * the fetch fails, and when the failure snackbar's retry is tapped afterwards.
 */
@OptIn(ExperimentalTestApi::class)
class EnrollmentProofEntriesUiTest {
	@Test
	fun when_retryIsTappedAfterTheDialogDismissedItself_then_itOnlyPushesTheDialogAgain() = runTuIndiceUiTest {
		val navActions = RecordingNavActions()
		val snackBarMessages = mutableListOf<SnackBarMessage>()

		withEntriesKoin(
			getEnrollmentProof = { throw clientRequestException(HttpStatusCode.ServiceUnavailable) }
		) {
			setEnrollmentProofDialogEntry(navActions = navActions, snackBarMessages = snackBarMessages)

			waitUntil(timeoutMillis = 2_000) { snackBarMessages.isNotEmpty() }

			// The dialog left the stack on its own as it reported the failure.
			assertEquals(listOf("pop"), navActions.calls)

			val retry = assertNotNull(snackBarMessages.single().onAction)
			runOnIdle { retry() }

			// A second pop here would not close the dialog, already gone: it would take the back
			// step of whatever is on top, and at a tab root that step leaves for the start tab.
			assertEquals(
				listOf("pop", "push:${EnrollmentProofDestination.EnrollmentProofDialog}"),
				navActions.calls
			)
		}
	}

	@Test
	fun when_cancelIsTappedWhileFetching_then_theDialogPopsItselfWithoutASnackBar() = runTuIndiceUiTest {
		val navActions = RecordingNavActions()
		val snackBarMessages = mutableListOf<SnackBarMessage>()

		withEntriesKoin(getEnrollmentProof = { awaitCancellation() }) {
			setEnrollmentProofDialogEntry(navActions = navActions, snackBarMessages = snackBarMessages)

			onNodeWithTag(EnrollmentProofUiTags.FetchingCancelButton).performClick()

			assertEquals(listOf("pop"), navActions.calls)
			assertEquals(emptyList(), snackBarMessages)
		}
	}

	private fun ComposeUiTest.setEnrollmentProofDialogEntry(
		navActions: TuIndiceNavActions,
		snackBarMessages: MutableList<SnackBarMessage>
	) {
		setTuIndiceTestContent {
			val storeOwner = remember { TestViewModelStoreOwner() }
			val entries = remember {
				entryProvider<NavKey> {
					enrollmentProofEntries(
						navActions = navActions,
						shellBindings = NavShellBindings(
							onViewStateChanged = {},
							showSnackBar = { message -> snackBarMessages += message }
						),
						onNavigateToUpdatePassword = {}
					)
				}
			}

			CompositionLocalProvider(LocalViewModelStoreOwner provides storeOwner) {
				entries(EnrollmentProofDestination.EnrollmentProofDialog).Content()
			}
		}
	}

	private inline fun withEntriesKoin(
		noinline getEnrollmentProof: suspend () -> EnrollmentProof,
		block: () -> Unit
	) {
		stopKoin()
		startKoin {
			modules(
				module {
					factory { createEnrollmentProofViewModel(getEnrollmentProof) }
					single<FileOpenerRepository> { NoOpFileOpenerRepository }
				}
			)
		}

		try {
			block()
		} finally {
			stopKoin()
		}
	}

	private fun createEnrollmentProofViewModel(
		getEnrollmentProof: suspend () -> EnrollmentProof
	): EnrollmentProofViewModel {
		val useCase = FetchEnrollmentProofUseCase(
			applicationRepository = FakeFileRepository(canOpenResult = true),
			enrollmentProofRepository = object : EnrollmentProofRepository {
				override suspend fun getEnrollmentProof(): EnrollmentProof = getEnrollmentProof()
			},
			reportingRepository = RecordingReportingRepository(),
			exceptionHandler = FetchEnrollmentProofExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = true)
			)
		)

		return EnrollmentProofViewModel(
			screenMachine = EnrollmentProofMachine(
				fetchEnrollmentProofUseCase = useCase,
				textProvider = DefaultEnrollmentProofTextProvider()
			),
			eventPublisher = NoOpEventPublisher
		)
	}

	private class RecordingNavActions : TuIndiceNavActions {
		val calls = mutableListOf<String>()

		override fun push(key: Destination) {
			calls += "push:$key"
		}

		override fun pop(): Boolean {
			calls += "pop"
			return true
		}

		override fun popWithResult(result: NavResult): Boolean {
			calls += "popWithResult:$result"
			return true
		}
	}

	private class TestViewModelStoreOwner : ViewModelStoreOwner {
		override val viewModelStore = ViewModelStore()
	}

	private data object NoOpFileOpenerRepository : FileOpenerRepository {
		override fun openFile(file: PlatformFile): Boolean = true
	}
}
