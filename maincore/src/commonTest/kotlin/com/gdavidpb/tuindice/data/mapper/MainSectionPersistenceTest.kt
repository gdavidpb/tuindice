package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.base.domain.model.MainSection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MainSectionPersistenceTest {
	@Test
	fun everySectionReadsBackAsItWasWritten() {
		MainSection.entries.forEach { section ->
			assertEquals(section, section.toPersistedName().toMainSectionOrThrow())
		}
	}

	@Test
	fun theAboutValueOfTheFifthTabReadsBackAsTheSummary() {
		// A student who closed the app on the old About tab left "about" behind; it opens on the summary now.
		assertEquals(MainSection.SUMMARY, "about".toMainSectionOrThrow())
	}

	@Test
	fun theAppNeverWritesTheAboutValueAgain() {
		assertEquals(
			setOf("summary", "record", "pensum", "evaluations"),
			MainSection.entries.map { section -> section.toPersistedName() }.toSet()
		)
	}

	@Test
	fun anUnknownValueIsStillRefused() {
		assertFailsWith<IllegalArgumentException> { "settings".toMainSectionOrThrow() }
	}
}
