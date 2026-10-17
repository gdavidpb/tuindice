package com.gdavidpb.tuindice.di

import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.base.data.source.network.ASSISTANT_STREAM_HTTP_CLIENT_QUALIFIER
import com.gdavidpb.tuindice.base.domain.repository.EvaluationsWeekFocusRepository
import com.gdavidpb.tuindice.base.domain.repository.RecordAttemptFocusRepository
import com.gdavidpb.tuindice.base.domain.repository.ScheduleClockRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.base.domain.session.SessionResidue
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationsRevalidationRepository
import com.gdavidpb.tuindice.pensum.presentation.model.PensumTopBarActionBus
import com.gdavidpb.tuindice.persistence.domain.repository.PensumProgressRepository
import com.gdavidpb.tuindice.persistence.domain.repository.VisibleEvaluationsRepository
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.record.presentation.viewmodel.ScheduleViewModel
import com.gdavidpb.tuindice.summary.data.source.IosImageShareDataSource
import com.gdavidpb.tuindice.summary.data.source.IosProfilePictureInputDataSource
import com.gdavidpb.tuindice.summary.domain.repository.ImageShareRepository
import com.gdavidpb.tuindice.summary.presentation.model.StatisticsTopBarActionBus
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.testkit.koin.assertResolves
import com.gdavidpb.tuindice.testkit.koin.withStartedKoin
import io.ktor.client.HttpClient
import io.ktor.client.plugins.pluginOrNull
import io.ktor.client.plugins.sse.SSE
import org.koin.core.qualifier.named
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class IosAppKoinSmokeTest {
	@Test
	fun resolvesIosAppEntryPoints() = withStartedKoin(
		start = {
			startAppKoin(
				AppKoinBootstrapRequest(
					platformBootstrap = IosKoinBootstrap(
						iOSContext = iosSmokeTestContext(),
						platformVariantModules = emptyList()
					)
				)
			)
		}
		) {
			assertResolves(
				SignInViewModel::class,
				MainViewModel::class,
				BrowserViewModel::class,
				RecordViewModel::class,
				ScheduleViewModel::class,
				SummaryViewModel::class,
				// The buses of the Pensum and Statistics top bars, which the shared route asks for on both platforms.
				PensumTopBarActionBus::class,
				StatisticsTopBarActionBus::class,
				// The jump to a week of Evaluations that the host asks for from the schedule and from Summary.
				EvaluationsWeekFocusRepository::class,
				// The jump to an attempt in Record that the host asks for from Summary.
				RecordAttemptFocusRepository::class,
				// The read contracts of the summary hub, which Summary's view model consumes through the
				// use case of the hub (the record, its prerequisite and the evaluations among them).
				ScheduleClockRepository::class,
				VisibleEvaluationsRepository::class,
				PensumProgressRepository::class,
				EvaluationsRevalidationRepository::class
			)
		}

	// The sign-out wipe and the start without a session reach the normalized profile pictures only
	// through these two bindings, which live in the iOS platform module.
	@Test
	fun bindsTheProfilePictureInputAsSessionMemoryAndResidue() = withStartedKoin(
		start = {
			startAppKoin(
				AppKoinBootstrapRequest(
					platformBootstrap = IosKoinBootstrap(
						iOSContext = iosSmokeTestContext(),
						platformVariantModules = emptyList()
					)
				)
			)
		}
	) {
		assertTrue(getAll<SessionMemory>().any { holder -> holder is IosProfilePictureInputDataSource })
		assertTrue(getAll<SessionResidue>().any { holder -> holder is IosProfilePictureInputDataSource })
	}

	// The statistics image goes to the share sheet through the iOS implementation, never a stand-in.
	@Test
	fun bindsTheImageShareToTheIosSheet() = withStartedKoin(
		start = {
			startAppKoin(
				AppKoinBootstrapRequest(
					platformBootstrap = IosKoinBootstrap(
						iOSContext = iosSmokeTestContext(),
						platformVariantModules = emptyList()
					)
				)
			)
		}
	) {
		assertTrue(get<ImageShareRepository>() is IosImageShareDataSource)
	}

	// The assistant's events come through a client of their own: the iOS platform module builds it under its qualifier,
	// with the SSE plugin, which the Darwin engine must support. The shared client is another one.
	@Test
	fun bindsTheAssistantStreamClientUnderItsQualifierWithSse() = withStartedKoin(
		start = {
			startAppKoin(
				AppKoinBootstrapRequest(
					platformBootstrap = IosKoinBootstrap(
						iOSContext = iosSmokeTestContext(),
						platformVariantModules = emptyList()
					)
				)
			)
		}
	) {
		val stream = get<HttpClient>(named(ASSISTANT_STREAM_HTTP_CLIENT_QUALIFIER))

		assertNotSame(get<HttpClient>(), stream)
		assertNotNull(stream.pluginOrNull(SSE))
	}
}
