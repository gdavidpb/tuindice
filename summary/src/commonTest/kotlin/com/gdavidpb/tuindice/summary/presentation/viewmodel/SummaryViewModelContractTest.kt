package com.gdavidpb.tuindice.summary.presentation.viewmodel

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryMachine
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import com.gdavidpb.tuindice.summary.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.summary.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.summary.testing.RecordingUserRepository
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SummaryViewModelContractTest {
	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun initialAction_observesSummary_andUserActionEmitsPickerEffect() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createViewModel(dispatchers = dispatchers)
			val stateCollector = backgroundScope.launchStateCollector(
				flow = viewModel.state,
				testScheduler = testScheduler
			)

			try {
				val content = withTimeout(3_000) {
					viewModel.state
						.filterIsInstance<Summary.State.Content>()
						.first()
				}
				assertEquals("Ana Diaz", content.name)
				assertEquals(DEFAULT_SUMMARY_USER.pictureUrl, content.profilePictureUrl)

				val pickerEffect = async { viewModel.effect.first() }
				viewModel.pickProfilePictureAction()

				assertIs<Summary.Effect.OpenPicker>(pickerEffect.await())
			} finally {
				stateCollector.cancel()
			}
		}
	}

	private fun createViewModel(
		dispatchers: TuIndiceDispatchers
	): SummaryViewModel {
		val userRepository = RecordingUserRepository(users = flowOf(DEFAULT_SUMMARY_USER))

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
