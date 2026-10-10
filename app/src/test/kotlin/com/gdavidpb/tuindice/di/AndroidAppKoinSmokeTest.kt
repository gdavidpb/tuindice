package com.gdavidpb.tuindice.di

import android.app.Application
import com.gdavidpb.tuindice.about.presentation.utils.ShareTextHandler
import com.gdavidpb.tuindice.auth.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.base.data.repository.config.RemoteConfigDataRepository
import com.gdavidpb.tuindice.base.data.repository.messaging.PushTokenDataRepository
import com.gdavidpb.tuindice.base.data.source.config.DebugRemoteConfigDataSource
import com.gdavidpb.tuindice.base.data.source.messaging.DebugPushTokenDataSource
import com.gdavidpb.tuindice.base.data.source.reporting.DebugReportingDataSource
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.base.domain.repository.EventSubscriber
import com.gdavidpb.tuindice.base.domain.repository.FileOpenerRepository
import com.gdavidpb.tuindice.base.domain.repository.PendingChangesRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.ReviewRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.domain.repository.UpdateRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.base.domain.session.SessionResidue
import com.gdavidpb.tuindice.base.domain.startup.AppStartupTask
import com.gdavidpb.tuindice.base.utils.DefaultRemoteConfigValues
import com.gdavidpb.tuindice.data.source.activity.CurrentActivityDataSource
import com.gdavidpb.tuindice.data.source.config.AndroidRemoteConfigDataSource
import com.gdavidpb.tuindice.data.source.messaging.FirebasePushTokenDataSource
import com.gdavidpb.tuindice.data.source.reporting.CrashlyticsReportingDataSource
import com.gdavidpb.tuindice.pensum.presentation.model.PensumTopBarActionBus
import com.gdavidpb.tuindice.platform.android.PushTokenRotationHandler
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.ui.screen.BrowserScreenRenderer
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Loads the graph the Android app starts with, on the host: the shared modules plus
 * `androidPlatformModule`, through [AndroidKoinBootstrap]. The shared modules have their own smoke
 * tests against fakes; this is the only place where they meet the Android bindings, so a binding
 * that is missing or asks for something nobody provides fails here and not when the app opens.
 *
 * Not covered: the `if (!BuildConfig.DEBUG)` block of the platform module. A debug unit test
 * cannot load it, so the Firebase Analytics and Performance definitions, their two event
 * subscribers and the usage data `AppStartupTask` of a release build are not checked by anything.
 */
