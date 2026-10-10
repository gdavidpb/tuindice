package com.gdavidpb.tuindice.di

import android.app.Application
import com.gdavidpb.tuindice.base.domain.repository.EventSubscriber
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.base.domain.startup.AppStartupTask
import com.gdavidpb.tuindice.debug.OverridableClock
import com.gdavidpb.tuindice.debug.freezeDebugClock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * The graph of the debug build, which is the one the E2E flows run against: what
 * [AndroidAppKoinSmokeTest] loads plus `androidDebugVariantModule` on top, as `MockTuIndiceApp`
 * starts it. Lives in the debug source set because the variant module does.
 */
@OptIn(ExperimentalTime::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class AndroidDebugAppKoinSmokeTest {
	private val debugGraph = AndroidKoinGraph(platformVariantModules = listOf(androidDebugVariantModule))

	// The variant module answers the Firebase-backed contracts itself, so nothing stands in here.
	// The Firebase definitions are still declared under their own type, and still not built.
	@Test
	fun everyDefinitionResolves() = debugGraph.start {
		assertNoProblems(resolutionProblems(exclusions = firebaseExclusions))
	}

	@Test
	fun theDefinitionsNotBuiltOnTheHostAreDeclaredWithWhatTheyAskFor() {
		assertNoProblems(debugGraph.exclusionProblems(exclusions = firebaseExclusions))
	}

	@Test
	fun theStartupListsAllResolve() = debugGraph.start {
		val tasks = debugGraph.declaring(AppStartupTask::class)

		assertEquals(
			listOf("androidDebugUsageDataCollectionTask", "summaryProfilePictureImageLoaderTask"),
			tasks.map { definition -> definition.qualifier?.value }.sortedBy { name -> name.orEmpty() }
		)
		assertEquals(tasks.size, getAll<AppStartupTask>().size)
		assertEquals(debugGraph.declaring(EventSubscriber::class).size, getAll<EventSubscriber>().size)
		assertEquals(debugGraph.declaring(SessionMemory::class).size, getAll<SessionMemory>().size)
	}

	// The clock the app reads for the dates it shows is the one the E2E launch argument freezes, and
	// whoever asks Koin for it gets that same instance.
	@Test
	fun theClockTheLaunchArgumentFreezesIsTheOneTheAppReads() = debugGraph.start {
		assertTrue(get<Clock>() is OverridableClock)

		freezeDebugClock(Instant.parse("2026-10-15T12:00:00Z"))

		assertEquals(Instant.parse("2026-10-15T12:00:00Z"), get<Clock>().now())
	}
}
