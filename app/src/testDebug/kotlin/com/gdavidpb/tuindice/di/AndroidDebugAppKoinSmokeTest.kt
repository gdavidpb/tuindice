package com.gdavidpb.tuindice.di

import android.app.Application
import com.gdavidpb.tuindice.base.domain.repository.EventSubscriber
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.base.domain.startup.AppStartupTask
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The graph of the debug build, which is the one the E2E flows run against: what
 * [AndroidAppKoinSmokeTest] loads plus `androidDebugVariantModule` on top, as `MockTuIndiceApp`
 * starts it. Lives in the debug source set because the variant module does.
 */
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
}