@OptIn(ExperimentalTime::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class AndroidAppKoinSmokeTest {
	// What TuIndiceApp starts.
	private val productionGraph = AndroidKoinGraph()

	// The same, with the Firebase-backed contracts answered by something the host can build.
	private val hostGraph = AndroidKoinGraph(platformVariantModules = listOf(hostStandInsModule))

	@Test
	fun everyDefinitionResolves() = hostGraph.start {
		assertNoProblems(resolutionProblems(exclusions = firebaseExclusions))
	}

	@Test
	fun theDefinitionsNotBuiltOnTheHostAreDeclaredWithWhatTheyAskFor() {
		assertNoProblems(productionGraph.exclusionProblems(exclusions = firebaseExclusions))
	}

	// A stand-in hides the contract it answers. This is what says the platform module still
	// provides each of them, and through the definition that is checked above.
	@Test
	fun thePlatformModuleProvidesTheContractsTheHostReplaces() {
		firebaseBackedContracts.forEach { (contract, provider) ->
			assertEquals(
				"Definitions providing ${contract.qualifiedName}",
				listOf(provider),
				productionGraph.declaring(contract).map { definition -> definition.primaryType }
			)
		}
	}

	// Building every definition cannot see one that is gone, and when nothing else depends on it
	// only the code that asks Koin for it by hand would notice. These are those call sites:
	// AndroidKoinBootstrap.afterStart, TuIndiceMessagingService, every koinInject() of the shared
	// UI, and the view models the iOS smoke test resolves.
	@Test
	fun whatTheAppAsksKoinForByHandResolves() = hostGraph.start {
		listOf(
			CurrentActivityDataSource::class,
			PushTokenRotationHandler::class,
			BrowserScreenRenderer::class,
			ShareTextHandler::class,
			FileOpenerRepository::class,
			BrowserRepository::class,
			ReviewRepository::class,
			UpdateRepository::class,
			SyncStatusRepository::class,
			PendingChangesRepository::class,
			SessionInvalidationRepository::class,
			PensumTopBarActionBus::class,
			SignInViewModel::class,
			MainViewModel::class,
			BrowserViewModel::class,
			RecordViewModel::class
		).forEach { type -> get<Any>(type) }
	}

	// Release reads the system clock: nothing in the production graph can freeze it.
	@Test
	fun theClockOfTheProductionGraphIsTheSystemOne() = hostGraph.start {
		assertSame(Clock.System, get<Clock>())
	}

	@Test
	fun theSessionMemoryHoldersAllResolve() = hostGraph.start {
		val declared = hostGraph.declaring(SessionMemory::class)

		assertTrue(declared.isNotEmpty())
		assertEquals(declared.size, getAll<SessionMemory>().size)
	}

	@Test
	fun theSessionResidueHoldersAllResolve() = hostGraph.start {
		val declared = hostGraph.declaring(SessionResidue::class)

		assertTrue(declared.isNotEmpty())
		assertEquals(declared.size, getAll<SessionResidue>().size)
	}

	// Startup tasks and event subscribers are collected with getAll, where a definition without a
	// qualifier replaces the previous one of its type without a word. The names are what start-up
	// runs on a debug build; none of them is started here.
	@Test
	fun theStartupTasksAndEventSubscribersAllResolve() = hostGraph.start {
		val tasks = hostGraph.declaring(AppStartupTask::class)
		val subscribers = hostGraph.declaring(EventSubscriber::class)

		assertEquals(
			listOf("summaryProfilePictureImageLoaderTask"),
			tasks.map { definition -> definition.qualifier?.value }
		)
		assertEquals(tasks.size, getAll<AppStartupTask>().size)
		assertEquals(subscribers.size, getAll<EventSubscriber>().size)
	}
}

/**
 * The definitions that are not built on the host, and why. Each one is checked by what it declares
 * in `theDefinitionsNotBuiltOnTheHostAreDeclaredWithWhatTheyAskFor`.
 *
 * Everything else is built, which is not the same as used: the Play Core managers (update, review,
 * integrity), `KSafe` and the Tink secure store are created and never called. The host has neither
 * Play services nor an Android keystore, and creating them asks for neither.
 */
internal val firebaseExclusions = listOf(
	HostExclusion(
		type = FirebaseRemoteConfig::class,
		reason = "getInstance() needs a FirebaseApp, and starting one on the host opens Firebase",
		asksFor = listOf(DefaultRemoteConfigValues::class)
	),
	HostExclusion(
		type = FirebaseMessaging::class,
		reason = "getInstance() needs a FirebaseApp, and starting one on the host opens Firebase",
		asksFor = emptyList()
	),
	HostExclusion(
		type = FirebaseCrashlytics::class,
		reason = "getInstance() needs a FirebaseApp, and starting one on the host opens Firebase",
		asksFor = emptyList()
	),
	HostExclusion(
		type = AndroidRemoteConfigDataSource::class,
		reason = "Takes the FirebaseRemoteConfig that is not built"
	),
	HostExclusion(
		type = FirebasePushTokenDataSource::class,
		reason = "Takes the FirebaseMessaging that is not built"
	),
	HostExclusion(
		type = CrashlyticsReportingDataSource::class,
		reason = "Takes the FirebaseCrashlytics that is not built"
	)
)

// The contracts whose Android implementation is one of the excluded definitions. About half of the
// graph reaches one of them, so without an answer the host could build none of that half.
private val firebaseBackedContracts = mapOf(
	RemoteConfigDataRepository::class to AndroidRemoteConfigDataSource::class,
	PushTokenDataRepository::class to FirebasePushTokenDataSource::class,
	ReportingRepository::class to CrashlyticsReportingDataSource::class
)

private const val HOST_SOURCE_NAME = "android-host-test"

private val hostStandInsModule = module {
	single<RemoteConfigDataRepository> {
		DebugRemoteConfigDataSource(defaults = get(), sourceName = HOST_SOURCE_NAME)
	}
	single<PushTokenDataRepository> {
		DebugPushTokenDataSource(token = "android-host-test-push-token", sourceName = HOST_SOURCE_NAME)
	}
	single<ReportingRepository> {
		DebugReportingDataSource(sourceName = HOST_SOURCE_NAME)
	}
}

internal fun assertNoProblems(problems: List<String>) {
	assertTrue(problems.joinToString(separator = "\n\n", prefix = "\n"), problems.isEmpty())
}
